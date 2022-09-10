package me.fabriziocoder.luxanna.utils;

import com.merakianalytics.orianna.types.core.staticdata.Champion;

public class ChampionUtils {
    public static String getChampionNameById(int championId) {
        return Champion.withId(championId).get().getName();
    }

    public static String normalizeChampionName(String championName) {
        return championName.replace("LeeSin", "Lee Sin").replace("XinZhao", "Xin Zhao").replace("DrMundo", "Dr. Mundo").replace("JarvanIV", "Jarvan IV").replace("MasterYi", "Master Yi").replace("TahmKench", "Tahm Kench").replace("MissFortune", "Miss Fortune").replace("TwistedFate", "Twisted Fate").replace("RenataGlasc", "Renata Glasc").replace("Renata", "Renata Glasc").replace("AurelionSol", "Aurelion Sol");
    }
}
