package com.covertlizard.api.radio.event.midi;

import com.covertlizard.api.radio.sound.MidiSound;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by CovertLizard on 5/3/2015.
 * Project RadioAPI
 */
public class MidiPauseEvent extends Event implements Cancellable
{
    private static final HandlerList handlerList = new HandlerList();

    private MidiSound midiSound;
    private boolean cancelled;
    /**
     * Occurs when a midi sound starts
     * @param midiSound the midi sound instance
     */
    public MidiPauseEvent(MidiSound midiSound)
    {
        this.midiSound = midiSound;
        this.cancelled = false;
    }
    /**
     * Get's the midi music instance
     * @return the midi music instance
     */
    public MidiSound getMidiSound()
    {
        return this.midiSound;
    }
    @Override
    public HandlerList getHandlers()
    {
        return handlerList;
    }
    public static HandlerList getHandlerList()
    {
        return handlerList;
    }
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}