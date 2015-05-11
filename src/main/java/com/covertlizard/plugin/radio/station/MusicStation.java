package com.covertlizard.plugin.radio.station;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Created by CovertLizard on 5/10/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all")
public class MusicStation
{
    private String id;
    private String name;
    private Material item;
    private boolean random;
    private int interval;

    public MusicStation(String id, String item, boolean random, int interval)
    {
        this.id = id.replaceAll("(&([a-f0-9]))", ""); //get's rid of those nasty color codes
        this.item = Material.getMaterial(item) == null ? Material.RECORD_9 : Material.getMaterial(item);
        this.random = random;
        this.interval = interval;
        this.name = ChatColor.translateAlternateColorCodes('&', id);
    }
    public String getID()
    {
        return id;
    }
    public Material getItem()
    {
        return item;
    }
    public boolean isRandom()
    {
        return random;
    }
    public int getInterval()
    {
        return interval;
    }
    public String getName()
    {
        return this.name;
    }
}