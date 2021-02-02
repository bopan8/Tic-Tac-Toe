package com.game;

public class Checks {
    public int getFistSpot() {
        return firstSpot;
    }

    public void setFistSpot(int fistSpot) {
        this.firstSpot = fistSpot;
    }

    public int getSecondSpot() {
        return secondSpot;
    }

    public void setSecondSpot(int secondSpot) {
        this.secondSpot = secondSpot;
    }

    private int firstSpot;
    private int secondSpot;

    public boolean checkForWin(boolean circle, String[] spaces, int[][] wins,boolean won){
        for (int i = 0 ; i < wins.length; i++){
            if (circle){
                if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O"){
                    this.setFistSpot(wins[i][0]);
                    this.setSecondSpot(wins[i][2]);
                    won = true;
                }
            } else{
                if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X"){
                    this.setFistSpot(wins[i][0]);
                    this.setSecondSpot(wins[i][2]);
                    won = true;
                }
            }
        }
        return won;
    }

    public boolean checkForEnemyWin(boolean circle, String[] spaces, int[][] wins, boolean enemyWon){
        for (int i = 0 ; i < wins.length; i++){
            if (circle){
                if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X"){
                    this.setFistSpot(wins[i][0]);
                    this.setSecondSpot(wins[i][2]);
                    enemyWon = true;
                }
            } else{
                if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O"){

                    this.setFistSpot(wins[i][0]);
                    this.setSecondSpot(wins[i][2]);
                    enemyWon = true;
                }
            }
        }
        return enemyWon;
    }

    public boolean checkForTie(boolean won, boolean enemyWon, boolean tie, String[] spaces){
        //if every spot is filled
        if (!won && !enemyWon){
            tie = true;
            for (int i = 0; i< spaces.length; i++){
                if (spaces[i] == null) {
                    tie = false;
                }
            }

        }
        return tie;
    }
}
