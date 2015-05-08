package org.bukkit.covertlizard.plugin.radio.station;

import org.bukkit.covertlizard.api.radio.sound.MusicStation;
import org.bukkit.covertlizard.plugin.radio.reference.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sound.midi.MidiUnavailableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by CovertLizard on 5/3/2015.
 * Project RadioPlugin
 */
public class StationHelper
{
    //Enumerables
    private List<MusicStation> stations = new ArrayList<>();
    private List<String> initStations = new ArrayList<>();
    //Booleans
    private boolean stationsPaused = false;
    //Integers
    private int inventorySize = 9;
    //Strings
    private final String RADIO_GUI_NAME = ChatColor.RED + "Please select a station";
    //Misc
    private JavaPlugin plugin;
    private Inventory inventory;
    private ItemStack muteLever = new ItemStack(Material.LEVER, 1);

    public StationHelper(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, this.inventorySize, this.RADIO_GUI_NAME);
        this.inventory.setMaxStackSize(1);
        ItemMeta meta = this.muteLever.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Toggle mute");
        meta.setLore(Arrays.asList(ChatColor.DARK_GREEN + "DJ Lizard in the house!", ChatColor.YELLOW + "Press me to toggle mute."));
        this.muteLever.setItemMeta(meta);
        this.inventory.setItem(inventorySize - 1, this.muteLever);
    }
    /**
     * Starts all stations
     */
    public void startStations()
    {
        for(MusicStation station : this.stations) station.play();
    }
    /**
     * Stops all stations from playing music
     */
    public void stopStations()
    {
        for(MusicStation station : this.stations) station.stop();
    }
    /**
     * Pauses/Un-pauses all stations
     */
    public void pauseStations()
    {
        for(MusicStation station : this.stations)
        {
            if(station.isMusicPaused() != stationsPaused) continue;
            station.pause();
        }
        this.stationsPaused = !this.stationsPaused;
    }
    /**
     * Removes all stations from memory
     */
    public void clearStations()
    {
        this.stations.clear();
    }
    /**
     * Registers a station
     * @param stationName the station's name
     */
    public void registerStation(String stationName, boolean playRandomSong, int interval)
    {
        if(this.stationExists(stationName)) return;
        if(!this.initStations.contains(stationName)) this.initStations.add(stationName);
        try
        {
            this.stations.add(new MusicStation(this.plugin, ChatColor.stripColor(stationName), playRandomSong, interval));
            ItemStack stack = new ItemStack(Material.RECORD_9, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(stationName);
            meta.setLore(Arrays.asList(ChatColor.GREEN + "Click on me to listen to the hottest " + stationName + ChatColor.GREEN + " songs!"));
            stack.setItemMeta(meta);
            if(this.initStations.size() == this.inventorySize - 2)
            {
                this.inventorySize =+ 9;
                this.inventory = Bukkit.createInventory(null, this.inventorySize, this.RADIO_GUI_NAME);
                this.inventory.setItem(this.inventorySize - 1, this.muteLever);
            }
            this.inventory.setItem(this.initStations.size() - 1, stack);
        } catch (MidiUnavailableException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Determines if the station exists in memory
     * @param stationName the station's name
     * @return true if it exists
     */
    public boolean stationExists(String stationName)
    {
        return this.getStationFromName(stationName) != null;
    }
    /**
     * Gets the station instance with the provided station name
     * @param stationName the station's name
     * @return the station instance
     */
    public MusicStation getStationFromName(String stationName)
    {
        for(MusicStation station : this.stations) if(station.getStationName().toLowerCase().equalsIgnoreCase(ChatColor.stripColor(stationName))) return station;
        return null;
    }
    /**
     * Toggles mute for a player
     * @param player the player instance
     */
    public void toggleMute(Player player)
    {
        if(!isInStation(player)) return;
        MusicStation station = this.getPlayerStation(player);
        if(!station.isMuted(player.getUniqueId()))
        {
            station.mute(player.getUniqueId());
            return;
        }
        station.unMute(player.getUniqueId());
    }
    public boolean isInStation(Player player)
    {
        return this.getPlayerStation(player) != null;
    }
    /**
     * Gets the station the player is currently in
     * @param player the player instance
     * @return the station they're in
     */
    public MusicStation getPlayerStation(Player player)
    {
        for(MusicStation station : this.stations)
        {
            if(station.getTunedPlayers().contains(player.getUniqueId())) return station;
        }
        return null;
    }
    /**
     * Sets the station the player is currently in
     * @param player the player instance
     * @param station the station instance
     */
    public void setPlayerStation(Player player, MusicStation station)
    {
        if(this.isInStation(player))
        {
            if(this.getPlayerStation(player).equals(station))
            {
                player.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You are already tuned into " + ChatColor.WHITE + station.getStationName());
                return;
            }
            this.getPlayerStation(player).tuneOut(player.getUniqueId());
        }
        station.tuneIn(player.getUniqueId());
    }
    //=========================================
    //               Getters
    //=========================================
    public List<MusicStation> getStations()
    {
        return this.stations;
    }
    public List<String> getInitStations()
    {
        return this.initStations;
    }
    public boolean isStationsPaused()
    {
        return this.stationsPaused;
    }
    public int getInventorySize()
    {
        return this.inventorySize;
    }
    public Inventory getInventory()
    {
        return this.inventory;
    }
    public ItemStack getMuteLever()
    {
        return this.muteLever;
    }
}