package me.fabriziocoder.luxanna.commands.league.summoner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.luxanna.utils.ChampionUtils;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import me.fabriziocoder.luxanna.utils.SummonerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.types.lol.TeamType;
import no.stelar7.api.r4j.impl.lol.builders.matchv5.match.MatchBuilder;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchTeam;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

import java.util.ArrayList;
import java.util.List;

public class LastMatchSubCommand extends SlashCommand {

    public static final String COMMAND_NAME = "lastmatch";
    public static final String COMMAND_DESCRIPTION = "Get the last match of a summoner";

    public LastMatchSubCommand() {
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

        String matchId = summonerData.getLeagueGames().getLazy().get(0);
        if (matchId == null) {
            event.getHook().editOriginal(String.format("%s That summoner hasn't played any games yet.", EmojiUtils.Discord.X)).queue();
            return;
        }

        LOLMatch summonerLastMatch = new MatchBuilder(summonerData.getPlatform()).withId(matchId).getMatch();

        EmbedBuilder messageEmbed = new EmbedBuilder().setAuthor("Last match").setThumbnail(SummonerUtils.makeProfileIconURL(String.valueOf(summonerData.getProfileIconId())));

        String[] matchInformation = {String.format("`Map:` %s", summonerLastMatch.getMap().prettyName()), String.format("`Queue type:` %s", summonerLastMatch.getQueue().prettyName()), String.format("`Game start:` <t:%s:R>", summonerLastMatch.getGameStartTimestamp() / 1000), String.format("`Game end:` <t:%s:R>", summonerLastMatch.getGameEndTimestamp() / 1000)};
        messageEmbed.addField("> Match information", String.join("\n", matchInformation), false);

        StringBuilder blueTeamBans = new StringBuilder();
        StringBuilder redTeamBans = new StringBuilder();

        List<MatchTeam> matchTeams = summonerLastMatch.getTeams();
        for (MatchTeam matchTeam : matchTeams) {
            if (matchTeam.getBans().isEmpty()) break;
            for (int i = 0; i < matchTeam.getBans().size(); i++) {
                int teamBansSize = matchTeam.getBans().size();
                if (teamBansSize > 0) {
                    if (!(matchTeam.getBans().get(i).getChampionId() == -1)) {
                        String championName = ChampionUtils.getChampionNameById(matchTeam.getBans().get(i).getChampionId());
                        switch (matchTeam.getTeamId()) {
                            case BLUE ->
                                    blueTeamBans.append(String.format("%s %s ", EmojiUtils.getChampionEmojiByChampionName(championName), championName));
                            case RED ->
                                    redTeamBans.append(String.format("%s %s ", EmojiUtils.getChampionEmojiByChampionName(championName), championName));
                        }
                    }
                } else {
                    break;
                }
            }
        }

        if (!blueTeamBans.isEmpty() && !redTeamBans.isEmpty()) {
            String[] bannedChampions = {String.format("`Blue team:` %s", String.join("", blueTeamBans)), String.format("`Red team:` %s", String.join(", ", redTeamBans)),};
            messageEmbed.addField("> Banned champions", String.join("\n", bannedChampions), false);
        }

        StringBuilder blueTeamParticipants = new StringBuilder();
        StringBuilder redTeamParticipants = new StringBuilder();

        boolean didWin = false;
        for (int i = 0; i < summonerLastMatch.getParticipants().size(); i++) {
            TeamType teamType = summonerLastMatch.getParticipants().get(i).getTeam();
            String championName = ChampionUtils.getChampionNameById(summonerLastMatch.getParticipants().get(i).getChampionId());
            MatchParticipant participant = summonerLastMatch.getParticipants().get(i);

            if (participant.getSummonerId().equals(summonerData.getSummonerId())) {
                didWin = participant.didWin();
            }

            int summonerKills = participant.getKills();
            int summonerDeaths = participant.getDeaths();
            int summonerAssists = participant.getAssists();
            int totalCS = participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled();
            String formattedTotalSummonerStats = String.format("(**%s**/**%s**/**%s**, **%s CS**)", summonerKills, summonerDeaths, summonerAssists, totalCS);
            boolean isThisSummoner = summonerLastMatch.getParticipants().get(i).getSummonerId().equals(summonerData.getSummonerId());
            switch (teamType) {
                case BLUE ->
                        blueTeamParticipants.append(String.format("%s | %s%s%s %s\n", EmojiUtils.getChampionEmojiByChampionName(championName), isThisSummoner ? "__**" : "", championName, isThisSummoner ? "**__" : "", formattedTotalSummonerStats));
                case RED ->
                        redTeamParticipants.append(String.format("%s | %s%s%s %s\n", EmojiUtils.getChampionEmojiByChampionName(championName), isThisSummoner ? "__**" : "", championName, isThisSummoner ? "**__" : "", formattedTotalSummonerStats));
            }
        }

        messageEmbed.setTitle(String.format("`%s` - `%s` - `%s`", summonerData.getName(), summonerData.getPlatform().prettyName(), didWin ? "Victory" : "Defeat")).addField("> Blue team", String.join("", blueTeamParticipants), true).addField("> Red team", String.join("", redTeamParticipants), true).setColor(didWin ? 0x2564f4 : 0xff2424);

        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();


    }
}
