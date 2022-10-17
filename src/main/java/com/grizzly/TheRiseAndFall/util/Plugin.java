package com.grizzly.TheRiseAndFall.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.logging.Logger;

public class Plugin {

    public static String prefix = "§7[§bTheRiseAndFall§7] ";

    public static boolean playerOnly(CommandSender sender) {
        sender.sendMessage(prefix + "§cThis command can only be used by a player.");
        return true;
    }

    public static boolean noPermission(CommandSender sender) {
        sender.sendMessage(prefix + "§cYou do not have permission to execute this command.");
        return true;
    }

    public static boolean playerOffline(CommandSender sender) {
        sender.sendMessage(prefix + "§cThat player is not online!");
        return true;
    }

    public static void sendDiscordMessage(String content) {
        if (new DiscordIntegration().getChatChannel() == null) return;
        MessageBuilder builder = new MessageBuilder().setContent(content);
        new DiscordIntegration().getChatChannel().sendMessage(builder.build()).queue();
    }

    public static void sendDiscordMessage(String title, String content, Color color) {
        if (new DiscordIntegration().getChatChannel() == null) return;
        EmbedBuilder builder = new EmbedBuilder().setColor(color);
        builder.setAuthor(title != null ? title : content,
                null, null);
        if (title != null) builder.setDescription(content);
        new DiscordIntegration().getChatChannel().sendMessage(builder.build()).queue();
    }

    public static void sendInteractiveMessage(CommandSender sender, String msg, String value, String hoverText) {
        TextComponent message = new TextComponent(msg);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        sender.spigot().sendMessage(message);
        sender.sendMessage();
    }

    public static void sendInteractiveMessage(CommandSender sender, String msg, String value) {
        TextComponent message = new TextComponent(msg);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));
        sender.spigot().sendMessage(message);
        sender.sendMessage();
    }

    public static final Logger log = Logger.getLogger("Minecraft");

    public static String firstLetterCapital(String string, boolean eachWord) {
        String beginning = string.toUpperCase().substring(0, 1);
        if (!eachWord) return beginning + string.toLowerCase().substring(1);

        String[] words = string.replaceAll(" ", "_").split("_");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            beginning = word.toUpperCase().substring(0, 1);
            String end = word.toLowerCase().substring(1);
            sb.append(beginning).append(end);
            if (i != words.length - 1) sb.append(" ");
        } return sb.toString();
    }

}
