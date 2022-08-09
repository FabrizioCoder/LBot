package me.fabriziocoder.luxanna.commands.league.summoner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class SummonerCommand extends SlashCommand {

    public SummonerCommand() {
        this.name = "summoner";
        this.help = "[LoL] Summoner commands";
        this.category = new Category("Summoner");
        this.children = new SlashCommand[]{new ProfileSubCommand(), new CurrentSubCommand(), new LastMatchSubCommand()};
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }

}