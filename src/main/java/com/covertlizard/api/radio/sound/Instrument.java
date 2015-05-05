package com.covertlizard.api.radio.sound;

import org.bukkit.Sound;

/**
 * Created by CovertLizard on 4/30/2015.
 * Project mc-radio
 */
public class Instrument
{
    /**
     * Returns the correct sound to play
     * @param patch the patch
     * @param channel the channel
     * @return the sound instance
     */
    public static Sound getInstrument(int patch, int channel)
    {
        Sound returnSound = (channel == 9 || patch >= 113 && patch <= 119) ? Sound.NOTE_BASS_DRUM : Sound.NOTE_PIANO;
        returnSound = ((patch >= 28 && patch <= 40) || (patch >= 44 && patch <= 46)) ? Sound.NOTE_BASS_GUITAR : returnSound;
        returnSound = (patch >= 120 && patch <= 127) ? Sound.NOTE_SNARE_DRUM : returnSound;
        return returnSound;
    }
}