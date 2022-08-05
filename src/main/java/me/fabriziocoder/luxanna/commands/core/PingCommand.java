package me.fabriziocoder.luxanna.commands.core;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

public class PingCommand extends SlashCommand {

    public PingCommand() {
        this.name = "ping";
        this.cooldown = 5;
        this.category = new Category("Core");
        this.help = "Returns the latency of the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_SEND};
    }

    @Override
    public void execute(SlashCommandEvent event) {
        long gateway = event.getJDA().getGatewayPing();
        event.deferReply().queue(
                hook -> hook.editOriginal(String.format("Pong! (gateway: %sms)", gateway)).queue()
        );
    }
}