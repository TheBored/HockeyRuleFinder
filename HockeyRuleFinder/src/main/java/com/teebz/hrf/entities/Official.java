package com.teebz.hrf.entities;

public class Official {
    public final String number;
    public final String name;
    public final String league;
    public final String memberSince;
    public final String regSeasonCount;
    public final String firstRegSeason;
    public final String firstRegGame;
    public final String playoffCount;
    public final String firstPlayoffSeason;
    public final String firstPlayoffGame;

    public Official(String number, String name, String league, String memberSince,
                    String regSeasonCount, String firstRegSeason, String firstRegGame,
                    String playoffCount, String firstPlayoffSeason, String firstPlayoffGame ){
        this.name = name;
        this.number = number;
        this.league = league;
        this.memberSince = memberSince;
        this.regSeasonCount = regSeasonCount;
        this.firstRegSeason = firstRegSeason;
        this.firstRegGame = firstRegGame;
        this.playoffCount = playoffCount;
        this.firstPlayoffSeason = firstPlayoffSeason;
        this.firstPlayoffGame = firstPlayoffGame;
    }
}
