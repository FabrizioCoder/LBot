package me.fabriziocoder.luxanna.utils;

import com.merakianalytics.orianna.types.core.staticdata.Champion;

public class ChampionUtils {
    public static String getChampionNameById(int championId) {
        return Champion.withId(championId).get().getName();
    }
}
