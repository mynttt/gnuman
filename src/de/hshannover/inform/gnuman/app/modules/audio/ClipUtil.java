package de.hshannover.inform.gnuman.app.modules.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Utility class that is used by the Music / Sound implementation.
 * @author Marc Herschel
 */

public class ClipUtil {

    /**
     * Create a simple clip (for music)
     * @param location of the file
     * @return a clip that you can use to create a music instance with
     */
    static Clip createClip(URL location) {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(location)) {
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
            Clip clip = AudioSystem.getClip();
            clip.open(din);
            din.close();
            return clip;
        } catch (Exception e) {
            Log.critical(ClipUtil.class.getSimpleName(), "Failed to load and convert clip from: " + location.toExternalForm() + "\n" + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
            return null;
        }
    }

    /**
     * Create a SimultaneousClip for sounds.
     * @param location of the file
     * @return a SimultaneousClip that you can use to create a sound instance with
     */
    static SimultaneousClip createSimultaneousClip(URL location) {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(location)) {
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
            byte[] data = getBytesFromInputStream(din);
            return new SimultaneousClip(decodedFormat, data);
        } catch (Exception e) {
            Log.critical(ClipUtil.class.getSimpleName(), "Failed to load and convert SimultaneousClip from: " + location.toExternalForm() + "\n" + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the volume of a clip.
     * @param clip to check for.
     * @return volume (between 0 and 1 inclusive)
     */
    static float getVolume(Clip clip) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        return (float) Math.pow(10f, gainControl.getValue() / 20f);
    }

    /**
     * Set the volume of a clip.
     * @param volume new volume (between 0 and 1 inclusive)
     * @param clip to adjust for
     */
    static void setVolume(float volume, Clip clip) {
        if (volume < 0f || volume > 1f) { throw new IllegalArgumentException("Volume not valid: " + volume); }
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(volume));
    }

    /**
     * Convert an AudioInputStream to a byte array so we can cache the audio for simultaneous clips.
     * @param is to convert
     * @return a byte array of all data
     * @throws IOException if anything goes wrong
     */
    private static byte[] getBytesFromInputStream(AudioInputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) { os.write(buffer, 0, len); }
        return os.toByteArray();
    }
}
