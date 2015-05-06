package com.covertlizard.api.radio.sound;

import com.covertlizard.api.radio.event.station.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by CovertLizard on 5/2/2015.
 * Project RadioAPI
 */
@SuppressWarnings("all") //screw you IDE ;-)
public class MusicStation implements Receiver
{
    //Misc
    private final JavaPlugin plugin;
    private Sequencer sequencer;
    //Enumerables
    private String[] songNames;
    private final HashMap<UUID, Boolean> tunedPlayers = new HashMap<>();
    private final HashMap<Integer, Byte> channelPatches = new HashMap<>();
    //Strings
    private final String stationName;
    private final String STATION_FOLDER;
    private String currentSong;
    //Integers
    private int currentSongID, runnableTaskID = 0;
    //Booleans
    private boolean stationStarted, taskStarted, musicPlaying, musicPaused, randomSong = false;

    /**
     * Used for playing music to players in this station
     *
     * @param plugin the plugin that instantiated this object
     * @param stationName the music station's name
     * @param randomSong whether or not to play a random song after one finishes
     */
    public MusicStation(JavaPlugin plugin, String stationName, boolean randomSong) throws MidiUnavailableException
    {
        this.plugin = plugin;
        this.stationName = stationName;
        this.randomSong = randomSong;

        this.STATION_FOLDER = this.plugin.getDataFolder().getPath().replace(this.plugin.getName(), "") + "Stations" + File.separator + this.stationName;

        this.start();
        this.loadMidiFiles();
    }
    //--------------------------------------------------------------
    //                       MusicStation methods
    //--------------------------------------------------------------

    /**
     * Stops the station from playing music
     */
    public void stop()
    {
        StationStopEvent event = new StationStopEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.sequencer.stop();
        if (this.taskStarted) this.plugin.getServer().getScheduler().cancelTask(this.runnableTaskID);
        this.musicPlaying = false;
        this.musicPaused = false;
        this.taskStarted = false;
        this.runnableTaskID = 0;
        this.currentSongID = 0;
        this.channelPatches.clear();
    }

    /**
     * Pause/Unpauses the music
     */
    public void pause()
    {
        StationPauseEvent event = new StationPauseEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.musicPaused = !this.musicPaused;
        try
        {
            if (this.musicPaused)
            {
                this.sequencer.stop();
                return;
            }
            this.sequencer.start();
        }
        catch (IllegalStateException e)
        {
            //prevents weird error that occurs every now and then (I know I need to fix it) ;p
            return;
        }
    }

    /**
     * Prepares the station for playing music
     *
     * @throws javax.sound.midi.MidiUnavailableException
     */
    public void start() throws MidiUnavailableException
    {
        if (this.stationStarted) return;
        StationStartEvent event = new StationStartEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.sequencer = MidiSystem.getSequencer();
        this.sequencer.open();
        for (Transmitter transmitter : this.sequencer.getTransmitters()) transmitter.setReceiver(this); //screw server play back
        this.stationStarted = true;
    }

    /**
     * Begins playing music
     */
    public void play()
    {
        StationStartEvent event = new StationStartEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        if (!this.stationStarted) return;
        if (musicPlaying) this.stop();
        if (this.songNames.length == 0)
        {
            System.err.println("No songs found for station " + this.stationName + "!");
            return;
        }
        this.playNextSong();
    }

    /**
     * Play's the next song from the song list
     */
    public void playNextSong()
    {
        String nextSong = this.randomSong ? this.songNames[new Random().nextInt(this.songNames.length)] : getNextSongName();
        if(nextSong.equalsIgnoreCase(this.currentSong))
        {
            playNextSong();
            return;
        }
        StationPlayNextSongEvent event = new StationPlayNextSongEvent(this, nextSong);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.currentSong = nextSong;
        this.playSong();
    }
    private String getNextSongName()
    {
        try
        {
            String song = this.songNames[this.currentSongID];
            this.currentSongID++;
            return song;
        } catch (ArrayIndexOutOfBoundsException e)
        {
            this.currentSongID = 0;
            return this.getNextSongName();
        }
    }
    /**
     * Actually plays the song
     */
    public void playSong()
    {
        StationPlaySongEvent event = new StationPlaySongEvent(this, this.currentSong);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        File file = this.getMidiFile();
        if (file == null)
        {
            System.err.println("The midi file was NULL for " + this.currentSong + "!");
            return;
        }
        try
        {
            this.sequencer.setSequence(MidiSystem.getSequence(file));
            this.sequencer.start();
            this.musicPlaying = true;
        } catch (InvalidMidiDataException e)
        {
            System.err.println("Corrupt midi file: " + file.getName());
            return;
        } catch (IOException e)
        {
            System.err.println("Cannot read file: " + file.getName());
            return;
        }
        if (!this.musicPlaying) return;
        for (UUID u : this.tunedPlayers.keySet()) tuneIn(u);

        BukkitRunnable runnable = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (musicPaused) return;
                if (sequencer.isRunning() || sequencer.getMicrosecondPosition() < sequencer.getMicrosecondLength())
                    return;
                playNextSong();
            }
        };
        runnable.runTaskTimer(this.plugin, 20L, 20L);
        this.runnableTaskID = runnable.getTaskId();
        this.taskStarted = true;
    }

    /**
     * Tunes the player into this station
     *
     * @param uuid the player's UUID
     */
    public void tuneIn(UUID uuid)
    {
        if(!this.tunedPlayers.containsKey(uuid))
        {
            StationTuneInEvent event = new StationTuneInEvent(this, Bukkit.getPlayer(uuid));
            this.plugin.getServer().getPluginManager().callEvent(event);
            if(!event.isCancelled()) this.tunedPlayers.put(uuid, false);
        }
    }
    /**
     * Tunes the player out of this station
     *
     * @param uuid the player's uuid
     */
    public void tuneOut(UUID uuid)
    {
        if(this.tunedPlayers.containsKey(uuid))
        {
            StationTuneOutEvent event = new StationTuneOutEvent(this, Bukkit.getPlayer(uuid));
            this.plugin.getServer().getPluginManager().callEvent(event);
            if(!event.isCancelled()) this.tunedPlayers.remove(uuid);
        }
    }
    /**
     * Mutes the radio station
     *
     * @param uuid the player's uuid
     */
    public void mute(UUID uuid)
    {
        if (!this.tunedPlayers.containsKey(uuid)) return;
        StationMuteEvent event = new StationMuteEvent(this, Bukkit.getPlayer(uuid));
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        tunedPlayers.put(uuid, true);
    }

    /**
     * Unmutes the radio station
     *
     * @param uuid
     */
    public void unMute(UUID uuid)
    {
        if (!this.tunedPlayers.containsKey(uuid)) return;
        StationUnMuteEvent event = new StationUnMuteEvent(this, Bukkit.getPlayer(uuid));
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        tunedPlayers.put(uuid, false);
    }

    /**
     * Determines if the player has the station muted
     *
     * @param uuid the player's uuid
     * @return true if they have the station muted
     */
    public boolean isMuted(UUID uuid)
    {
        if (!this.tunedPlayers.containsKey(uuid)) return true;
        return this.tunedPlayers.get(uuid);
    }

    /**
     * Removes all tuned in players from this station
     */
    public void removeAllTunedPlayers()
    {
        this.tunedPlayers.clear();
    }
    //--------------------------------------------------------------
    //                       MidiFile methods
    //--------------------------------------------------------------

    /**
     * Loads all midi files from the station folder and saves their names in an array
     */
    public void loadMidiFiles()
    {
        File parent = new File(this.STATION_FOLDER);
        if (!parent.exists()) parent.mkdirs();
        File[] files = parent.listFiles();
        List<String> fileNames = new ArrayList<>();
        for (File f : files != null ? files : new File[0])
        {
            if (!f.getName().contains(".mid")) continue;
            fileNames.add(f.getName().substring(0, f.getName().lastIndexOf(".mid")));
        }
        this.songNames = fileNames.toArray(new String[fileNames.size()]);
    }

    /**
     * Get's the midi file instance of the current song
     *
     * @return the midi file instance
     */
    public File getMidiFile()
    {
        try
        {
            File file = new File(this.STATION_FOLDER, this.currentSong + ".mid");
            if (!file.exists()) return null;
            return file;
        } catch (Exception e)
        {
            return null;
        }
    }

    //--------------------------------------------------------------
    //                       Receiver methods
    //--------------------------------------------------------------
    @Override
    public void close()
    {
    }
    @Override
    public void send(MidiMessage message, long timeStamp)
    {
        if (this.musicPaused || this.tunedPlayers.size() == 0) return;
        if (!(message instanceof ShortMessage)) return;

        ShortMessage event = (ShortMessage) message;

        if (event.getCommand() == ShortMessage.NOTE_ON)
        {
            int midiNote = event.getData1();
            float volume = event.getData2() / 127;

            volume = volume == 0 ? 1 : volume;

            int note = (midiNote - 6) % 24;

            int channel = event.getChannel();
            byte patch = 1;
            if (this.channelPatches.containsKey(channel)) patch = this.channelPatches.get(channel);
            for (UUID uuid : this.tunedPlayers.keySet())
            {
                if (Bukkit.getPlayer(uuid) == null)
                {
                    this.tuneOut(uuid);
                    continue;
                }
                if (this.tunedPlayers.get(uuid)) continue;
                Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Instrument.getInstrument(patch, channel), volume, NotePitch.getPitch(note));
            }
        } else if (event.getCommand() == ShortMessage.PROGRAM_CHANGE)
        {
            this.channelPatches.put(event.getChannel(), (byte) event.getData1());
        } else if (event.getCommand() == ShortMessage.STOP)
        {
            this.playNextSong();
        }
    }
    //--------------------------------------------------------------
    //                           Getters
    //--------------------------------------------------------------
    /**
     * Returns the plugin instance
     *
     * @return the plugin instance
     */
    public JavaPlugin getPlugin()
    {
        return this.plugin;
    }

    /**
     * Returns the station's name
     *
     * @return the station's name
     */
    public String getStationName()
    {
        return this.stationName;
    }

    /**
     * Returns an array of song names in this station
     *
     * @return the array of song names
     */
    public String[] getSongNames()
    {
        return this.songNames;
    }
    /**
     * Get's the song that is currently playing
     * @return the song that is currently playing
     */
    public String getCurrentSong()
    {
        return this.currentSong;
    }
    /**
     * Get's the tuned in players as a uuid set
     *
     * @return the set of tuned in players
     */
    public Set<UUID> getTunedPlayers()
    {
        return this.tunedPlayers.keySet();
    }

    /**
     * Get's the station's task ID
     *
     * @return the task ID
     */
    public int getTaskID()
    {
        return this.runnableTaskID;
    }

    /**
     * Get's this station's music folder
     *
     * @return the station's music folder
     */
    public String getStationFolder()
    {
        return this.STATION_FOLDER;
    }

    /**
     * Determines if the music is paused
     *
     * @return true if it is paused
     */
    public boolean isMusicPaused()
    {
        return this.musicPaused;
    }

    /**
     * Determines if the music is playing
     *
     * @return true if the music is playing
     */
    public boolean isMusicPlaying()
    {
        return this.musicPlaying;
    }

    /**
     * Determines if the station has been started
     *
     * @return true if the station has been started
     */
    public boolean isStationStarted()
    {
        return this.stationStarted;
    }

    /**
     * Gets this station's sequencer instance
     *
     * @return the sequencer instance
     */
    public Sequencer getSequencer()
    {
        return this.sequencer;
    }
}