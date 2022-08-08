package me.fabriziocoder.luxanna.commands.core;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class CoreCommand extends SlashCommand {

    public CoreCommand() {
        this.name = "core";
        this.help = "Core commands";
        this.category = new Category("Core");
        this.children = new SlashCommand[]{new HelpSubCommand(), new PingSubCommand()};
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }

}