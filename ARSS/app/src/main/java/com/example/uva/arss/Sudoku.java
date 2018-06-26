package com.example.uva.arss;

import java.util.Arrays;

public class Sudoku {

    private static final int[] freqs = new int[10];

    static public int[] solveSudoku(int [] grid, int cell){
        if (solve(grid, cell)){
            return grid;
        }
        return null;
    }

    public static boolean solve(int[] grid, int cell){
        while (cell < 81 && grid[cell] > 0){
            //System.out.println("Cell: " + cell + " ========================");
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

    public static boolean rowcheck(int[] grid, int row){
        Arrays.fill(freqs, 0);

        for(int column = 0; column < 9; column++){
            int cell = grid[(row * 9)+ column];
            if(cell > 0 && ++freqs[cell] > 1)
                return false;
        }
        return true;
    }

    public static boolean columncheck(int[] grid, int column){
        Arrays.fill(freqs, 0);

        for(int row = 0; row < 9; row++){
            int cell = grid[(row*9) + column];
            if(cell > 0 && ++freqs[cell] > 1)
                return false;
        }
        return true;
    }

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

}
