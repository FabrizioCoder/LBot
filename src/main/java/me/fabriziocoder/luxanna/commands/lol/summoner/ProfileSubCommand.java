package me.fabriziocoder.luxanna.commands.lol.summoner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.merakianalytics.orianna.types.common.Platform;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.championmastery.ChampionMasteries;
import com.merakianalytics.orianna.types.core.league.LeagueEntry;
import com.merakianalytics.orianna.types.core.league.LeaguePositions;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import me.fabriziocoder.luxanna.utils.MatchUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class ProfileSubCommand extends SlashCommand {

    public static final String COMMAND_NAME = "profile";
    public static final String COMMAND_DESCRIPTION = "Summoner profile with ranks, champions, last game, etc";

    private static final DecimalFormat oneDecimal = new DecimalFormat("0.0");

    public ProfileSubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 15;
        this.options = List.of(new OptionData(OptionType.STRING, "name", "The name of the summoner to search for").setRequired(true), new OptionData(OptionType.STRING, "region", "The region of the account").addChoices(regionChoices()).setRequired(true));


    }

    public static String capitalize(String str) {
        if (str == null || str.length() <= 1) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String humanReadableInt(long number) {
        long absNumber = Math.abs(number);
        double result;
        String suffix = "";
        if (absNumber < 1024) {
            result = number;
        } else if (absNumber < 1024 * 1024) {
            result = number / 1024.0;
            suffix = "K";
        } else if (absNumber < 1024 * 1024 * 1024) {
            result = number / (1024.0 * 1024);
            suffix = "M";
        } else {
            result = number / (1024.0 * 1024 * 1024);
            suffix = "";
        }
        return oneDecimal.format(result) + suffix;
    }

    private List<Command.Choice> regionChoices() {
        List<Command.Choice> options = new ArrayList<>();
        for (Platform c : Platform.values()) {
            options.add(new Command.Choice(c.getTag(), c.name()));
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

        String summonerName = event.optString("name");
        String region = event.optString("region");

        assert summonerName != null;
        final Summoner summoner = Summoner.named(summonerName).withRegion(Region.valueOf(region)).get();

        if (!summoner.exists()) {
            event.getHook().editOriginal("That summoner couldn't be found, at least on that region.").queue();
            return;
        }

        final ChampionMasteries championMasteries = summoner.getChampionMasteries();
        final LeaguePositions rankedEntries = summoner.getLeaguePositions();
        final MatchParticipant lastMatch = MatchUtils.getLastMatch(summoner.getName(), LeagueShard.valueOf(Platform.valueOf(region).getTag()));

        String[] basicInformation = {String.format("`Name:` %s", summoner.getName()), String.format("`Level:` %s", summoner.getLevel()), String.format("`Platform:` %s", capitalize(summoner.getRegion().name().replace("_", " ").toLowerCase())), String.format("`Icon URL:` [View here](%s)", summoner.getProfileIcon().getImage().getURL()),};

        final EmbedBuilder messageEmbed = new EmbedBuilder().setColor(0x2564f4).setTitle(String.format("%s Profile", event.getJDA().getSelfUser().getName())).setThumbnail(summoner.getProfileIcon().getImage().getURL()).addField("> Basic Information", String.join("\n", basicInformation), true);

        if (!championMasteries.exists()) {
            messageEmbed.addField("> Top 3 Champions", "This summoner has not played champions", true);
        } else {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                str.append(String.format("`%s:` %s %s (Level %s, **%s**)\n", i + 1, EmojiUtils.getChampionEmojiByChampionName(championMasteries.get(i).getChampion().getName()), championMasteries.get(i).getChampion().getName(), championMasteries.get(i).getLevel(), humanReadableInt(championMasteries.get(i).getPoints())));
            }
            messageEmbed.addField("> Top 3 Champions", String.join("\n", str), true);
        }

        if (!rankedEntries.exists()) {
            messageEmbed.addField("> Ranked Stats", "This summoner has not played ranked games", false);
        } else {
            String textSoloQ = "*Unranked*";
            String textFlex = "*Unranked*";
            String textTFT = "*Unranked*";

            for (final LeagueEntry entry : rankedEntries) {

                switch (entry.getQueue()) {
                    case RANKED_SOLO ->
                            textSoloQ = String.format("%s %s %s (**%s LP**) (**%s W** / **%s L**, %s", EmojiUtils.getRankEmojiByRankName(entry.getTier().toString()), capitalize(entry.getTier().toString().toLowerCase()), entry.getDivision(), entry.getLeaguePoints(), entry.getWins(), entry.getLosses(), Math.round((entry.getWins() * 100d) / (entry.getWins() + entry.getLosses())) + "%)");
                    case RANKED_FLEX ->
                            textFlex = String.format("%s %s %s (**%s LP**) (**%s W** / **%s L**, %s", EmojiUtils.getRankEmojiByRankName(entry.getTier().toString()), capitalize(entry.getTier().toString().toLowerCase()), entry.getDivision(), entry.getLeaguePoints(), entry.getWins(), entry.getLosses(), Math.round((entry.getWins() * 100d) / (entry.getWins() + entry.getLosses())) + "%)");
                    case RANKED_TFT ->
                            textTFT = String.format("%s %s %s (**%s LP**) (**%s W** / **%s L**, %s", EmojiUtils.getRankEmojiByRankName(entry.getTier().toString()), capitalize(entry.getTier().toString().toLowerCase()), entry.getDivision(), entry.getLeaguePoints(), entry.getWins(), entry.getLosses(), Math.round((entry.getWins() * 100d) / (entry.getWins() + entry.getLosses())) + "%)");
                }
            }

            String[] rankedStats = {String.format("`Solo/Duo:` %s", textSoloQ), String.format("`Flex:` %s", textFlex), String.format("`TFT:` %s", textTFT)};
            messageEmbed.addField("> Ranked Stats", String.join("\n", rankedStats), false);
        }

        if (lastMatch == null) {
            messageEmbed.addField("> Last Game", "This summoner has not played any games", false);
        } else {
            messageEmbed.addField("> Last Match", String.join("\n", String.format("%s with **%s** %s, **%s**/**%s**/**%s** **%s CS**", lastMatch.didWin() ? ":white_check_mark: **Victory**" : ":x: **Defeat**", lastMatch.getChampionName(), EmojiUtils.getChampionEmojiByChampionName(lastMatch.getChampionName()), lastMatch.getKills(), lastMatch.getDeaths(), lastMatch.getAssists(), (lastMatch.getTotalMinionsKilled() + lastMatch.getNeutralMinionsKilled()))), false);
        }

        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();

    }


}
