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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.w3c.dom.*;
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

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private DrawView drawView;
    private int CAMERA_REQUEST = 1;
    private int GALLERY_REQUEST = 2;
    
    private String language = "nl_NL";
    private SpeechRecognizer sr;
    private int[] startSudoku;
    private int[] currentSudoku;


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
        this.currentSudoku = sud2;

        BruteSudoku bruteSudoku = new BruteSudoku(grid);

        long startTime = System.nanoTime();;
//        for(int i = 0; i < 25; i ++){
//            System.out.println(i + "loooooooooooooooooooooop");
//            Sudoku.solve(sud2.clone(), 0);
//            System.out.println(System.nanoTime());
//        }
        int[] clone;
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);
        clone = sud;
        Sudoku.solve(clone, 0);
        print(clone);

        long stopTime = System.nanoTime();
        long elapsedTime = (stopTime - startTime);
        System.out.println("=================================================");
        System.out.println(elapsedTime);
        System.out.println("=================================================");
        //print(sud2);


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
                Intent galleryIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                       "content://media/internal/images/media"
                ));
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
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
                //System.out.println(intentManager.getIntent().toString() + " intent");
                sr.startListening(new IntentManager(language).getIntent());
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
            //recognizeSudoku(photo);
        }
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
             Uri imageUri = data.getData();
             try {
                 Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
             }
             catch(java.io.IOException e) {
                 System.out.println("Something went wrong.");
             }

        }

    }

    public boolean isEditable(int x, int y) {
        return startSudoku[y*9 + x] == 0;
    }

    public void setCell(int x, int y, int value, boolean permanent) {
        String cell = "row" + x + "column" + y;
        String stringValue = "" + value;
        EditText editText = (EditText) findViewById(getResources().getIdentifier(cell, "id", getPackageName()));
        editText.setText(stringValue);
        if (permanent)
            editText.setEnabled(false);
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
            System.out.println(error + " error!!");
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
