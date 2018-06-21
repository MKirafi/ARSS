package com.example.uva.arss;

import android.content.Intent;
import java.util.Locale;
import android.speech.RecognizerIntent;

public class IntentManager {
    public Intent intent;
    public IntentManager() {
        this.intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say X Y Value. ex: 4 5 7");
    }
}
