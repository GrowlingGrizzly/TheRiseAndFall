package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.Main;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Songs {

    int i2 = 0;
    int inst = 0;

    public void play(Player player, String name) {
        if (name.equals("Lose") || name.equals("Tetris")) parseSong(player, Configs.configs.getSongs().getConfigurationSection(name));
    }


    void parseSong(Player player, ConfigurationSection song) {
        List<Instrument> instruments = new ArrayList<>();
        List<List<String>> notes = new ArrayList<>();
        for (String instrument : song.getString("Instruments").split(";")) {
            switch (instrument) {
                case "Bit":  instruments.add(Instrument.BIT); break;
                case "Bass": instruments.add(Instrument.BASS_GUITAR); break;
                case "DoubleBass": instruments.add(Instrument.BASS_DRUM); break;
                case "Snare": instruments.add(Instrument.SNARE_DRUM); break;
                case "Xylophone": instruments.add(Instrument.XYLOPHONE); break;
                default: instruments.add(Instrument.PIANO); break;
            }
        } for (String note : song.getStringList("Lines")) notes.add(List.of(note.split(";")));

        i2 = 0;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (i2 == notes.size()) {
                    cancel();
                    return;
                } inst = 0;
                for (String note : notes.get(i2)) {
                    char[] parts = note.toCharArray();
                    Note.Tone tone;
                    switch (parts[0]) {
                        case 'B': tone = Note.Tone.B; break;
                        case 'C': tone = Note.Tone.C; break;
                        case 'D': tone = Note.Tone.D; break;
                        case 'E': tone = Note.Tone.E; break;
                        case 'F': tone = Note.Tone.F; break;
                        case 'G': tone = Note.Tone.G; break;
                        default: tone = Note.Tone.A; break;
                    } if (parts[0] != '_') {
                        int octave = Integer.parseInt(String.valueOf(parts[parts.length - 1]));
                        if (parts[1] == '#') player.playNote(getLocation(player), instruments.get(inst), Note.sharp(octave, tone));
                        else player.playNote(getLocation(player), instruments.get(inst), Note.natural(octave, tone));
                    } inst++;
                } i2++;
            }
        }.runTaskTimer(Main.plugin, 0, 2);
    }

    Location getLocation(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection();
        direction.multiply(5);
        return origin.clone().add(direction);
    }

}
