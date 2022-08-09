package me.fabriziocoder.luxanna.commands.league.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.luxanna.utils.ChampionUtils;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import me.fabriziocoder.luxanna.utils.SummonerUtils;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

import java.util.ArrayList;
import java.util.List;

public class MasterySubCommand extends SlashCommand {

    public static final String COMMAND_NAME = "mastery";
    public static final String COMMAND_DESCRIPTION = "Shows the best summoner champions";

    public MasterySubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 15;
        this.guildOnly = false;
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

        final List<ChampionMastery> summonerTopChampions = SummonerUtils.getSummonerTopChampionsSummonerId(summonerData.getSummonerId(), LeagueShard.valueOf(region), 15);

        if (summonerTopChampions == null) {
            event.getHook().editOriginal("This summoner has not played champions.").queue();
            return;
        }

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < summonerTopChampions.size(); i++) {
            ChampionMastery mastery = summonerTopChampions.get(i);
            String champion = ChampionUtils.getChampionNameById(mastery.getChampionId());
            str.append(String.format("%3d) %-16s %,7d (%d)%n", i + 1, champion, mastery.getChampionPoints(), mastery.getChampionLevel()));
        }

        event.getHook().editOriginal(String.format("**%s**'s Top %s Champion%s:\n```k\n%s\n```", summonerData.getName(), summonerTopChampions.size(), summonerTopChampions.size() > 1 ? "s" : "", str)).queue();
    }
}
