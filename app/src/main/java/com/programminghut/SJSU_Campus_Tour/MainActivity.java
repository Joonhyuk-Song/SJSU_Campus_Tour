package com.programminghut.SJSU_Campus_Tour;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Bitmap bitmap;
    private String responseBody;
    private Yolov5TFLiteDetector yolov5TFLiteDetector;
    private Paint boxPaint = new Paint();
    private Paint textPaint = new Paint();
    private TextureView textureView;
    private ImageView imageView;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private HandlerThread handlerThread;
    private Handler handler;
    private TextToSpeech textToSpeech;
    private String recognizedText = ""; // Variable to store recognized text
    private SpeechRecognizer speechRecognizer;
    private static final int REQUEST_CODE_VOICE_INPUT = 1000;
    private boolean isCameraInUse = false;
    private OkHttpClient client;

    private Button button;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    getPermission();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();
        getPermission();
        button = findViewById(R.id.button);

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onResults(Bundle results) {
                // Get the speech recognition result
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    recognizedText = matches.get(0);
                    Log.d("RecognizedText", "Recognized text: " + recognizedText);
                    // Save or use the text as needed
                    Toast.makeText(MainActivity.this, "Recognized: " + recognizedText, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

//CAMERA
        handlerThread = new HandlerThread("videoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        textureView = findViewById(R.id.textureView);
        imageView = findViewById(R.id.imageView);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // Check if the camera is already in use
                if (!isCameraInUse) {
                    openCamera(); // Open the camera only if it's not in use
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // Get the bitmap from the texture view every frame
                bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    runModelOnLiveFrame(bitmap, surface);  // Run inference on the camera frame
                }
            }
        });

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("yolov5s-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this);

        boxPaint.setStrokeWidth(2);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        textPaint.setTextSize(10);
        textPaint.setColor(Color.GREEN);
        textPaint.setStyle(Paint.Style.FILL);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set the language for TextToSpeech
                    int langResult = textToSpeech.setLanguage(Locale.US);
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



// OD PART
    private void runModelOnLiveFrame(Bitmap frame, SurfaceTexture surfaceTexture) {
        // Detect objects in the current camera frame
        frame = Bitmap.createScaledBitmap(frame, yolov5TFLiteDetector.getInputSize().getWidth(), yolov5TFLiteDetector.getInputSize().getHeight(), true);
        ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(frame);

        // Create a mutable copy of the original frame to draw bounding boxes and labels
        Bitmap mutableBitmap = frame.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        // Draw bounding boxes and labels for detected objects
        for (Recognition recognition : recognitions) {
            if (recognition.getConfidence() > 0.4) {
                RectF location = recognition.getLocation();
                canvas.drawRect(location, boxPaint);
                canvas.drawText(recognition.getLabelName() + ": " + recognition.getConfidence(),
                        location.left, location.top, textPaint);
            }
        }

        // Set the modified bitmap (with bounding boxes and labels) to the ImageView
        imageView.setImageBitmap(mutableBitmap);
    }


    // BUTTON PART
    public void ask(View v) {
        Button button = (Button) v;

        if (button.getText().toString().equals("Start")) {
            button.setText("End");

            // Start speech recognition programmatically
            speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));

        } else {
            button.setText("Start");

            if (speechRecognizer != null) {
                speechRecognizer.stopListening();  // Stop voice recognition
                Toast.makeText(this, "Voice recognition stopped.", Toast.LENGTH_SHORT).show();
                if (!recognizedText.isEmpty()) {
                    sendTextToServer(recognizedText);

                    // Show waiting message to user
                    Toast.makeText(this, "Processing request, please wait...", Toast.LENGTH_LONG).show();

                    // Wait 60 seconds before requesting response
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Get the response from the server and speak it
                            serverToApp(new ServerResponseCallback() {
                                @Override
                                public void onResponseReceived(String responseBody) {
                                    speakText(responseBody);  // Speak the server response
                                }
                            });
                        }
                    }, 60000); // 60 seconds in milliseconds
                }
            }
        }
    }

    // Method to fetch the text from Flask server
    public interface ServerResponseCallback {
        void onResponseReceived(String responseBody);
    }

    private void serverToApp(ServerResponseCallback callback) {
        // The URL of your Flask server endpoint
        String url = "http://10.251.31.128:5000/get_text";  // Replace with your Flask server URL

        // Create a request object (No body needed if it's a GET request)
        Request request = new Request.Builder()
                .url(url)  // Corrected URL to get text
                .header("Connection", "keep-alive")  // Ensure keep-alive header
                .build();

        // Send the request asynchronously
        OkHttpClient client = new OkHttpClient();  // Make sure client is defined somewhere

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure (e.g., network issues)
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();  // Get the response body as a string

                    // Log the response body for debugging purposes
                    Log.d("ServerResponse", "Response received: " + responseBody);

                    // Update UI and pass the response to the callback (ensure it's done on the main thread)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onResponseReceived(responseBody);  // Pass the response to the callback
                            }
                        }
                    });
                } else {
                    // Handle unsuccessful response
                    Log.e("ServerResponse", "Request failed with code: " + response.code());
                }
            }
        });
    }

    private void sendTextToServer(String recognizedText) {
        // Create a new thread for the background task
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String response = "";

                try {
                    // Initialize OkHttpClient with custom timeouts
                    // Add exponential backoff retry logic
                    OkHttpClient okhttpClient = new OkHttpClient.Builder()
                            .connectTimeout(120, TimeUnit.SECONDS)
                            .writeTimeout(120, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS)
                            .build();

                    // Prepare the JSON payload
                    String jsonPayload = "{\"text\":\"" + recognizedText + "\"}";

                    // Create the request body with JSON
                    MediaType JSON = MediaType.get("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(jsonPayload, JSON);

                    // Build the request
                    Request request = new Request.Builder()
                            .url("http://10.251.31.128:5000/send_text") // Flask server URL
                            .post(body)
                            .build();

                    // Send the request and get the response
                    Response serverResponse = okhttpClient.newCall(request).execute();

                    // Handle the response
                    if (serverResponse.isSuccessful()) {
                        response = "Text sent successfully!";
                        Log.d("HTTP Success", "Response: " + response);
                    } else {
                        response = "Failed to send text. Response code: " + serverResponse.code();
                        Log.e("HTTP Error", "Response code: " + serverResponse.code());
                    }

                    // Log the server response
                    String responseBody = serverResponse.body() != null ? serverResponse.body().string() : "";
                    Log.d("HTTP Response", "Response: " + responseBody);
                    Log.d("JSON Payload", "Sending: " + jsonPayload);

                    serverResponse.close();  // Close the response

                } catch (IOException e) {
                    e.printStackTrace();
                    response = "Error occurred: " + e.getMessage();
                    Log.e("HTTP Error", "Error: " + e.getMessage());
                }

                // Post the result back to the UI thread
                String finalResponse = response;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Show the result in a Toast message
                        Toast.makeText(MainActivity.this, finalResponse, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Initialize Text-to-Speech

    private void speakText(String text) {
        if (text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);  // Speak the text
        } else {
            Toast.makeText(this, "Received empty text", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        try {
            // Check if the camera is already in use
            if (isCameraInUse) {
                return;  // Exit if the camera is already being used
            }

            isCameraInUse = true; // Mark the camera as in use

            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;

                    SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                    surfaceTexture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
                    Surface surface = new Surface(surfaceTexture);

                    try {
                        CameraCaptureSession.StateCallback captureCallback = new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                try {
                                    // Create the capture request
                                    CaptureRequest.Builder captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    captureRequestBuilder.addTarget(surface);

                                    // Set repeating request, but add a delay for 5 FPS
                                    long frameInterval = 200; // 200ms per frame (5 FPS)
                                    session.setRepeatingRequest(captureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                                        @Override
                                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                            // You can add extra processing here if needed
                                        }
                                    }, handler);

                                    // Use postDelayed to ensure we skip frames and only process one every 200ms
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                // Send the capture request again every 200ms for 5 FPS
                                                session.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
                                            } catch (CameraAccessException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, frameInterval); // Delay between frames (200ms)

                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {}
                        };

                        camera.createCaptureSession(Collections.singletonList(surface), captureCallback, handler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                    isCameraInUse = false; // Mark the camera as no longer in use
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    isCameraInUse = false; // Mark the camera as no longer in use
                }
            }, handler);
        } catch (SecurityException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void getPermission() {
        // Check if the CAMERA permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request CAMERA permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Check if the RECORD_AUDIO (microphone) permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request RECORD_AUDIO permission
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quitSafely();
        if (cameraDevice != null) {
            cameraDevice.close();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();  // Stop speaking
            textToSpeech.shutdown();  // Clean up resources
        }
    }
}
