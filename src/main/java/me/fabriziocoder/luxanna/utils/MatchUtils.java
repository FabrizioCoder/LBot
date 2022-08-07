package me.fabriziocoder.luxanna.utils;

import no.stelar7.api.r4j.impl.lol.builders.matchv5.match.MatchBuilder;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class MatchUtils {

    @Nullable
    public static MatchParticipant getSummonerLastMatchBySummoner(Summoner summoner) {

        MatchParticipant matchParticipant = null;
        String matchId = summoner.getLeagueGames().getLazy().get(0);

        if (matchId == null) {
            return null;
        }
        MatchBuilder matchBuilder = new MatchBuilder(summoner.getPlatform());

        matchBuilder = matchBuilder.withId(matchId);
        for (final MatchParticipant participant : matchBuilder.getMatch().getParticipants()) {

            if (participant.getSummonerId().equals(summoner.getSummonerId())) {
                matchParticipant = participant;
            }
        }
        return matchParticipant;
    }

    @Nullable
    public static List<MatchParticipant> getSummonerThreeRecentGames(Summoner summoner) {
        List<String> recentGamesId = summoner.getLeagueGames().get();
        if (recentGamesId.size() == 0) {
            return null;
        }
        MatchBuilder matchBuilder = new MatchBuilder(summoner.getPlatform());
        List<MatchParticipant> data = new ArrayList<>();

        for (int i = 0; i < recentGamesId.size(); i++) {
            if (i >= 3) break;
            LOLMatch match = matchBuilder.withId(recentGamesId.get(i)).getMatch();
            for (final MatchParticipant participant : match.getParticipants()) {
                if (participant.getSummonerId().equals(summoner.getSummonerId())) {
                    data.add(participant);
                }
            }
        }
        return data;
    }

}
