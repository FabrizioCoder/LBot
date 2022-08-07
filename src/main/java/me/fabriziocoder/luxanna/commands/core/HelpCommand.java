package me.fabriziocoder.luxanna.commands.core;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;

public class HelpCommand extends SlashCommand {

    public static final String COMMAND_NAME = "help";
    public static final String COMMAND_DESCRIPTION = "Shows help options for all commands";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHelp() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        StringBuilder leagueCommands = new StringBuilder();
        StringBuilder coreCommands = new StringBuilder();
        event.deferReply().queue();
        event.getClient().getSlashCommands().forEach(command -> {
            if (command.getName().equals("lol")) {
                Arrays.stream(command.getChildren()).map(SlashCommand::getName).forEach((it) -> leagueCommands.append("`").append(it).append("` "));
            } else {
                coreCommands.append("`").append(command.getName()).append("` ");
            }
        });

        MessageEmbed messageEmbed = new EmbedBuilder().setColor(0x2564f4).setTitle(String.format("%s — Commands", event.getJDA().getSelfUser().getName())).setDescription(String.format("Hi! I'm %s, the perfect way to play your games.", event.getJDA().getSelfUser().getName())).addField("» League of Legends", String.join("", leagueCommands), false).addField("» Core", String.join("", coreCommands), false).setThumbnail(event.getJDA().getSelfUser().getAvatarUrl()).setImage("https://i.imgur.com/ntMRhP0.png").build();

        event.getHook().sendMessageEmbeds(messageEmbed).queue();
    }
}
