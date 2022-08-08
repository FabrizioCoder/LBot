package me.fabriziocoder.luxanna.commands.core;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.merakianalytics.orianna.Orianna;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import no.stelar7.api.r4j.impl.R4J;

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
        EmbedBuilder messageEmbed = new EmbedBuilder().setColor(0x2564f4).setAuthor(String.format("All about %s", event.getJDA().getSelfUser().getName()), event.getClient().getServerInvite(), null).setFooter("Last restart", null).setTimestamp(event.getClient().getStartTime()).setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());

        String[] botStats = {String.format("`Users:` %s", event.getJDA().getUsers().size()), String.format("`Servers:` %s", event.getJDA().getGuilds().size()), String.format("`Gateway ping:` %sms", event.getJDA().getGatewayPing()), String.format("`Text channels:` %s", event.getJDA().getTextChannels().size()), String.format("`Voice channels:` %s", event.getJDA().getVoiceChannels().size())};
        messageEmbed.addField("> Bot stats", String.join("\n", botStats), false);

        String[] librariesInfoDescription = {String.format("`JDA:` [%s](%s)", JDAInfo.VERSION, JDAInfo.GITHUB), String.format("`JDA-Utilities:` [%s](%s)", JDAUtilitiesInfo.VERSION, JDAUtilitiesInfo.GITHUB), String.format("`Orianna:` [%s](%s)", Orianna.class.getPackage().getImplementationVersion(), "https://github.com/meraki-analytics/orianna"), String.format("`R4J:` [%s](%s)", R4J.class.getPackage().getImplementationVersion(), "https://github.com/stelar7/R4J")};
        messageEmbed.addField("> Libraries versions", String.join("\n", librariesInfoDescription), false);

        messageEmbed.addField("> Developer", event.getJDA().retrieveApplicationInfo().complete().getOwner().getAsMention(), true);
        messageEmbed.addField("> Bot invite", String.format("[Click here](%s)", event.getClient().getServerInvite()), true);

        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();
    }
}
