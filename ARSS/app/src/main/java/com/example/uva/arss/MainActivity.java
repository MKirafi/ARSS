package com.example.uva.arss;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.w3c.dom.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    private DrawView drawView;
    private int CAMERA_REQUEST = 1;
    private int GALLERY_REQUEST = 2;
    
    private String language = "nl_NL";
    private SpeechRecognizer sr;
    private int[] startSudoku;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        int[] sud2 = {0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0};

        this.startSudoku = sud;

        setContentView(R.layout.activity_main);

        Spinner language_spinner = (Spinner) findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.
                    createFromResource(this, R.array.language_array, android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language_spinner.setAdapter(languageAdapter);

        checkPermission();

        Button takeImage = findViewById(R.id.take_image_button);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        Button loadImage = findViewById(R.id.load_image_button);
        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            }

        });

        this.sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new speechListener());

        /* "record audio" button: */
        View recordAudio = findViewById(R.id.record_audio);
        recordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instruction = (language == "en")
                        ?"Say: location X Y Value. Ex: location B3 7."
                        : "Zeg: plaats X Y Waarde. Vb: plaats A3 7";
                Toast.makeText(getApplicationContext(),
                        instruction,
                        Toast.LENGTH_LONG).show();
                sr.startListening(new IntentManager(language).getIntent());
            }
        });

        /* "check sudoku" button: */
        View checkSudoku = findViewById(R.id.check_sudoku);
        checkSudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] sudoku = getSudoku();
                boolean valid = Sudoku.solveSudoku(sudoku, 0) != null;
                boolean complete = Sudoku.complete(sudoku);

                Toast.makeText(getApplicationContext(),
                        "Sudoku is\n" + "valid: " + valid + "\n" + "complete: " + complete,
                        Toast.LENGTH_LONG).show();
            }
        });

        /* "solve sudoku" button: */
        View solveSudoku = findViewById(R.id.solve_sudoku);
        solveSudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] sudoku = getSudoku();
                int[] solved = Sudoku.solveSudoku(sudoku, 0);
                if(solved == null) {
                    Toast.makeText(getApplicationContext(),
                            "Sudoku is unsolvable.",
                            Toast.LENGTH_LONG).show();
                } else {
                    fillSudokuWithGrid(solved, false);
                }
            }
        });

        Spinner languageSpinner = (Spinner)this.findViewById(R.id.language_spinner);

        /* Switching between colors: */
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    language = "nl_NL";
                } else {
                    language = "en";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public static void print(int[] grid) {
        for (int i = 0; i < 81; i += 9) {
            System.out.println(Arrays.toString(Arrays.copyOfRange(grid, i, i + 9)));
        }
        System.out.println();
    }

    private void checkPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
    }

    /**
     * Callback for speech recognition activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            fillSudoku(photo);
        }
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
             Uri imageUri = data.getData();
             try {
                 Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                 fillSudoku(photo);
             }
             catch(java.io.IOException e) {
                 System.out.println("Something went wrong.");
             }

        }

    }

    public boolean isEditable(int x, int y) {
        return this.startSudoku[y*9 + x] == 0;
    }

    public void setCell(int x, int y, int value, boolean permanent) {
        String cell = "row" + x + "column" + y;
        String stringValue = value == 0 ? "" : "" + value;
        EditText editText = (EditText) findViewById(getResources().getIdentifier(cell, "id", getPackageName()));
        editText.setText(stringValue);
        if (permanent && value != 0)
            editText.setEnabled(false);
    }

    public int[] getSudoku() {
        int[] sudoku = new int[81];
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                String selector = "row" + i + "column" + j;
                EditText cell = (EditText) findViewById(getResources().getIdentifier(selector, "id", getPackageName()));
                String cellText = cell.getText().toString();
                cellText = cellText.equals("") ? "0" : cellText;
                sudoku[i*9 + j] = Integer.parseInt(cellText);
            }
        }
        return sudoku;
    }

    public void fillSudokuWithGrid(int[] grid, boolean permanent) {
        for(int i = 0; i < grid.length; i++) {
            setCell(i / 9, i % 9, grid[i], permanent);
        }
    }

    public void fillSudoku(Bitmap bitmap) {
//        int[] sud = recognizeSudoku(bitmap);
        int[] sud = this.startSudoku;
        fillSudokuWithGrid(sud, true);
    }

    class speechListener implements RecognitionListener {

        public void onReadyForSpeech(Bundle params) {
        }

        public void onBeginningOfSpeech() {
        }

        public void onRmsChanged(float rmsdB) {
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
        }

        public void onError(int error) {

        }

        public void onResults(Bundle results) {
            String res = results.getStringArrayList(sr.RESULTS_RECOGNITION).get(0);
            VoiceCommand command = new VoiceCommand(res, language);
            Move m = command.getMove();
            if (!m.isValid()) {
                Toast.makeText(getApplicationContext(),
                        "Could not parse move. ",
                        Toast.LENGTH_SHORT).show();
            } else {
                int x = m.getX();
                int y = m.getY();
                int value = m.getValue();

                if(isEditable(x, y)) {
                    setCell(y, x, value, false);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Tried to modify non editable cell.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }

        public void onPartialResults(Bundle partialResults) {
        }

        public void onEvent(int eventType, Bundle params) {
        }
    }

}
