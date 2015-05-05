package com.covertlizard.api.radio.event.station;

import com.covertlizard.api.radio.sound.MusicStation;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by CovertLizard on 5/2/2015.
 * Project RadioAPI
 */
@SuppressWarnings("all")
public class StationStartEvent extends Event implements Cancellable
{
    private static final HandlerList handlerList = new HandlerList();

    private MusicStation musicStation;
    private boolean cancelled;
    /**
     * Occurs when a player tunes into a station
     * @param musicStation the station the player tunes into
     */
    public StationStartEvent(MusicStation musicStation)
    {
        this.musicStation = musicStation;
        this.cancelled = false;
    }
    /**
     * Get's the music station instance
     * @return the music station instance
     */
    public MusicStation getMusicStation()
    {
        return this.musicStation;
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