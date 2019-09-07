package com.example.projectapplication;

import java.util.Date;

public class Games {

    public Date upcomingGame;
    public String upcomingOpponent, recentOpponent;

    public Games() {
        // Default constructor
    }

    public Games(Date upcoming, String upcomingOppo, String recentOppo) {
        this.upcomingGame = upcoming;
        this.upcomingOpponent = upcomingOppo;
        this.recentOpponent = recentOppo;
    }

    public void setUpcomingGame(Date upcomingGameDate) {
        this.upcomingGame = upcomingGameDate;
    }

    public void setUpcomingOpponent(String setUpcomingOpponent) {
        this.upcomingOpponent = setUpcomingOpponent;
    }

    public void setRecentOpponent(String setRecentOpponent) {
        this.recentOpponent = setRecentOpponent;
    }

    public Date getUpcomingGame() {
        return upcomingGame;
    }

    public String getUpcomingOpponent() {
        return upcomingOpponent;
    }

    public String getRecentOpponent() {
        return recentOpponent;
    }

}
