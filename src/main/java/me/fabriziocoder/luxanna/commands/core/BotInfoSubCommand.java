package me.fabriziocoder.luxanna.commands.core;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;

public class BotInfoSubCommand extends SlashCommand {

    public BotInfoSubCommand() {
        this.name = "botinfo";
        this.cooldown = 15;
        this.help = "Shows info about the bot";
        this.guildOnly = true;
    }


    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        EmbedBuilder messageEmbed = new EmbedBuilder().setTitle(String.format("%s - your League of Legends bot", event.getJDA().getSelfUser().getName())).setColor(0x2564f4).setFooter("Last restart", null).setTimestamp(event.getClient().getStartTime()).setThumbnail(event.getJDA().getSelfUser().getAvatarUrl()).setImage("https://i.imgur.com/XTgkUlF.png");

        String[] botStats = {String.format("`Servers:` %,7d", event.getJDA().getGuilds().size()), String.format("`Gateway ping:` %sms", event.getJDA().getGatewayPing()), String.format("`Rest ping:` %sms", event.getJDA().getRestPing().complete())};
        messageEmbed.addField("> Bot stats", String.join("\n", botStats), false);

        String[] librariesInfoDescription = {String.format("`JDA:` [%s](%s)", JDAInfo.VERSION, JDAInfo.GITHUB), String.format("`JDA-Utilities:` [%s](%s)", JDAUtilitiesInfo.VERSION, JDAUtilitiesInfo.GITHUB), String.format("`Orianna:` [%s](%s)", "4.0.0-rc8", "https://github.com/meraki-analytics/orianna"), String.format("`R4J:` [%s](%s)", "2.1.22", "https://github.com/stelar7/R4J")};
        messageEmbed.addField("> Libraries versions", String.join("\n", librariesInfoDescription), false);

        messageEmbed.addField("> Developer", event.getJDA().retrieveApplicationInfo().complete().getOwner().getAsMention(), true);
        messageEmbed.addField("> Links", String.format("[Support Server](%s)\n[Bot Invite](%s)", event.getClient().getServerInvite(), "https://discord.com/api/oauth2/authorize?client_id=949565943275720736&permissions=2147796992&scope=bot%20applications.commands"), true);

        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();
    }
}
