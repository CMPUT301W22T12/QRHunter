package com.example.qrhunter;

public class LeaderboardData {
    String name;
    Long score;

    public LeaderboardData() {
    }

    public LeaderboardData(String name, Long score) {
        this.name = name;
        this.score = score;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}

