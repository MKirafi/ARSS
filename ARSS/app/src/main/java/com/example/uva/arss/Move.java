/*
 Move is a data type for a move in a sudoku. It contains the x and y coordinate of a move and its
 value.
 */

package com.example.uva.arss;

public class Move {
    private int[] coordinate= new int[2];
    private int value;
    private boolean valid;

    // Sets coordinate and value with translation.
    Move(char x, int y, int value) {
        this.setValid();
        this.coordinate[0] = translateToNumber(x);
        this.coordinate[1] = y - 1;
        this.value = value;
    }

    // Sets coordinate and value without translation.
    Move(int x, int y, int value) {
        this.setValid();
        this.coordinate[0] = x;
        this.coordinate[1] = y;
        this.value = value;
    }

    // Creates an invalid move.
    Move() {
        this.setInvalid();
    }

    // Sets the x coordinate of a move with translation.
    void setTranslatedX(char x) {
        this.coordinate[0] = translateToNumber(x);
    }

    // Sets the y coordinate of a move with translation.
    void setTranslatedY(int y) {
        this.coordinate[1] = y - 1;
    }

    void setValue(int value) {
        this.value = value;
    }

    void setValid() {
        this.valid = true;
    }

    void setInvalid() {
        this.valid = false;
    }

    boolean isValid() {
        return this.valid;
    }

    int[] getCoordinate() {
        return this.coordinate;
    }

    int getX() {
        return this.coordinate[0];
    }

    int getY() {
        return this.coordinate[1];
    }

    int getValue() { return this.value; }

    // Returns a number from a translated char.
    // A -> 0, B -> 1, C -> 2 ...
    // a -> 0, b -> 1, c -> 2 ...
    private int translateToNumber(char c) {
        return (c - 97 >= 0) ? c - 97 : c - 65;
    }
}
