/*
    The sudoku-class implements methods to solve and check sudokus.
 */
package com.example.uva.arss;

import java.util.Arrays;

public class Sudoku {

    private static final int[] freqs = new int[10];

    // Returns a solution for a grid if grid is unsolvable return null.
    static public int[] solveSudoku(int [] grid, int cell){
        if (solve(grid, cell)){
            return grid;
        }
        return null;
    }

    // Returns true if sudoku is solved and modifies grid. If sudoku cannot be solved returns
    // false.
    public static boolean solve(int[] grid, int cell){
        while (cell < 81 && grid[cell] > 0){
            cell++;
        }

        if(cell == 81){
            return true;
        }

        for(int i = 1; i <= 9; i++){
            grid[cell] = i;
            if(rowcheck(grid, cell / 9)){
                if(columncheck(grid, cell % 9)){
                    if(blockcheck(grid, cell / 9 % 3 * 3, cell % 3 * 3)){
                        if(valid(grid) && solve(grid, cell + 1)){
                            return true;
                        }
                    }
                }
            }
        }
        grid[cell] = 0;
        return false;
    }

    // Returns whether the current state of the sudoku is correct according to the rules of a
    // sudoku
    public static boolean valid(int[] grid){
        for(int i = 0; i < 9; i++) {
            if(!rowcheck(grid, i))
                return false;
            if(!columncheck(grid, i))
                return false;
            if(!blockcheck(grid, i % 3 * 3, i / 3 * 3))
                return false;
        }
        return true;
    }

    // Returns true if all ints in row are unique, otherwise returns false.
    public static boolean rowcheck(int[] grid, int row){
        Arrays.fill(freqs, 0);

        for(int column = 0; column < 9; column++){
            int cell = grid[(row * 9)+ column];
            if(cell > 0 && ++freqs[cell] > 1)
                return false;
        }
        return true;
    }

    // Returns true if all ints in a collumn are unique, otherwise returns false.
    public static boolean columncheck(int[] grid, int column){
        Arrays.fill(freqs, 0);

        for(int row = 0; row < 9; row++){
            int cell = grid[(row*9) + column];
            if(cell > 0 && ++freqs[cell] > 1)
                return false;
        }
        return true;
    }

    // Returns true if all int in block are unique, otherwise returns false.
    public static boolean blockcheck(int[] grid, int row, int column){
        Arrays.fill(freqs, 0);

        for(int i = 0; i < 9; i++)
        {
            int cell = grid[(row + i / 3) * 9 + (column + i % 3)];
            if(cell > 0 && ++freqs[cell] > 1)
                return false;
        }
        return true;
    }

    // Returns whether sudoku is filled in completely.
    public static boolean complete(int[] grid) {
        for(int cell : grid) {
            if(cell == 0) return false;
        }
        return true;
    }

}
