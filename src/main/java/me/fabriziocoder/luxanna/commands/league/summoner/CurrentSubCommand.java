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
import no.stelar7.api.r4j.pojo.lol.spectator.SpectatorGameInfo;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CurrentSubCommand extends SlashCommand {

    public static final String COMMAND_NAME = "current";
    public static final String COMMAND_DESCRIPTION = "Get the current game of a summoner";

    public CurrentSubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 30;
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
            event.getHook().editOriginal("[\\❌] That summoner couldn't be found, at least on that region.").queue();
            return;
        }

        SpectatorGameInfo summonerCurrentGame = summonerData.getCurrentGame();
        if (summonerCurrentGame == null) {
            event.getHook().editOriginal("[\\❌] That summoner is not currently in a game.").queue();
            return;
        }

        EmbedBuilder messageEmbed = new EmbedBuilder().setColor(0x2564f4).setThumbnail(SummonerUtils.makeProfileIconURL(String.valueOf(summonerData.getProfileIconId()))).setAuthor("Current match").setTitle(String.format("`%s` - `%s`", summonerData.getName(), summonerData.getPlatform().prettyName()));

        String[] gameInformation = {String.format("`Map:` %s", summonerCurrentGame.getMap().prettyName()), String.format("`Game mode:` %s", summonerCurrentGame.getGameMode().prettyName()), String.format("`Game type:` %s", summonerCurrentGame.getGameType().prettyName()), String.format("`Game start:` %s", DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm:ss").format(summonerCurrentGame.getGameStartAsDate())),};
        messageEmbed.addField("> Match infomation", String.join("\n", gameInformation), false);

        StringBuilder bannedChampionsByBlueTeam = new StringBuilder();
        StringBuilder bannedChampionsByRedTeam = new StringBuilder();

        for (int i = 0; i < summonerCurrentGame.getBannedChampions().size(); i++) {
            int teamId = summonerCurrentGame.getBannedChampions().get(i).getTeamId();
            String championName = ChampionUtils.getChampionNameById(summonerCurrentGame.getBannedChampions().get(i).getChampionId());
            switch (teamId) {
                case 100 ->
                        bannedChampionsByBlueTeam.append(String.format("%s %s ", EmojiUtils.getChampionEmojiByChampionName(championName), championName));
                case 200 ->
                        bannedChampionsByRedTeam.append(String.format("%s %s ", EmojiUtils.getChampionEmojiByChampionName(championName), championName));
            }
        }

        String[] bannedChampions = {String.format("`Blue team:` %s", String.join("", bannedChampionsByBlueTeam)), String.format("`Red team:` %s", String.join(", ", bannedChampionsByRedTeam)),};

        messageEmbed.addField("> Banned champions", String.join("\n", bannedChampions), false);

        StringBuilder blueTeamParticipants = new StringBuilder();
        StringBuilder redTeamParticipants = new StringBuilder();

        for (int i = 0; i < summonerCurrentGame.getParticipants().size(); i++) {
            TeamType teamType = summonerCurrentGame.getParticipants().get(i).getTeam();
            String championName = ChampionUtils.getChampionNameById(summonerCurrentGame.getParticipants().get(i).getChampionId());
            switch (teamType) {
                case BLUE ->
                        blueTeamParticipants.append(String.format("%s%s | %s\n", summonerCurrentGame.getParticipants().get(i).getSummonerId().equals(summonerData.getSummonerId()) ? "- " : "", EmojiUtils.getChampionEmojiByChampionName(championName), championName));
                case RED ->
                        redTeamParticipants.append(String.format("%s%s | %s\n", summonerCurrentGame.getParticipants().get(i).getSummonerId().equals(summonerData.getSummonerId()) ? "- " : "", EmojiUtils.getChampionEmojiByChampionName(championName), championName));
            }
        }


        messageEmbed.addField("> Blue team", String.join("", blueTeamParticipants), true);
        messageEmbed.addField("> Red team", String.join("", redTeamParticipants), true);

        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();
    }
}
