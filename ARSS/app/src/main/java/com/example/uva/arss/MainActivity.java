package com.example.uva.arss;
import com.example.uva.arss.Sudoku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[][] grid2 = {
                {0,0,7,0,6,0,1,0,9},
                {0,9,0,2,7,0,3,0,0},
                {0,0,0,0,0,5,0,0,7},
                {0,0,9,7,0,0,5,0,3},
                {7,0,0,0,0,0,0,0,8},
                {5,0,1,0,0,9,6,0,0},
                {2,0,0,9,0,0,0,0,0},
                {0,0,4,0,5,7,0,2,0},
                {3,0,8,0,4,0,7,0,0}
        };

        int[][] grid = {
                {8,0,0,0,0,0,0,0,0},
                {0,0,3,6,0,0,0,0,0},
                {0,7,0,0,9,0,2,0,0},
                {0,5,0,0,0,7,0,0,0},
                {0,0,0,0,4,5,7,0,0},
                {0,0,0,1,0,0,0,3,0},
                {0,0,1,0,0,0,0,6,8},
                {0,0,8,5,0,0,0,1,0},
                {0,9,0,0,0,0,4,0,0}
        };

        int[] sud = {8,0,0,0,0,0,0,0,0,
                0,0,3,6,0,0,0,0,0,
                0,7,0,0,9,0,2,0,0,
                0,5,0,0,0,7,0,0,0,
                0,0,0,0,4,5,7,0,0,
                0,0,0,1,0,0,0,3,0,
                0,0,1,0,0,0,0,6,8,
                0,0,8,5,0,0,0,1,0,
                0,9,0,0,0,0,4,0,0};

        BruteSudoku bruteSudoku = new BruteSudoku(grid);
        long startTime = System.nanoTime();
        bruteSudoku.solveSudoku(grid);
//        Sudoku.solve(sud, 0);
        System.out.println("Elapsed time: " + (System.nanoTime() - startTime));
        print(sud);


//        for(int i = 0; i < grid[0].length; i++){
//            System.out.print("{");
//            for(int j = 0; j < grid.length; j++){
//                if(j == 3 || j == 6){
//                    System.out.print("|");
//                }
//                System.out.print(grid[i][j] + ", ");
//            }
//            System.out.println("}");
//        }

    }

    public static void print(int[] grid)
    {
        for(int i = 0; i < 81; i += 9)
            System.out.println(Arrays.toString(Arrays.copyOfRange(grid, i, i + 9)));
        System.out.println();
    }
}
