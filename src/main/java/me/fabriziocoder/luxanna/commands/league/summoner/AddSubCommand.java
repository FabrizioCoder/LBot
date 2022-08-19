package me.fabriziocoder.luxanna.commands.league.summoner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.database.MongoDB;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import me.fabriziocoder.luxanna.utils.SummonerUtils;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AddSubCommand extends SlashCommand {
    public static final String COMMAND_NAME = "add";
    public static final String COMMAND_DESCRIPTION = "Add a summoner to the database for automatic lookup";

    public AddSubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 15;
        this.options = List.of(new OptionData(OptionType.STRING, "summoner-name", "The name of the summoner to search for").setRequired(true), new OptionData(OptionType.STRING, "region", "The region of the account").addChoices(regionChoices()).setRequired(true));
    }

    private List<Command.Choice> regionChoices() {
        List<Command.Choice> options = new ArrayList<>();
        LeagueShard[] leagueShards = LeagueShard.values();
        for (int i = 1; i < leagueShards.length; i++) {
            if (i >= 12) break;
            LeagueShard leagueShard = leagueShards[i];
            String keyName = leagueShard.getKeys()[1];
            if (keyName.isEmpty()) continue;
            options.add(new Command.Choice(keyName.toUpperCase(), leagueShard.name()));
        }
        return options;
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

        String summonerName = event.optString("summoner-name");
        String region = event.optString("region");

        final Summoner summonerData = SummonerUtils.getSummonerByName(summonerName, LeagueShard.valueOf(region));

        if (summonerData == null) {
            event.getHook().editOriginal(String.format("%s That summoner couldn't be found, at least on that region.", EmojiUtils.Discord.X)).queue();
            return;
        }

        Document existUserProfile = MongoDB.userProfileExists(event.getUser().getIdLong()).first();
        if (existUserProfile == null) {
            MongoDB.addUserProfile(event.getUser().getIdLong(), summonerData.getName(), region);
            event.getHook().editOriginal(String.format("%s The summoner (`%s`, `%s`) added to the database.", EmojiUtils.Discord.CHECK, summonerData.getName(), region)).queue();
        } else {
            event.getHook().editOriginal(String.format("%s You already have a summoner in the database, use `/summoner remove` to update it.", EmojiUtils.Discord.X)).queue();
        }

    }
}

