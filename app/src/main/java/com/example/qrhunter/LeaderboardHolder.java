package com.example.qrhunter;

public class LeaderboardHolder implements Comparable<LeaderboardHolder>{
    private String username;
    private String score;
    private int userRank;

    public LeaderboardHolder(String userName, String userScore) {
        this.username = userName;
        this.score = userScore;
    }

    public String getUsername() {
        return username;
    }

    public void setScore(String score) {
        this.score = score;
    }
    public String getScore() {
        return score;
    }

    public void setUserRank(int userRank) {
        this.userRank = userRank;
    }
    public int getUserRank() {
        return userRank;
    }

    @Override
    public String toString() {
        return "LeaderBoardHolder{" +
                "userScore='" + score + '\'' +
                '}';
    }

    @Override
    public int compareTo(LeaderboardHolder leaderBoardHolder) {
        return toString().compareTo(leaderBoardHolder.toString());
    }
}
