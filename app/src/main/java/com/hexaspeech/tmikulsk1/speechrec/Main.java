package com.hexaspeech.tmikulsk1.speechrec;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class Main extends AppCompatActivity {

    //CONFIGS
    public static int OUTPORT = 60500;
    public static String DEVICEIP = "192.168.1.191";
    public static boolean HASNEWCONFIG = false;
    public boolean ISCONFIGURED = false;
    //
    private final int SPEECH_RECOGNITION_CODE = 1;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent intent;
    private TextView outputText;
    private TextView outputText2;
    private Button startSpeech;
    private Button stopSpeech;

    //OSC OBJECTS
    private OSCPortOut oscPortOut;
    //OSC MESSAGE
    private String message = "word/teste 1";
    private ArrayList<Object> val = new ArrayList<>();
    private boolean newMessage = false;
    private boolean speechNotStopped = false;
    private boolean threadOpened = false;
    private boolean threadStop = true;

    public Thread speechOSC = new Thread() {

        public void openConnection() {
            try {

                oscPortOut = new OSCPortOut(InetAddress.getByName(DEVICEIP), OUTPORT);

            } catch (UnknownHostException e) {
                Toast.makeText(getApplicationContext(), "Error1: " + e, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error2: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public void oscOUT() {

            OSCMessage cmdLineOSC = new OSCMessage(message, val);

            try {
                oscPortOut.send(cmdLineOSC);
                newMessage = false;
                sleep(500);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "FALHA: " + e.toString(), Toast.LENGTH_LONG).show();
            }

        }

        public void initSpeech() {

            try {

                speechNotStopped = true;
                mSpeechRecognizer.startListening(intent);

            } catch (Exception e) {

                speechNotStopped = false;
                Toast.makeText(getApplicationContext(), "Dispositivo não suportado", Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        public void run() {

            while (threadStop) {

                if (HASNEWCONFIG) {
                    try {
                        openConnection();
                        HASNEWCONFIG = false;
                    } catch (Exception e) {
                    }
                }
                if (newMessage) {

                    try {
                        oscOUT();
                    } catch (Exception e) {
                    }
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        startSpeech.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (ISCONFIGURED) {

                                    speechNotStopped = true;
                                    stopSpeech.setEnabled(true);
                                    startSpeech.setEnabled(false);
                                    initSpeech();

                                }

                            }
                        });
                        stopSpeech.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                speechNotStopped = false;
                                stopSpeech.setEnabled(false);
                                startSpeech.setEnabled(true);
                                mSpeechRecognizer.stopListening();
                                mSpeechRecognizer.cancel();

                            }
                        });



                    }
                });

            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.configs, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.out_config_menu) {
            startActivity(new Intent(Main.this, OutConfigs.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ISCONFIGURED) {
            initializeOSC();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeOSC();

        outputText = findViewById(R.id.output_text);
        outputText2 = findViewById(R.id.output_text2);
        startSpeech = findViewById(R.id.start_speech);
        stopSpeech = findViewById(R.id.stop_speech);
        stopSpeech.setEnabled(false);
        startSpeech.setEnabled(false);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

                if (speechNotStopped) {
                    initSpeech();
                }

            }

            @Override
            public void onError(int i) {

                if (speechNotStopped) {
                    initSpeech();
                }

            }

            @Override
            public void onResults(Bundle bundle) {

                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                outputText2.setText(matches.get(0).toString());
                if (speechNotStopped) {
                    initSpeech();
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                outputText.setText(matches.get(0).toString());
                int isMatch = matches.get(0).toString().indexOf("testando");
                int isMatch2 = matches.get(0).toString().indexOf("teste");
                if (isMatch > 0) {

                    isMatch = 0;
                    val.clear();
                    message = "/word";
                    val.add(10000);
                    val.add(900000);
                    val.add(30);
                    val.add(1);
                    val.add(1);

                    newMessage = true;
                    mSpeechRecognizer.stopListening();
//
                }else if (isMatch2 > 0) {

                    isMatch2 = 0;
                    val.clear();
                    message = "/word";
                    val.add(770000);
                    val.add(1000000);
                    val.add(20);
                    val.add(1);
                    val.add(1);

                    newMessage = true;
                    mSpeechRecognizer.stopListening();
//
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        };


        mSpeechRecognizer.setRecognitionListener(listener);

    }

    public void initializeOSC() {
        if ((OutConfigs.secondConfirm == true)) {
            HASNEWCONFIG = true;

        }

        if (HASNEWCONFIG) {

            startSpeech.setEnabled(true);
            ISCONFIGURED = true;
            speechOSC.start();



        } else {

            Toast.makeText(getApplicationContext(), "" + "Configure a porta de saída e IP antes!", Toast.LENGTH_SHORT).show();

        }
    }
    public void initSpeech() {

        try {

            speechNotStopped = true;
            mSpeechRecognizer.startListening(intent);

        } catch (Exception e) {

            speechNotStopped = false;
            Toast.makeText(getApplicationContext(), "Dispositivo não suportado", Toast.LENGTH_SHORT).show();

        }

    }

}