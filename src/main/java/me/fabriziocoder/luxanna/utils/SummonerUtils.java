package me.fabriziocoder.luxanna.utils;

import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.staticdata.Versions;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.impl.lol.builders.championmastery.ChampionMasteryBuilder;
import no.stelar7.api.r4j.impl.lol.builders.league.LeagueBuilder;
import no.stelar7.api.r4j.impl.lol.builders.summoner.SummonerBuilder;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.league.LeagueEntry;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

import javax.annotation.Nullable;
import java.util.List;

public class SummonerUtils {
    @Nullable
    public static Summoner getSummonerByName(String summonerName, LeagueShard platform) {
        return new SummonerBuilder().withName(summonerName).withPlatform(platform).get();
    }

    @Nullable
    public static List<ChampionMastery> getSummonerTopChampionsSummonerId(String summonerId, LeagueShard platform, int count) {
        return new ChampionMasteryBuilder().withSummonerId(summonerId).withPlatform(platform).getTopChampions(count);
    }

    @Nullable
    public static List<LeagueEntry> getSummonerLeagueEntryBySummonerId(String summonerId, LeagueShard platform) {
        return new LeagueBuilder().withSummonerId(summonerId).withPlatform(platform).getLeagueEntries();
    }

    public static String getLatestVersion() {
        return Versions.withRegion(Region.NORTH_AMERICA).get().get(0);
    }

    public static String makeProfileIconURL(String iconId) {
        return "http://ddragon.leagueoflegends.com/cdn/" + getLatestVersion() + "/img/profileicon/" + iconId + ".png";
    }

}
