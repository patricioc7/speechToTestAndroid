package com.pcordoba.speechtotexttest1;


import java.util.ArrayList;
import java.util.Map;

import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;




@RequiresApi(api = Build.VERSION_CODES.FROYO)
public class MainActivity extends Activity implements
        RecognitionListener {

    private TextView returnedText;
    private TextView responseText;

    private Button submitButton;
    private Button button;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private Switch langSwitch;
    private String lang;
    private int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        returnedText = (TextView) findViewById(R.id.resultMessage);
        responseText = (TextView) findViewById(R.id.response);
        langSwitch = (Switch) findViewById(R.id.langSwitch);
        button = (Button) findViewById(R.id.mantener);
        submitButton = (Button) findViewById(R.id.submitButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        lang = "es-AR";
        langSwitch.setChecked(true);
        langSwitch.setText("Español");

        createSpeech();

        langSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (speech != null) {
                    speech.destroy();
                    createSpeech();
                }

                if(isChecked){
                    langSwitch.setText("Español");
                    lang = "es-AR";
                }else{
                    langSwitch.setText("English");
                    lang = "en";
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
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
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
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSomething();
               // String message = "No Mandé una garcha";
                // i++;
                //showResponse(message + i);

            }
        });
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
        if(matches.size() >= 1){
            text = matches.get(1);
            returnedText.setText(text);
        }else{
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

    public void showResponse(String response) {
        if(responseText.
                getVisibility() == View.GONE) {
            responseText.setVisibility(View.VISIBLE);
        }
        responseText.setText(response);
    }
    public void requestSomething() {
        final String url = "http://demo9721303.mockable.io/";
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"message\":\"tuvieja\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                responseText.setText("Response is: CORRECTO" + response.toString());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseText.setText("Response is: ERROR"+ error.toString());
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                // something to do here ??
                return null;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return null;
            }
        };
    }
}