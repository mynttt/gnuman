package de.hshannover.inform.gnuman.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.modules.audio.Music;
import de.hshannover.inform.gnuman.app.modules.audio.Sound;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Manages sounds inside the game.
 * @author Marc Herschel
 */

public class AudioManager {
    private GameSettings settings;
    private HashMap<String, Sound> sounds;
    private HashMap<String, Music> music;

    private AudioManager(GameSettings settings) {
        this.settings = settings;
        this.sounds = new HashMap<>();
        this.music = new HashMap<>();

        try {
            initiateSounds();
        } catch (Exception e) {
            Log.critical(getClass().getSimpleName(), "Failed to initiate the sound system.\n" + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
        }
    }

    private void initiateSounds() throws Exception {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(Constants.AUDIO_PARSE_MUSIC)))) {
            loadSound(br, false);
        }
        try(BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(Constants.AUDIO_PARSE_SOUND)))) {
            loadSound(br, true);
        }
    }

    private void loadSound(BufferedReader br, boolean isSound) throws Exception {
        String s; String[] r;
        while((s = br.readLine()) != null) {
            if(s.startsWith("#")) { continue; }
            r = s.split("\\s+");
            String key = r[0];
            URL resource = getClass().getResource(Constants.AUDIO_FILE_PREFIX + r[1]);
            float volume = Float.parseFloat(r[2]);
            if(isSound) {
                sounds.put(key, new Sound(resource, volume));
            } else {
                music.put(key, new Music(resource, volume));
            }
            Log.info(getClass().getSimpleName(), "Loaded: " + (isSound ? "Sound :" : "Music :") + resource.toExternalForm());
        }
    }

    public void playSound(String key) { if(settings.isSoundOn()) { sounds.get(key).createInstance(); } }
    public void playMusic(String key) { if(settings.isMusicOn()) { music.get(key).start(); } }
    public void stopMusic(String key) { music.get(key).stop(); }

    public void stopEntireAudio() {
        music.values().forEach(Music::stopAllInstances);
        sounds.values().forEach(Sound::stopAllInstances);
    }

    public void adjustAll(float scale) {
        music.values().forEach(m -> m.setVolume(m.getInitialVolume()*scale));
        sounds.values().forEach(s -> s.setVolume(s.getInitialVolume()*scale));
    }

    public void decideMusic(boolean isElroy, boolean isFrightened) {
        stopGameMusic();
        if(isElroy && !isFrightened) { playMusic("MUSIC_ELROY"); return; }
        if(isFrightened) { playMusic("MUSIC_FRIGHTENED"); return; }
        playMusic("MUSIC_NORMAL");
    }

    public void toggleMainTheme() {
        if(music.get("MUSIC_MENU").isPlaying()) { music.get("MUSIC_MENU").stop(); }
        if(!music.get("MUSIC_MENU").isPlaying() && settings.isMusicOn()) {  music.get("MUSIC_MENU").start(); }
    }

    public void startUiMusic() { if(settings.isMusicOn() && !music.get("MUSIC_MENU").isPlaying()) { music.get("MUSIC_MENU").start(); } }
    public void stopUiMusic() { music.get("MUSIC_MENU").stop(); }
    public void stopGameMusic() { music.entrySet().stream().filter(k -> !k.getKey().equals("EATING_DOTS")).forEach(k -> k.getValue().stop()); }
    public void beginWithDotsSequence() { if(settings.isSoundOn()) { music.get("EATING_DOTS").start(); } }
    public boolean isMusicPlaying(String key) { return music.get(key).isPlaying(); }

    public static AudioManager createAudioManager(GameSettings settings) {
        return new AudioManager(settings);
    }

    public void testAudioFiles() {
        stopEntireAudio();
        System.out.println("Testing sounds...");
        sounds.entrySet().forEach(e -> {
            System.out.println(e.getKey());
            e.getValue().createInstance();
            try { Thread.sleep(1000); } catch (InterruptedException e1) {}
        });
        music.entrySet().forEach(e -> {
            System.out.println(e.getKey());
            stopEntireAudio();
            e.getValue().start();
            try { Thread.sleep(2500); } catch (InterruptedException e1) {}
        });
        System.out.println("Done.");
        stopEntireAudio();
    }
}
