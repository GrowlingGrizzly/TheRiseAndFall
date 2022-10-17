package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.events.DiscordEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class DiscordIntegration {

    static JDA jda = null;
    static TextChannel chatChannel;

    public void startup() {
        String botToken = Configs.configs.getConfig().getString("Discord-Integration.Bot-Token");

        if (botToken != null)
            try {
                jda = JDABuilder.createDefault(botToken).addEventListeners(new DiscordEvents()).build();
                chatChannel = jda.getTextChannelsByName(Configs.configs.getConfig().getString("Discord-Integration.Channel-Name"), true).get(0);
            } catch (LoginException e) {
                Plugin.log.severe(Plugin.prefix + "§cAn error occured when connecting to discord. Please check the config and reboot the server.");
            }
    }

    public void shutDown() {
        if (jda != null) jda.shutdownNow();
    }

    public TextChannel getChatChannel() {
        return chatChannel;
    }

    public void onCommand(String cmd, String[] args) {
        if (jda != null) {
            if (cmd.equalsIgnoreCase("list")) {
                if (Bukkit.getOnlinePlayers().toArray().length == 0) Plugin.sendDiscordMessage("No players are currently online.");
                else {
                    StringBuilder sb = new StringBuilder();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        sb.append(player.getName());
                        sb.append("\n");
                    } sb.delete(sb.length() - 2, sb.length() - 1);
                    Plugin.sendDiscordMessage("Online Players", sb.toString(), Color.GREEN);
                }
            }
        }
    }

    public boolean isValidCommand(String cmd) {
        ArrayList<String> cmds = new ArrayList<>(Collections.singletonList("list"));
        return cmds.contains(cmd.toLowerCase());
    }

}
