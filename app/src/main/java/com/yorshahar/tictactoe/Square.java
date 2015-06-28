package com.yorshahar.tictactoe;

public class Square {
    private SquareType type;

    public Square(SquareType type) {
        this.type = type;
    }

    public SquareType getType() {
        return type;
    }

    public void setType(SquareType type) {
        this.type = type;
    }

}
