package com.covertlizard.api.radio.sound;

import com.covertlizard.api.radio.event.midi.MidiPauseEvent;
import com.covertlizard.api.radio.event.midi.MidiPlayEvent;
import com.covertlizard.api.radio.event.midi.MidiStartEvent;
import com.covertlizard.api.radio.event.midi.MidiStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by CovertLizard on 5/3/2015.
 * Project RadioAPI
 */
@SuppressWarnings("all") //screw you IDE ;-)
public class MidiSound implements Receiver
{
    //Init vars
    private final JavaPlugin plugin;
    private final File midiFile;
    private final UUID uuid;
    private final String soundName;
    //Midi vars
    private Sequencer sequencer;
    private final HashMap<Integer, Byte> channelPatches = new HashMap<>();
    //Booleans
    private boolean loop, musicStarted, musicPlaying, musicPaused, taskStarted = false;
    //Integer
    private int taskID;
    /**
     * Used for playing a midifile to a player
     * @param plugin the plugin that instantiated this object
     * @param midiFile the midifile to play
     * @param uuid the player's uuid
     * @param loop whether or not to loop the midifile
     */
    public MidiSound(JavaPlugin plugin, File midiFile, UUID uuid, boolean loop) throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        this.plugin = plugin;
        this.midiFile = midiFile;
        this.uuid = uuid;
        this.soundName = this.midiFile.getName().substring(0, this.midiFile.getName().lastIndexOf(".mid"));
        this.loop = loop;
        this.start();
    }
    /**
     * Prepares the music for playing
     * @throws MidiUnavailableException
     */
    public void start() throws MidiUnavailableException
    {
        if(this.musicStarted) return;
        MidiStartEvent event = new MidiStartEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.sequencer = MidiSystem.getSequencer();
        this.sequencer.open();
        for (Transmitter transmitter : this.sequencer.getTransmitters()) transmitter.setReceiver(this); //screw server play back
        this.musicStarted = true;
    }
    /**
     * Plays the music
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public void play() throws InvalidMidiDataException, IOException
    {
        MidiPlayEvent event = new MidiPlayEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.sequencer.setSequence(MidiSystem.getSequence(this.midiFile));
        this.sequencer.start();
        this.musicPlaying = true;
        BukkitRunnable runnable = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (musicPaused) return;
                if (sequencer.isRunning() || sequencer.getMicrosecondPosition() < sequencer.getMicrosecondLength())
                    return;
                if(loop)
                {
                   loop();
                    return;
                }
                stop();
            }
        };
        runnable.runTaskTimer(this.plugin, 20L, 20L);
        this.taskID = runnable.getTaskId();
        this.taskStarted = true;
    }
    /**
     * Stops the music
     */
    public void stop()
    {
        MidiStopEvent event = new MidiStopEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.sequencer.stop();
        if (this.taskStarted) this.plugin.getServer().getScheduler().cancelTask(this.taskID);
        this.musicPaused = false;
        this.musicPlaying = false;
        this.musicStarted = false;
        this.taskID = 0;
        this.channelPatches.clear();
    }
    /**
     * Pauses/Unpauses the music
     */
    public void pause()
    {
        if(!this.musicPlaying) return;
        MidiPauseEvent event = new MidiPauseEvent(this);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        this.musicPaused = !this.musicPaused;
    }
    /**
     * Loops the music
     */
    public void loop()
    {
        try
        {
            this.stop();
            this.play();
        } catch (InvalidMidiDataException | IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void send(MidiMessage message, long timeStamp)
    {
        if (this.musicPaused) return;
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
            Bukkit.getPlayer(this.uuid).playSound(Bukkit.getPlayer(this.uuid).getLocation(), Instrument.getInstrument(patch, channel), volume, NotePitch.getPitch(note));
        }
        else if (event.getCommand() == ShortMessage.PROGRAM_CHANGE)
        {
            this.channelPatches.put(event.getChannel(), (byte) event.getData1());
        }
        else if (event.getCommand() == ShortMessage.STOP)
        {
            if(this.loop)
            {
                this.loop();
                return;
            }
            this.stop();
        }
    }
    @Override
    public void close(){} //not needed
    //--------------------------------------
    //              Getters
    //--------------------------------------
    public JavaPlugin getPlugin()
    {
        return this.plugin;
    }
    public File getMidiFile()
    {
        return this.midiFile;
    }
    public UUID getUUID()
    {
        return this.uuid;
    }
    public String getSoundName()
    {
        return this.soundName;
    }
    public boolean isLoop()
    {
        return this.loop;
    }
    public boolean hasMusicStarted()
    {
        return this.musicStarted;
    }
    public boolean isMusicPlaying()
    {
        return this.musicPlaying;
    }
    public boolean isMusicPaused()
    {
        return this.musicPaused;
    }
    public boolean isTaskStarted()
    {
        return this.taskStarted;
    }
    public int getTaskID()
    {
        return this.taskID;
    }
}