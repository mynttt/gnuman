package de.hshannover.inform.gnuman.app.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.util.Base64;

import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Parser to read maps. This allows to use custom maps made by an editor.<br>
 * @author Marc Herschel
 */

public class MapParser {

    private enum State {
        METADATA, GHOSTDATA, MAPDATA, BLOCKED, SLOW_DOWN, BACKGROUND, BACKGROUND_FLASH
    }

    /**
     * Construct a map parser.
     * @param location of map
     * @param internalMap if inside jar
     * @return parsing reply containing success and data
     * @throws Exception if we fail to load anything.
     */
    public static ParsingReply parseMap(String location, boolean internalMap) throws Exception {
        MapData data = new MapData(internalMap);
        try (BufferedReader reader =
                internalMap ? new BufferedReader(new InputStreamReader(MapParser.class.getResourceAsStream(location)))
                            : new BufferedReader(new FileReader(location))){
            internalParsing(reader, data);
        }
        return new ParsingReply(data, validate(data));
    }

    /**
     * Validate data and return a status code.
     * @param data to validate
     * @return <pre> 0 if no errors.
     * -1 if errors with metadata
     * -2 if errors with scatter points
     * -3 if errors with the map data
     * -4 if errors with the background image
     * -5 if errors with the flash image
     * -6 if more then one player spawn
     * -7 if no player spawn
     * -8 illegal horizontal teleporter
     * -9 illegal vertical teleporter
     * -10 mapdata is null
     * </pre>
     */
    public static ParsingStatus validate(MapData data) {
        ParsingStatus s = new ParsingStatus(0, 0, 0);
        if(data == null) { s.code = -10; return s; }
        if((data.getWidthInBlocks() == 0 || data.getHeightInBlocks() == 0 || data.getName() == null || data.getAuthor() == null || data.getCreationDate() == null)) { s.code = -1; return s; }
        for(int i = 1; i < EntityObjects.values().length; i++) {
            if(data.getScatterPoint(EntityObjects.values()[i]) == null) { s.code = -2; return s; }
        }
        int playerspawns = 0;
        for(int y = 0; y < data.getHeightInBlocks(); y++) {
            for(int x = 0; x < data.getWidthInBlocks(); x++) {
                if(data.getData()[y][x] == StaticObjects.PLAYER_SPAWN) { playerspawns++; }
                if(data.getData()[y][x] == null) { s.code = -3; return s; }
            }
        }
        if(playerspawns < 1) { s.code = -7; return s; }
        if(playerspawns > 1) { s.code = -6; return s; }
        if(data.getBackground() == null || data.getBackground().isError()) { s.code = -4; return s; }
        if(data.getBackgroundFlash() == null || data.getBackground().isError()) { s.code = -5; return s; }
        for(int y = 0; y < data.getHeightInBlocks(); y++) {
            if(data.getData()[y][0] == StaticObjects.WALL ^ data.getData()[y][data.getWidthInBlocks()-1] == StaticObjects.WALL) {
                s.code = -8; s.y = y; s.x = data.getData()[y][0] == StaticObjects.WALL ? 0 : data.getWidthInBlocks()-1; return s;
            }
        }
        for(int x = 0; x < data.getWidthInBlocks(); x++) {
            if(data.getData()[0][x] == StaticObjects.WALL ^ data.getData()[data.getHeightInBlocks()-1][x] == StaticObjects.WALL) {
                s.code = -9; s.x = x; s.y = data.getData()[0][x] == StaticObjects.WALL ? 0 : data.getHeightInBlocks()-1; return s;
            }
        }
        return s;
    }

    /**
     * Dump a map to the .GNUMAN format. The map must be valid, if not this will throw some exception.
     * @param data to dump.
     * @param location of the map to dump.
     * @throws Exception whatever happens when we fail.
     */
    public static void dumpMap(MapData data, String location) throws Exception {
        StringBuilder sb = new StringBuilder(5000);
        sb.append(":meta\n");
        sb.append(data.getName());
        sb.append('\n');
        sb.append(data.getAuthor());
        sb.append('\n');
        sb.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(data.getCreationDate()));
        sb.append('\n');
        sb.append(data.getWidthInBlocks());
        sb.append('\n');
        sb.append(data.getHeightInBlocks());
        sb.append("\n:ghosts\n");
        data.getScatterPoints().forEach((k, v) -> sb.append(k + " " + v.getCellX() + " " + v.getCellY() + '\n'));
        sb.append(":blocked\n");
        data.getBlocked().forEach((k, v) -> {
            sb.append(k+" ");
            if(v.size() == 0) {
                sb.append("NONE");
            } else {
                v.forEach(cell -> sb.append(cell.getCellX() + " " + cell.getCellY() + " "));
            }
            if(v.size() != 0) sb.deleteCharAt(sb.length()-1);
            sb.append('\n');
        });
        sb.append(":slowdown\n");
        boolean triggeredOnce = false;
        for(int y = 0; y < data.getHeightInBlocks(); y++) {
            for(int x = 0; x < data.getWidthInBlocks(); x++) {
                if(data.getSlowdown()[x][y]) {
                    triggeredOnce = true;
                    sb.append(x + " " + y + " ");
                }
            }
        }
        if(triggeredOnce) { sb.deleteCharAt(sb.length()-1); } else { sb.append("NONE"); }
        sb.append('\n');
        sb.append(":map\n");
        for(StaticObjects[] y : data.getData()) {
            for(StaticObjects x : y) { sb.append(x.ordinal()); }
            sb.append('\n');
        }
        sb.append(":background\n");
        sb.append(imageToBase64(data.getBackground()));
        sb.append('\n');
        sb.append(":flash\n");
        sb.append(imageToBase64(data.getBackgroundFlash()));
        try (BufferedWriter br = new BufferedWriter(new FileWriter(new File(location)))) {
            br.append(sb.toString());
        }
    }

    /**
     * @param status status code of validation
     * @return description of code
     */
    static String lookupStatusCode(ParsingStatus status) {
        switch(status.code) {
            case 0: return "SUCCESS";
            case -1: return "CORRUPT OR MISSING METADATA";
            case -2: return "CORRUPT OR MISSING SCATTER DATA";
            case -3: return "CORRUPT OR MISSING MAP DATA";
            case -4: return "CORRUPT OR MISSING BACKGROUND";
            case -5: return "CORRUPT OR MISSING FLASHMAP";
            case -6: return "PLAYERSPAWN AMOUNT > 1";
            case -7: return "MISSING PLAYER SPAWN";
            case -8: return "ILLEGAL HORIZONTAL TELEPORTER @ [X:" +status.x + "|Y:" + status.y+"]";
            case -9: return "ILLEGAL VERTICAL TELEPORTER   @ [X:" +status.x + "|Y:" + status.y+"]";
            case -10: return "MAPDATA IS NULL";
            default: return "UNKNOWN STATUS CODE";
        }
    }

    /**
     * Convert an Image to Base64
     * @param image JavaFX image
     * @return Base64 representation of the image.
     * @throws Exception if something fails.
     */
    private static String imageToBase64(Image image) throws Exception {
        String result = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedImage to = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(SwingFXUtils.fromFXImage(image, to), "png", out);
        result = Base64.getEncoder().encodeToString(out.toByteArray());
        out.close();
        return result;
    }

    /**
     * Internal parsing operation
     * @param reader to use
     * @param data to write into
     * @throws Exception if we fail to parse anything.
     */
    private static void internalParsing(BufferedReader reader, MapData data) throws Exception {
        String currentLine;
        State state = null;
        int metaLine = 0, mapY = 0, mapX = 0;
        while((currentLine = reader.readLine()) != null) {
            if(currentLine.startsWith(":meta")) { state = State.METADATA; continue; }
            if(currentLine.startsWith(":ghosts")) { state = State.GHOSTDATA; continue; }
            if(currentLine.startsWith(":map")) { state = State.MAPDATA; continue; }
            if(currentLine.startsWith(":blocked")) { state = State.BLOCKED; continue; }
            if(currentLine.startsWith(":background")) { state = State.BACKGROUND; continue; }
            if(currentLine.startsWith(":flash")) { state = State.BACKGROUND_FLASH; continue; }
            if(currentLine.startsWith(":slowdown")) { state = State.SLOW_DOWN; continue; }
            switch(state) {
            case GHOSTDATA:
                Scanner ghostLine = new Scanner(currentLine);
                EntityObjects entity = EntityObjects.valueOf(ghostLine.next());
                int x = ghostLine.nextInt();
                int y = ghostLine.nextInt();
                data.addScatterPoint(entity, x, y);
                break;
            case MAPDATA:
                for(char c : currentLine.toCharArray()) { data.setMapData(c, mapX++, mapY); }
                mapX = 0;
                mapY++;
                break;
            case BLOCKED:
                Scanner blocked = new Scanner(currentLine);
                Directions d = Directions.valueOf(blocked.next());
                String s = blocked.next();
                if(s.startsWith("NONE")) { break; }
                data.addBlocked(d, Integer.parseInt(s), blocked.nextInt());
                while(blocked.hasNextInt()) { data.addBlocked(d, blocked.nextInt(), blocked.nextInt()); }
                break;
            case SLOW_DOWN:
                if(currentLine.startsWith("NONE")) { data.initiateEmptySlowdown(); break; }
                Scanner slowdown = new Scanner(currentLine);
                while(slowdown.hasNextInt()) { data.addSlowdown(slowdown.nextInt(), slowdown.nextInt()); }
                break;
            case METADATA:
                switch(metaLine++) {
                    case 0: data.setName(currentLine); break;
                    case 1: data.setAuthor(currentLine); break;
                    case 2: data.setCreationDate(currentLine); break;
                    case 3: data.setWidthInBlocks(Integer.parseInt(currentLine)); break;
                    case 4: data.setHeightInBlocks(Integer.parseInt(currentLine)); break;
                    default: break;
                }
                break;
            case BACKGROUND:
                data.setBackground(new ByteArrayInputStream(Base64.getDecoder().decode(currentLine)));
                break;
            case BACKGROUND_FLASH:
                data.setBackgroundFlash(new ByteArrayInputStream(Base64.getDecoder().decode(currentLine)));
                break;
            default:
                throw new RuntimeException("??????????");
            }
        }
    }
}
