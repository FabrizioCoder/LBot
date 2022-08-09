package me.fabriziocoder.luxanna.commands.league.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.impl.lol.raw.DDragonAPI;
import no.stelar7.api.r4j.pojo.lol.staticdata.champion.StaticChampion;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InformationSubCommand extends SlashCommand {
    static final String COMMAND_NAME = "information";
    static final String COMMAND_DESCRIPTION = "Get information about a champion";

    public InformationSubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.guildOnly = false;
        this.cooldown = 10;
        this.options = List.of(new OptionData(OptionType.STRING, "champion", "Champion's name", true).setAutoComplete(true));
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        Map<Integer, StaticChampion> allChampions = DDragonAPI.getInstance().getChampions();
        if (event.getFocusedOption().getName().equals("champion")) {
            List<Command.Choice> choiceList;
            choiceList = allChampions.values().stream().filter(champion -> champion.getName().toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase())).map(championData -> new Command.Choice(championData.getName(), championData.getId())).limit(15).toList();
            event.replyChoices(choiceList).queue();
        }
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
        int championId = Integer.parseInt(Objects.requireNonNull(event.optString("champion")));
        final Champion championData = Orianna.championWithId(championId).get();
        if (!championData.exists()) {
            event.getHook().editOriginal(String.format("%s This champion doesn't exist, or you misspelled it.", EmojiUtils.Discord.X)).queue();
            return;
        }

        EmbedBuilder messageEmbed = new EmbedBuilder().setColor(0x2564f4).setThumbnail(championData.getImage().getURL()).setTitle(String.format("`%s`, `%s`", championData.getName(), championData.getTitle())).setDescription(championData.getLore());

        messageEmbed.addField("> Ally tips", championData.getAllyTips().toString().replace("[", "").replace("]", ""), true);
        messageEmbed.addField("> Enemy tips", championData.getEnemyTips().toString().replace("[", "").replace("]", ""), true);

        event.getHook().sendMessageEmbeds(messageEmbed.build()).queue();
    }
}
