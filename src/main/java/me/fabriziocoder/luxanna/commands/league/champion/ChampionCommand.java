package me.fabriziocoder.luxanna.commands.league.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class ChampionCommand extends SlashCommand {

    public ChampionCommand() {
        this.name = "champion";
        this.help = "[LoL] Champion commands";
        this.category = new Category("Champion");
        this.children = new SlashCommand[]{new MasterySubCommand(), new RotationSubCommand()};
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }

}