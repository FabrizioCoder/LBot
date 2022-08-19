package me.fabriziocoder.luxanna.commands.league.champion;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.fabriziocoder.database.MongoDB;
import me.fabriziocoder.luxanna.utils.ChampionUtils;
import me.fabriziocoder.luxanna.utils.EmojiUtils;
import me.fabriziocoder.luxanna.utils.SummonerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import org.bson.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MasterySubCommand extends SlashCommand {

    public static final String COMMAND_NAME = "mastery";
    public static final String COMMAND_DESCRIPTION = "Shows the best summoner champions";
    private static final DecimalFormat oneDecimal = new DecimalFormat("0.0");

    public MasterySubCommand() {
        this.name = COMMAND_NAME;
        this.help = COMMAND_DESCRIPTION;
        this.cooldown = 15;
        this.guildOnly = false;
        this.options = List.of(new OptionData(OptionType.STRING, "summoner-name", "The name of the summoner to search for").setRequired(false), new OptionData(OptionType.STRING, "region", "The region of the account").addChoices(regionChoices()).setRequired(false));
    }

    public static String humanReadableInt(long number) {
        long absNumber = Math.abs(number);
        double result;
        String suffix = "";
        if (absNumber < 1024) {
            result = number;
        } else if (absNumber < 1024 * 1024) {
            result = number / 1024.0;
            suffix = "K";
        } else if (absNumber < 1024 * 1024 * 1024) {
            result = number / (1024.0 * 1024);
            suffix = "M";
        } else {
            result = number / (1024.0 * 1024 * 1024);
            suffix = "B";
        }
        return oneDecimal.format(result) + suffix;
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

        String summonerName = event.optString("summoner-name");
        String region = event.optString("region");
        if (summonerName == null && region == null) {
            Document existUserProfile = MongoDB.userProfileExists(event.getUser().getIdLong()).first();
            if (existUserProfile != null) {
                summonerName = existUserProfile.getString("summonerName");
                region = existUserProfile.getString("summonerPlatform");
            } else {
                event.getHook().editOriginal(String.format("%s You don't yet have an account registered to use the no-argument command.", EmojiUtils.Discord.X)).queue();
                return;
            }
        } else if (summonerName == null) {
            event.getHook().editOriginal(String.format("%s You need to specify a summoner name.", EmojiUtils.Discord.X)).queue();
            return;
        } else if (region == null) {
            event.getHook().editOriginal(String.format("%s You need to specify a region.", EmojiUtils.Discord.X)).queue();
            return;
        }

        final Summoner summonerData = SummonerUtils.getSummonerByName(summonerName, LeagueShard.valueOf(region));

        if (summonerData == null) {
            event.getHook().editOriginal(String.format("%s That summoner couldn't be found, at least on that region.", EmojiUtils.Discord.X)).queue();
            return;
        }

        final List<ChampionMastery> summonerTopChampions = SummonerUtils.getSummonerTopChampionsSummonerId(summonerData.getSummonerId(), LeagueShard.valueOf(region), 15);

        if (summonerTopChampions == null) {
            event.getHook().editOriginal("This summoner has not played champions.").queue();
            return;
        }

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < summonerTopChampions.size(); i++) {
            ChampionMastery mastery = summonerTopChampions.get(i);
            String championName = ChampionUtils.getChampionNameById(mastery.getChampionId());
            str.append(String.format("`%3d.` [`%5s`] [`%d`] %s %s%n", i + 1, humanReadableInt(mastery.getChampionPoints()), mastery.getChampionLevel(), EmojiUtils.getChampionEmojiByChampionName(championName), championName));
        }
        MessageEmbed messageEmbed = new EmbedBuilder().setColor(0x2564f4).setAuthor(String.format("Top %s Champion%s", summonerTopChampions.size(), summonerTopChampions.size() == 1 ? "" : "s")).setTitle(String.format("`%s` - `%s`", summonerData.getName(), summonerData.getPlatform().prettyName())).setThumbnail(SummonerUtils.makeProfileIconURL(String.valueOf(summonerData.getProfileIconId()))).setDescription(str.toString()).build();
        event.getHook().sendMessageEmbeds(messageEmbed).queue();
    }
}