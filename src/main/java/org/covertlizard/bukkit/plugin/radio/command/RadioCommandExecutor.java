package org.covertlizard.bukkit.plugin.radio.command;

import com.covertlizard.api.radio.midi.MidiStation;
import org.covertlizard.bukkit.plugin.radio.RadioPlugin;
import org.covertlizard.bukkit.plugin.radio.reference.Reference;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by CovertLizard on 5/4/2015.
 * Project RadioPlugin
 */
public class RadioCommandExecutor implements CommandExecutor
{
    private RadioPlugin plugin;
    public RadioCommandExecutor(RadioPlugin plugin)
    {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(Reference.PLUGIN_PREFIX + "For available commands type /radio help");
            return true;
        }
        try
        {
            switch (args[0])
            {
                case "pause":
                    if (sender.hasPermission("radio.pause"))
                    {
                        if (args.length == 1)
                        {
                            if (!(sender instanceof Player))
                            {
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You must be a player to run this command without arguments!");
                                break;
                            }
                            if (!this.plugin.stationHelper.isInStation((Player) sender))
                            {
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You are not in a station!");
                                break;
                            }
                            this.plugin.stationHelper.getPlayerStation((Player) sender).pause();
                            break;
                        }
                        if (args.length == 2)
                        {
                            if (!args[1].equalsIgnoreCase("all"))
                            {
                                if (!this.plugin.stationHelper.stationExists(args[1].toLowerCase()))
                                {
                                    this.plugin.stationHelper.getStationFromName(args[1].toLowerCase()).pause();
                                    break;
                                }
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "The station does not exist!");
                                break;
                            }
                            if (sender.hasPermission("radio.pause.all"))
                            {
                                this.plugin.stationHelper.pauseStations();
                                this.plugin.getServer().broadcastMessage(Reference.PLUGIN_PREFIX + ChatColor.DARK_RED + "All radio stations have been paused by " + sender.getName() + "!");
                                break;
                            }
                            sendPermissionInvalidMessage(sender);
                        }
                        if (args.length > 2)
                            sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "Too many arguments, type /radio help");
                        break;
                    }
                    sendPermissionInvalidMessage(sender);
                    break;
                case "stop":
                    if (sender.hasPermission("radio.stop"))
                    {
                        if (args.length == 1)
                        {
                            if (!(sender instanceof Player))
                            {
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You must be a player to run this command without arguments!");
                                break;
                            }
                            if (!this.plugin.stationHelper.isInStation((Player) sender))
                            {
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You are not in a station!");
                                break;
                            }
                            this.plugin.stationHelper.getPlayerStation((Player) sender).end();
                            break;
                        }
                        if (args.length == 2)
                        {
                            if (!args[1].equalsIgnoreCase("all"))
                            {
                                if (!this.plugin.stationHelper.stationExists(args[1].toLowerCase()))
                                {
                                    this.plugin.stationHelper.getStationFromName(args[1].toLowerCase()).end();
                                    break;
                                }
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "The station does not exist!");
                                break;
                            }
                            if (sender.hasPermission("radio.stop.all"))
                            {
                                this.plugin.stationHelper.stopStations();
                                this.plugin.getServer().broadcastMessage(Reference.PLUGIN_PREFIX + ChatColor.DARK_RED + "All radio stations have been stopped by " + sender.getName() + "!");
                                break;
                            }
                            sendPermissionInvalidMessage(sender);
                        }
                        if (args.length > 2)
                            sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "Too many arguments, type /radio help");
                        break;
                    }
                    sendPermissionInvalidMessage(sender);
                    break;
                case "start":
                    if (sender.hasPermission("radio.start"))
                    {
                        if (args.length == 1)
                        {
                            if (!(sender instanceof Player))
                            {
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You must be a player to run this command without arguments!");
                                break;
                            }
                            if (!this.plugin.stationHelper.isInStation((Player) sender))
                            {
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You are not in a station!");
                                break;
                            }
                       // might have to add this     this.plugin.stationHelper.getPlayerStation((Player) sender).load();
                            this.plugin.stationHelper.getPlayerStation((Player) sender).begin();
                            break;
                        }
                        if (args.length == 2)
                        {
                            if (!args[1].equalsIgnoreCase("all"))
                            {
                                if (!this.plugin.stationHelper.stationExists(args[1].toLowerCase()))
                                {
                                  // might have to add this  this.plugin.stationHelper.getStationFromName(args[1].toLowerCase()).start();
                                    this.plugin.stationHelper.getStationFromName(args[1].toLowerCase()).begin();
                                    break;
                                }
                                sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "The station does not exist!");
                                break;
                            }
                            if (sender.hasPermission("radio.start.all"))
                            {
                                for(MidiStation station : this.plugin.stationHelper.getStations()) station.begin();
                                this.plugin.stationHelper.startStations();
                                this.plugin.getServer().broadcastMessage(Reference.PLUGIN_PREFIX + ChatColor.DARK_RED + "All radio stations have been started by " + sender.getName() + "!");
                                break;
                            }
                            sendPermissionInvalidMessage(sender);
                        }
                        if (args.length > 2)
                            sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "Too many arguments, type /radio help");
                        break;
                    }
                    sendPermissionInvalidMessage(sender);
                    break;
                case "help":
                    sender.sendMessage(ChatColor.GOLD + "=====================Radio======================");
                    sender.sendMessage(ChatColor.RED + "/radio pause" + ChatColor.DARK_AQUA + " This pauses the music station you're in.");
                    sender.sendMessage(ChatColor.RED + "/radio pause <station>" + ChatColor.DARK_AQUA + " This pauses the music station specified.");
                    sender.sendMessage(ChatColor.RED + "/radio pause all" + ChatColor.DARK_AQUA + " This pauses every music station.");

                    sender.sendMessage(ChatColor.RED + "/radio stop" + ChatColor.DARK_AQUA + " This stops the music station you're in.");
                    sender.sendMessage(ChatColor.RED + "/radio stop <station>" + ChatColor.DARK_AQUA + " This stops the music station specified.");
                    sender.sendMessage(ChatColor.RED + "/radio stop all" + ChatColor.DARK_AQUA + " This stops every music station.");

                    sender.sendMessage(ChatColor.RED + "/radio start" + ChatColor.DARK_AQUA + " This starts the music station you're in.");
                    sender.sendMessage(ChatColor.RED + "/radio start <station>" + ChatColor.DARK_AQUA + " This starts the music station specified.");
                    sender.sendMessage(ChatColor.RED + "/radio start all" + ChatColor.DARK_AQUA + " This starts every music station.");

                    sender.sendMessage(ChatColor.RED + "/radio reload" + ChatColor.DARK_AQUA + " Reloads the plugin.");
                    sender.sendMessage(ChatColor.GOLD + "================================================");
                    break;
                case "reload":
                    if(sender.hasPermission("radio.reload"))
                    {
                        sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.BLUE + "Reloaded radio plugin.");
                        this.plugin.stationHelper.stopStations();
                        this.plugin.stationHelper.clearStations();
                        this.plugin.registerStations();
                        this.plugin.stationHelper.startStations();
                        break;
                    }
                    sendPermissionInvalidMessage(sender);
                    break;
                default:
                    sender.sendMessage(Reference.PLUGIN_PREFIX + "Unknown argument: " + args[0] + ", type /radio help to view available arguments.");
                    return true;
            }
        } catch (Exception e) {e.printStackTrace(); sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.DARK_RED + "An error has occurred.");return true;}
        return true;
    }
    private void sendPermissionInvalidMessage(CommandSender sender)
    {
        sender.sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "You do not have sufficient permissions.");
    }
}