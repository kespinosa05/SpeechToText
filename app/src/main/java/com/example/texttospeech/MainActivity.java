package com.example.texttospeech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.text.similarity.LevenshteinDetailedDistance;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;
    private TextToSpeech t1;

    private TextView textViewIntent;
    private Button btnNext;
    private Button btnPrevious;


    private TextView textViewImportFile;
    private View mainLayout;
    private View resultLayout;
    private TextView textViewDistance;
    private TextView textViewConfidence;


    private List<Data> intents = new ArrayList<>();

    private static final Integer SPEECH_REQUEST_CODE = 100;
    private static final Integer IMPORT_FILE_CODE = 101;
    private static final Integer EXPORT_FILE_CODE = 102;

    int position=0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        textViewIntent = findViewById(R.id.textIntent);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);

        textViewDistance = findViewById(R.id.textViewDistance);
        textViewConfidence = findViewById(R.id.textViewConfidence);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        textViewImportFile = findViewById(R.id.textViewImportFile);
        mainLayout = findViewById(R.id.mainLayout);
        resultLayout = findViewById(R.id.resultLayout);


        //final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the magic word");

        //startActivityForResult(speechRecognizerIntent, SPEECH_REQUEST_CODE);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Setting offline speech recognition to true
            //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        }
        position = 0;
        /*
        intents.add(new Data("Open my calendar to friday"));
        intents.add(new Data("Open my calendar to next"));
        intents.add(new Data("Go to next"));
        intents.add(new Data("Got to home"));
        setIntentText(position+1, position);
        */

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position >= 0 && position < intents.size()-1) {
                    editText.setText("");
                    editText.setHint("Tap to Speak");

                    setIntentText(position+2, ++position);

                    showResult();

                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position > 0 && position <= intents.size()-1) {
                    editText.setText("");
                    editText.setHint("Tap to Speak");

                    setIntentText(position, --position);

                    showResult();
                }
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                editText.setHint("Ready...");
            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                editText.setHint("Completed...");
            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
                editText.setHint("Error..."+i);
                micButton.setImageResource(R.drawable.ic_mic_black_off);
            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                List<String> resultData = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String text = resultData.get(0);
                editText.setText(text);
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                float[] confidence = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                float confidenteData = confidence[0];

                Data data = intents.get(position);
                data.setRecognizedText(text);
                data.setConfidence(confidenteData);

                data.setResult(LevenshteinDetailedDistance.getDefaultInstance().apply(data.getText(), data.getRecognizedText()));

                showResult();
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                System.out.println("onPartialResults:"+ bundle);
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }else{
                    editText.setText("");
                    editText.setHint("Preparing...");
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micButton.setImageResource(R.drawable.ic_mic_red_on);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });
    }

    private void showResult() {
        Data data = intents.get(position);
        if(data.getRecognizedText() == null){
            resultLayout.setVisibility(View.INVISIBLE);
        }else{
            resultLayout.setVisibility(View.VISIBLE);
            textViewDistance.setText(data.getResult().getDistance().toString());
            textViewConfidence.setText(data.getConfidence().toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        System.out.println("click"+item.toString());
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_import:

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                        +  File.separator + "Download" + File.separator);
                //intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setDataAndType(uri, "text/plain");
                //startActivityForResult(Intent.createChooser(intent, "Open folder"), IMPORT_FILE_CODE);
                startActivityForResult(intent, IMPORT_FILE_CODE);

                return true;
            case R.id.menu_item_export:
                System.out.println("click"+item.toString());
                boolean notCompleted = false;
                for (Data i: intents) {
                    if(i.getRecognizedText()==null){
                        notCompleted = true;
                        Toast.makeText(getApplicationContext(),"Some intents are not completed",Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                if(notCompleted){
                    return false;
                }

                Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
                exportIntent.setType("text/plain");
                exportIntent.putExtra(Intent.EXTRA_TITLE, "google_asr_result.txt");

                startActivityForResult(exportIntent, EXPORT_FILE_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
    }

    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }


    public List<String> laterFunction(Uri uri) {
        List<String> result =new ArrayList<>();
        BufferedReader br;
        FileOutputStream os;
        try {
            br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = null;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult:"+ requestCode+"--"+resultCode + "DATA:"+data);
        if (requestCode == SPEECH_REQUEST_CODE) {

        } else if(requestCode == IMPORT_FILE_CODE){
            //Get the text file
            Uri uri = data.getData();
            final List<String> intentTexts = laterFunction(uri);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    intents.clear();
                    position = 0;
                    for (int i = 0; i < intentTexts.size(); i++) {
                        intents.add(new Data(intentTexts.get(i)));
                    }
                    setIntentText(position+1, position);
                    textViewImportFile.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                }
            });
        } else if(requestCode == EXPORT_FILE_CODE){
            try {
                OutputStream os = getBaseContext().getContentResolver().openOutputStream(data.getData());
                if( os != null ) {
                    os.write("Input text; Asr text; Confidence; Distance; InsertCount; DeleteCount; SubstituteCount".getBytes());
                    os.write("\n".getBytes());

                    for (Data i: intents) {
                        os.write((i.getText()+";"+i.getRecognizedText()+";"+i.getConfidence()+";"+i.getResult().getDistance()+";"+i.getResult().getInsertCount()+";"+i.getResult().getDeleteCount()+";"+i.getResult().getSubstituteCount()+";").getBytes());
                        os.write("\n".getBytes());
                    }
                    os.close();
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setIntentText(Integer index, Integer positionDesired) {
        textViewIntent.setText( index+"/"+intents.size() + " - " +  intents.get(positionDesired).getText());
    }
}
