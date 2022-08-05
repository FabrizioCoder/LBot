package commands.lol;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class LoLCommand extends SlashCommand {

    public LoLCommand() {
        this.name = "lol";
        this.help = "League of Legends commands";
        this.category = new Category("LoL");
        this.children = new SlashCommand[]{new ProfileSubCommand()};
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }

}
