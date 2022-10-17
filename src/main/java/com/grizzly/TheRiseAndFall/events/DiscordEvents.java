package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.util.Configs;
import com.grizzly.TheRiseAndFall.util.DiscordIntegration;
import com.grizzly.TheRiseAndFall.util.Plugin;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordEvents extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        Plugin.log.info("message detected, " + e.getChannel().getName());
        if (!e.getChannel().getName().equals(Configs.configs.getConfig().getString("Discord-Integration.Channel-Name"))) return;
        Plugin.log.info("message in channel");
        Member member = e.getMember();
        if (member == null || member.getUser().isBot()) return;
        Plugin.log.info("not null/bot");

        String msg = e.getMessage().getContentDisplay();

        e.getChannel().sendMessage(new MessageBuilder().setContent("test1").build()).queue();
        new DiscordIntegration().getChatChannel().sendMessage(new MessageBuilder().setContent("test2").build()).queue();

        Plugin.log.info(msg);

        if (msg.startsWith("!") && new DiscordIntegration().isValidCommand(msg.split(" ")[0].replaceFirst("!", ""))) {
            msg = msg.replaceFirst("!", "");
            new DiscordIntegration().onCommand(msg.split(" ")[0].toLowerCase(),
                    msg.replaceAll(msg.split(" ")[0], "").split(" "));
        } else e.getMessage().delete().queue();

    }

}
