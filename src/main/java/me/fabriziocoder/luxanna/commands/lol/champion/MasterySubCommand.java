package me.fabriziocoder.luxanna.commands.lol.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.championmastery.ChampionMasteries;
import com.merakianalytics.orianna.types.core.championmastery.ChampionMastery;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class MasterySubCommand extends SlashCommand {
    public MasterySubCommand() {
        this.name = "mastery";
        this.help = "Shows the top mastery champions";
        this.cooldown = 15;
        this.guildOnly = false;
        this.options = List.of(new OptionData(OptionType.STRING, "name", "The summoner's name").setRequired(true), new OptionData(OptionType.STRING, "region", "The region of the summoner").addChoices(regionChoices()).setRequired(true));
    }

    private List<Command.Choice> regionChoices() {
        List<Command.Choice> options = new ArrayList<>();
        for (Region c : Region.values()) {
            options.add(new Command.Choice(c.name(), c.name()));
        }
        return options;
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

        if (!championMasteries.exists()) {
            event.getHook().editOriginal("This summoner has not played champions.").queue();
            return;
        }

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < 15; i++) {
            if (i >= championMasteries.size()) {
                break;
            }

            ChampionMastery mastery = championMasteries.get(i);
            Champion champion = mastery.getChampion();
            str.append(String.format("%3d) %-16s %,7d (%d)%n", i + 1, champion.getName(), mastery.getPoints(), mastery.getLevel()));
        }

        event.getHook().editOriginal(String.format("%s's Top Champs:\n```k\n%s\n```", summoner.getName(), str)).queue();
    }
}
