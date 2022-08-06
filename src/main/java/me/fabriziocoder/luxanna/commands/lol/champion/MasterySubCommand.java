package me.fabriziocoder.luxanna.commands.lol.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.merakianalytics.orianna.types.common.Platform;
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

    public static final String COMMAND_NAME = "mastery";
    public static final String COMMAND_DESCRIPTION = "Shows the best summoner champions";

    public MasterySubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 15;
        this.guildOnly = false;
        this.options = List.of(new OptionData(OptionType.STRING, "name", "The name of the summoner to search for").setRequired(true), new OptionData(OptionType.STRING, "region", "The region of the account").addChoices(regionChoices()).setRequired(true));
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
