package com.example.uva.arss;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private IntentManager intentManager;
    private TextView txtOutput;
    private String language = "nl_NL";
    private SpeechRecognizer sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.intentManager = new IntentManager(language);
        this.sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new speechListener());

        /* "record audio" button: */
        findViewById(R.id.record_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sr.startListening(intentManager.intent);
            }
        });
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
            System.out.println("test123");
            String res = results.getStringArrayList(sr.RESULTS_RECOGNITION).get(0);
            VoiceCommand command = new VoiceCommand(res, language);
            int[] values = command.getValues();
            if (values.length == 0) {
                Toast.makeText(getApplicationContext(),
                        "Could not parse move.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        values[0] + " " + values[1] + " " + values[2],
                        Toast.LENGTH_LONG).show();
            }

        }

        public void onPartialResults(Bundle partialResults) {
        }

        public void onEvent(int eventType, Bundle params) {
        }
    }

}







