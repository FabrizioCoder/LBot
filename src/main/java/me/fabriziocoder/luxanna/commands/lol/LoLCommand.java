package me.fabriziocoder.luxanna.commands.lol;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.luxanna.commands.lol.champion.MasterySubCommand;
import me.fabriziocoder.luxanna.commands.lol.champion.RotationSubCommand;
import me.fabriziocoder.luxanna.commands.lol.summoner.ProfileSubCommand;

public class LoLCommand extends SlashCommand {

    public LoLCommand() {
        this.name = "lol";
        this.help = "League of Legends commands";
        this.category = new Category("LoL");
        this.children = new SlashCommand[]{new ProfileSubCommand(), new MasterySubCommand(), new RotationSubCommand()};
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }

}
