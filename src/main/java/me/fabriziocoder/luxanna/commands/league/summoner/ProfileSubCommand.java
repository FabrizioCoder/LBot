package me.fabriziocoder.luxanna.commands.league.summoner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.luxanna.utils.ChampionUtils;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import me.fabriziocoder.luxanna.utils.MatchUtils;
import me.fabriziocoder.luxanna.utils.SummonerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.league.LeagueEntry;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

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
        this.options = List.of(new OptionData(OptionType.STRING, "summoner-name", "The name of the summoner to search for").setRequired(true), new OptionData(OptionType.STRING, "region", "The region of the account").addChoices(regionChoices()).setRequired(true));
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
            suffix = "B";
        }
        return oneDecimal.format(result) + suffix;
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
            event.getHook().editOriginal("[\\❌] That summoner couldn't be found, at least on that region.").queue();
            return;
        }


        final List<ChampionMastery> summonerTopChampions = SummonerUtils.getSummonerTopChampionsSummonerId(summonerData.getSummonerId(), LeagueShard.valueOf(region), 3);
        final List<LeagueEntry> summonerLeagueEntries = SummonerUtils.getSummonerLeagueEntryBySummonerId(summonerData.getSummonerId(), LeagueShard.valueOf(region));
        final List<MatchParticipant> summonerRecentThreeMatches = MatchUtils.getSummonerThreeRecentGames(summonerData);
        final MatchParticipant summonerLastMatch = MatchUtils.getSummonerLastMatchBySummoner(summonerData);


        // Make embedBuilder
        final EmbedBuilder messageEmbed = new EmbedBuilder().setColor(0x2564f4).setThumbnail(SummonerUtils.makeProfileIconURL(String.valueOf(summonerData.getProfileIconId()))).setTitle(String.format("%s Profile", event.getJDA().getSelfUser().getName()));

        String[] summonerBasicInformation = {String.format("`Name:` %s", summonerData.getName()), String.format("`Level:` %s", summonerData.getSummonerLevel()), String.format("`Platform:` %s", summonerData.getPlatform().prettyName()), String.format("`Icon URL:` [View here](%s)", SummonerUtils.makeProfileIconURL(String.valueOf(summonerData.getProfileIconId())))};
        messageEmbed.addField("> Basic Information", String.join("\n", summonerBasicInformation), true);

        // Top Mastery Champions for the Summoner
        if (summonerTopChampions == null) {
            messageEmbed.addField("> Top 3 Champions", "This summoner has not played champions", true);
        } else {
            StringBuilder summonerTopChampionsText = new StringBuilder();
            for (int i = 0; i < summonerTopChampions.size(); i++) {
                ChampionMastery championMastery = summonerTopChampions.get(i);
                String championName = ChampionUtils.getChampionNameById(championMastery.getChampionId());
                summonerTopChampionsText.append(String.format("`%s.` %s %s (Level %s, **%s**)\n", i + 1, EmojiUtils.getChampionEmojiByChampionName(championName), championName, championMastery.getChampionLevel(), humanReadableInt(championMastery.getChampionPoints())));
            }
            messageEmbed.addField(String.format("> Top %s Champions", summonerTopChampions.size()), summonerTopChampionsText.toString(), true);
        }

        // Ranked stats for the Summoner
        if (summonerLeagueEntries == null) {
            messageEmbed.addField("> Ranked Stats", "This summoner has not played ranked games", false);
        } else {
            String soloQText = "*Unranked*";
            String flexSRText = "*Unranked*";
            String tftText = "*Unranked*";

            for (final LeagueEntry entry : summonerLeagueEntries) {
                GameQueueType queue = entry.getQueueType();

                switch (queue) {
                    case RANKED_SOLO_5X5 ->
                            soloQText = String.format("%s %s %s (**%s LP**) (**%s W** / **%s L**, %s", EmojiUtils.getRankEmojiByRankName(entry.getTier()), capitalize(entry.getTier().toLowerCase()), entry.getTierDivisionType().getDivision(), entry.getLeaguePoints(), entry.getWins(), entry.getLosses(), Math.round((entry.getWins() * 100d) / (entry.getWins() + entry.getLosses())) + "%)");
                    case RANKED_FLEX_SR ->
                            flexSRText = String.format("%s %s %s (**%s LP**) (**%s W** / **%s L**, %s", EmojiUtils.getRankEmojiByRankName(entry.getTier()), capitalize(entry.getTier().toLowerCase()), entry.getTierDivisionType().getDivision(), entry.getLeaguePoints(), entry.getWins(), entry.getLosses(), Math.round((entry.getWins() * 100d) / (entry.getWins() + entry.getLosses())) + "%)");
                    case TEAMFIGHT_TACTICS_RANKED ->
                            tftText = String.format("%s %s %s (**%s LP**) (**%s W** / **%s L**, %s", EmojiUtils.getRankEmojiByRankName(entry.getTier()), capitalize(entry.getTier().toLowerCase()), entry.getTierDivisionType().getDivision(), entry.getLeaguePoints(), entry.getWins(), entry.getLosses(), Math.round((entry.getWins() * 100d) / (entry.getWins() + entry.getLosses())) + "%)");
                }
            }
            String[] summonerRankedStatsText = {String.format("`Solo/Duo:` %s", soloQText), String.format("`Flex SR:` %s", flexSRText), String.format("`TFT:` %s", tftText)};
            messageEmbed.addField("> Ranked Stats", String.join("\n", summonerRankedStatsText), false);
        }

        // Recent Matches for the Summoner
        if (summonerRecentThreeMatches == null) {
            messageEmbed.addField("> Recent Matches", "This summoner has not played any matches", false);
        } else {
            StringBuilder summonerRecentMatchesText = new StringBuilder();
            for (int i = 0; i < summonerRecentThreeMatches.size(); i++) {
                MatchParticipant matchParticipant = summonerRecentThreeMatches.get(i);
                summonerRecentMatchesText.append(String.format("`%s.` %s %s %s\n", i + 1, EmojiUtils.getChampionEmojiByChampionName(matchParticipant.getChampionName()), matchParticipant.didWin() ? ":white_check_mark:" : ":x:", matchParticipant.getChampionName()));
            }
            messageEmbed.addField(String.format("> Recent %s Matches", summonerRecentThreeMatches.size()), summonerRecentMatchesText.toString(), false);
        }

        // Last Match for the Summoner
        if (summonerLastMatch == null) {
            messageEmbed.addField("> Last Match", "This summoner has not played any matches", false);
        } else {
            messageEmbed.addField("> Last Match", String.join("\n", String.format("%s with **%s** %s, **%s**/**%s**/**%s** **%s CS**", summonerLastMatch.didWin() ? ":white_check_mark: **Victory**" : ":x: **Defeat**", summonerLastMatch.getChampionName(), EmojiUtils.getChampionEmojiByChampionName(summonerLastMatch.getChampionName()), summonerLastMatch.getKills(), summonerLastMatch.getDeaths(), summonerLastMatch.getAssists(), (summonerLastMatch.getTotalMinionsKilled() + summonerLastMatch.getNeutralMinionsKilled()))), false);
        }


        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();

    }
}