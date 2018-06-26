package com.example.uva.arss;

public class BruteSudoku {

    private int[][] grid;
    private int width, height;

    public BruteSudoku(int[][] grid){
        this.grid = grid;
        this.width = grid[0].length;
        this.height = grid.length;
    }

    public int[][] solveSudoku(int [][] grid){
        if (solve(grid)){
            return this.grid;
        }
        return null;
    }

    private boolean solve(int[][] grid){
        for(int row = 0; row < height; row++){
            for(int column = 0; column < width; column++){
                if(grid[row][column] == 0){
                    for(int i = 1; i <= 9; i++){
                        grid[row][column] = i;
                        if(valid(grid, row, column) && solve(grid)){
                            return true;
                        }
                        grid[row][column] = 0;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean valid(int[][] grid, int row, int column){
        return (rowcheck(grid, row) && columncheck(grid, column) && blockcheck(grid, row, column));
    }

    private boolean rowcheck(int[][] grid, int row){
        int[] values = new int[9];
        for (int i = 0; i < grid[row].length; i++){
            for(int j = 0; j < values.length; j++){
                if (grid[row][i] == values[j] && grid[row][i] != 0){
                    return false;
                }

            }
            values[i] = grid[row][i];
        }
        return true;
    }

    private boolean columncheck(int[][] grid, int column){
        int[] values = new int[9];
        for (int i = 0; i < grid.length; i++){
            for(int j = 0; j < values.length; j++){
                if (grid[i][column] == values[j] && grid[i][column] != 0){
                    return false;
                }
            }
            values[i] = grid[i][column];
        }
        return true;
    }

    private boolean blockcheck(int[][] grid, int row, int column){
        int [] rows = new int[3];
        int [] columns = new  int[3];
        int [] values = new int[9];
        if(row == 0 || row == 1 || row == 2){
            rows = new int[]{0, 1, 2};
        }else if(row == 3 || row == 4 || row == 5){
            rows = new int[]{3, 4, 5};
        }else if(row == 6 || row == 7 || row == 8){
            rows = new int[]{6, 7, 8};
        }

        if(column == 0 || column == 1 || column == 2){
            columns = new int[]{0, 1, 2};
        }else if(column == 3 || column == 4 || column == 5){
            columns = new int[]{3, 4, 5};
        }else if(column == 6 || column == 7 || column == 8){
            columns = new int[]{6, 7, 8};
        }


        for(int i = rows[0]; i <= rows[2]; i++){
            for(int j = columns[0]; j <= columns[2]; j++){
                for(int k = 0; k < values.length; k++){
                    if (grid[i][j] == values[k] && grid[i][j] != 0){
                        return false;
                    }
                }
                for(int x = 0; x < values.length; x++){
                    if (values[x] == 0){
                        values[x] = grid[i][j];
                        break;
                    }
                }
            }
        }
        return true;
    }

}
