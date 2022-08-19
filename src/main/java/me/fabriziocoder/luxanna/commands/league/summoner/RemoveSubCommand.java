package me.fabriziocoder.luxanna.commands.league.summoner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.database.MongoDB;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import org.bson.Document;

public class RemoveSubCommand extends SlashCommand {
    public static final String COMMAND_NAME = "remove";
    public static final String COMMAND_DESCRIPTION = "Remove a summoner from the database, so it won't be automatically looked up";

    public RemoveSubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 15;
    }

    @Override
    public String getHelp() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        Document existUserProfile = MongoDB.userProfileExists(event.getUser().getIdLong()).first();
        if (existUserProfile == null) {
            event.getHook().editOriginal(String.format("%s You don't have a summoner in the database, use `/summoner add` to add it.", EmojiUtils.Discord.X)).queue();
            return;
        }
        MongoDB.removeUserProfile(event.getUser().getIdLong());
        event.getHook().editOriginal(String.format("%s Your account has been removed from the database.", EmojiUtils.Discord.CHECK)).queue();
    }
}
