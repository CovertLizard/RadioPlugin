package org.covertlizard.bukkit.plugin.radio.station;

import com.covertlizard.api.radio.midi.MidiStation;
import org.covertlizard.bukkit.plugin.radio.reference.Reference;
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
    private List<MidiStation> stations = new ArrayList<>();
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
        for(MidiStation station : this.stations)
        {
            try
            {
                station.load();
                station.begin();
            } catch (MidiUnavailableException e)
            {
                e.printStackTrace();
            }
        }
    }
    /**
     * Stops all stations from playing music
     */
    public void stopStations()
    {
        for(MidiStation station : this.stations) station.exit();
    }
    /**
     * Pauses/Un-pauses all stations
     */
    public void pauseStations()
    {
        for(MidiStation station : this.stations)
        {
            if(station.isPaused() != stationsPaused) continue;
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
            this.stations.add(new MidiStation(this.plugin, ChatColor.stripColor(stationName), playRandomSong, interval));
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
    public MidiStation getStationFromName(String stationName)
    {
        for(MidiStation station : this.stations) if(station.getStationID().toLowerCase().equalsIgnoreCase(ChatColor.stripColor(stationName))) return station;
        return null;
    }
    /**
     * Toggles mute for a player
     * @param player the player instance
     */
    public void toggleMute(Player player)
    {
        if(!isInStation(player)) return;
        MidiStation station = this.getPlayerStation(player);
        station.mute(player.getUniqueId());
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
    public MidiStation getPlayerStation(Player player)
    {
        for(MidiStation station : this.stations)
        {
            if(station.getPlayers().contains(player.getUniqueId())) return station;
        }
        return null;
    }
    /**
     * Sets the station the player is currently in
     * @param player the player instance
     * @param station the station instance
     */
    public void setPlayerStation(Player player, MidiStation station)
    {
        if(this.isInStation(player) && this.getPlayerStation(player).equals(station))
        {
            player.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You are already tuned into " + ChatColor.WHITE + station.getStationID());
            return;
        }
        station.tune(player.getUniqueId());
    }
    //=========================================
    //               Getters
    //=========================================
    public List<MidiStation> getStations()
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