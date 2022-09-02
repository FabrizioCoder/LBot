package me.fabriziocoder.luxanna.commands.league.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.impl.lol.raw.ChampionAPI;
import no.stelar7.api.r4j.pojo.lol.staticdata.champion.StaticChampion;

import java.util.ArrayList;
import java.util.List;

public class RotationSubCommand extends SlashCommand {
    public static final String COMMAND_NAME = "rotation";
    public static final String COMMAND_DESCRIPTION = "Displays the champion rotation";

    public RotationSubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.guildOnly = false;
        this.cooldown = 20;
        this.options = List.of(new OptionData(OptionType.STRING, "region", "The region from which you want to obtain the free champion rotation").addChoices(regionChoices()).setRequired(true));
    }

    private List<Command.Choice> regionChoices() {
        List<Command.Choice> options = new ArrayList<>();
        LeagueShard[] leagueShards = LeagueShard.values();
        for (int i = 1; i < leagueShards.length; i++) {
            if (i >= 12) break;
            LeagueShard leagueShard = leagueShards[i];
            String keyName = leagueShard.getKeys()[1];
            if (keyName.isEmpty()) continue;
            options.add(new Command.Choice(keyName.toUpperCase(), leagueShard.name()));
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

        List<StaticChampion> freeChampions = ChampionAPI.getInstance().getFreeToPlayRotation(LeagueShard.valueOf(event.optString("region"))).getFreeChampions();

        StringBuilder freeChampionsToPLayText = new StringBuilder();
        for (int i = 0; i < freeChampions.size(); i++) {
            StaticChampion freeChampion = freeChampions.get(i);
            freeChampionsToPLayText.append(String.format("`%3d.` %s %s\n", i + 1, EmojiUtils.getChampionEmojiByChampionName(freeChampion.getName()), freeChampion.getName()));
        }
        EmbedBuilder messageEmbed = new EmbedBuilder().setColor(0x2564f4).setThumbnail(event.getJDA().getSelfUser().getAvatarUrl()).setTitle(String.format("Free champions rotation to play in `%s`", LeagueShard.valueOf(event.optString("region")).prettyName())).setDescription(freeChampionsToPLayText.toString());
        messageEmbed.setFooter("Vote for me | https://top.gg/bot/949565943275720736/vote");
        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();
    }
}
