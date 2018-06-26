/*
 *   A wrapper class for intent.
 */

package com.example.uva.arss;

import android.content.Intent;
import android.speech.RecognizerIntent;

public class IntentManager {
    private Intent intent;
    public IntentManager(String lang) {
        String prompt = (lang == "en")
            ?"Say: location X Y Value. Ex: location A3 7."
                : "Zeg: plaats X Y Waarde. Vb: plaats A3 7";

        this.intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
    }
    Intent getIntent() {
        return intent;
    }
}
