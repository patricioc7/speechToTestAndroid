package com.pcordoba.speechtotexttest1;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.FROYO)
public class MainActivity extends Activity implements
        RecognitionListener {

    private TextView returnedText;
    private TextView responseText;


   // private Button submitButton;
    private Button button;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private Switch langSwitch;
    private String lang;

    final private String SPANISH = "es-AR";
    final private String ENGLISH = "en";
    final private String ESPANOL = "Español";
    final private String INGLES = "English";

    final private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        returnedText = (TextView) findViewById(R.id.resultMessage);
        responseText = (TextView) findViewById(R.id.response);
        langSwitch = (Switch) findViewById(R.id.langSwitch);
        button = (Button) findViewById(R.id.mantener);
       // submitButton = (Button) findViewById(R.id.mantener);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        lang = SPANISH;

        langSwitch.setChecked(true);
        langSwitch.setText(ESPANOL);



        createSpeech();

        langSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (speech != null) {
                    speech.destroy();
                    createSpeech();
                }

                if (isChecked) {
                    langSwitch.setText(ESPANOL);
                    lang = SPANISH;
                } else {
                    langSwitch.setText(INGLES);
                    lang = ENGLISH;
                }

                if (speech != null) {
                    speech.destroy();
                    createSpeech();
                }

            }
        });

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        askForPermission();
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setIndeterminate(true);
                        if (speech != null) {
                            speech.destroy();
                            createSpeech();
                        }
                        speech.startListening(recognizerIntent);
                        break;
                    case MotionEvent.ACTION_UP:
                        progressBar.setIndeterminate(false);
                        progressBar.setVisibility(View.INVISIBLE);
                        speech.stopListening();

                        break;
                }
                return false;
            }

            private void askForPermission() {

                    String permission = "android.permission.RECORD_AUDIO";
                    if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                            //This is called if user has denied the permission before
                            //In this case I am just asking the permission again
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 1);

                        } else {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 1);
                        }
                    } else {
                    }
                }



        });

        // Here, thisActivity is the current activity


    }

    private void createSpeech() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (speech == null) {
            createSpeech();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        progressBar.setIndeterminate(true);
    }

    @Override
    public void onError(int errorCode) {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        String errorMessage = getErrorText(errorCode);
        returnedText.setText(errorMessage);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {

    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        /*for (String result : matches)
            text += result + "\n";
        */
        if (matches.size() >= 1) {
            text = matches.get(1);
            returnedText.setText(text);
            requestSomething(text);
        } else {
            returnedText.setText("nop");
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public void requestSomething(String text) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.43.156:8087/Guestbook/sendMessage";
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"message\":\""+text+"\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest =new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                // Display the first 500 characters of the response string.
                responseText.setText("Response is: "+ response);
            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseText.setText("BAD REQUEST!");
            }
        }){
        };

        queue.add(jsonRequest);

    }


}