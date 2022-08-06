package me.fabriziocoder.luxanna.utils;

import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.impl.lol.builders.matchv5.match.MatchBuilder;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;


public class MatchUtils {

    public static MatchParticipant getLastMatch(String summoner, LeagueShard platform) {

        MatchParticipant matchParticipant = null;
        Summoner summonerByName = Summoner.byName(platform, summoner);

        String matchId = summonerByName.getLeagueGames().getLazy().get(0);

        if (matchId == null) {
            return null;
        }
        MatchBuilder matchBuilder = new MatchBuilder(summonerByName.getPlatform());

        matchBuilder = matchBuilder.withId(matchId);
        for (final MatchParticipant participant : matchBuilder.getMatch().getParticipants()) {

            if (participant.getSummonerId().equals(summonerByName.getSummonerId())) {
                matchParticipant = participant;
            }
        }
        return matchParticipant;
    }

}
