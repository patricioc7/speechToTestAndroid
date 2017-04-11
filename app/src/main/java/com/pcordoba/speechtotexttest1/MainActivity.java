package com.pcordoba.speechtotexttest1;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.FROYO)
public class MainActivity extends ActionBarActivity implements
        RecognitionListener {

    private RestPoster restPoster = new RestPoster(this);
    private TextView returnedText;
    private TextView responseText;
    private Button button;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String lang;
    private String restUrl;
    private String restPort;
    final private String SPANISH = "es-AR";
    final private String ENGLISH = "en";

    private SharedPreferences SP;

    final private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        returnedText = (TextView) findViewById(R.id.resultMessage);
        responseText = (TextView) findViewById(R.id.response);
        button = (Button) findViewById(R.id.mantener);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setLanguage();

        setTitle("Control por voz");

        createSpeech();

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        askForPermission();
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setIndeterminate(true);
                        if (speech != null) {
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
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 1);

                        } else {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 1);
                        }
                    } else {
                    }
                }
        });
    }


    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        restUrl = SP.getString("restUrl", "192.168.43.156");
        restPort = SP.getString("restPort", "8087/Guestbook/sendMessage");

        restPoster.postVoiceResult(matches, restUrl, restPort, this);

        if (matches.size() >= 1) {
            text = matches.get(1);
            returnedText.setText(text);
        } else {
            returnedText.setText("Listening Service Error");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_new_thingy:
                Intent i = new Intent(this, CustomPreferenceActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void createSpeech() {
        if (speech != null) {
            speech.destroy();
        }
        setLanguage();
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

    private void setLanguage() {
        if("1".equalsIgnoreCase(SP.getString("lang", "1"))){
            lang = SPANISH;
        }else{
            lang = ENGLISH;
        }
    }
    @Override
    public void onError(int errorCode) {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        String errorMessage = getErrorText(errorCode);
        returnedText.setText(errorMessage);
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
    public void onRmsChanged(float rmsdB) {
        progressBar.setProgress((int) rmsdB);
    }
}