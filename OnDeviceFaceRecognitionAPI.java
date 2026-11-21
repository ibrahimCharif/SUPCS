package com.example.spucs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

// The AI: the MobileFaceNet model runs on the phone and turns a 112x112 face picture into 128 numbers
// (the face template). Pictures of the same person give similar numbers.
public class OnDeviceFaceRecognitionAPI implements FaceRecognitionAPI {
    private static final int SIZE = 112;
    // Two faces match when the angle between their numbers is under 74.18 degrees (the model's threshold).
    private static final double THRESHOLD = Math.cos(Math.toRadians(74.18));

    private Interpreter model;
    private HashMap<String, float[]> templates = new HashMap<>();
    private int nextNumber = 1;

    public OnDeviceFaceRecognitionAPI(Context context) {
        try {
            model = new Interpreter(loadModel(context, "mobile_facenet.tflite"));
        } catch (IOException | RuntimeException e) {
            model = null;
        }
    }

    @Override
    public String enroll(Bitmap face) {
        String reference = "FACE-" + nextNumber;
        nextNumber++;
        templates.put(reference, getNumbers(face));
        return reference;
    }

    @Override
    public String match(Bitmap face, ArrayList<String> references) {
        float[] numbers = getNumbers(face);
        String best = null;
        double bestSimilarity = THRESHOLD;
        for (String reference : references) {
            double similarity = similarity(numbers, templates.get(reference));
            if (similarity >= bestSimilarity) {
                bestSimilarity = similarity;
                best = reference;
            }
        }
        return best;
    }

    // The blink is checked on the camera screen. Here the picture must not be too dark or too bright.
    @Override
    public boolean checkLiveness(Bitmap face) {
        if (face == null) {
            return false;
        }
        long total = 0;
        for (int y = 0; y < face.getHeight(); y++) {
            for (int x = 0; x < face.getWidth(); x++) {
                int pixel = face.getPixel(x, y);
                total = total + (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
            }
        }
        long average = total / (face.getWidth() * face.getHeight());
        return average > 40 && average < 220;
    }

    @Override
    public void deleteEnrollment(String reference) {
        templates.remove(reference);
    }

    // Runs the model. It takes two pictures, so the same face is given twice.
    private float[] getNumbers(Bitmap face) {
        if (model == null) {
            throw new IllegalStateException("The face recognition model could not be loaded.");
        }
        Bitmap picture = Bitmap.createScaledBitmap(face, SIZE, SIZE, true);
        float[][][][] input = new float[1][3][SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int pixel = picture.getPixel(x, y);
                input[0][0][y][x] = Color.red(pixel) / 255f;
                input[0][1][y][x] = Color.green(pixel) / 255f;
                input[0][2][y][x] = Color.blue(pixel) / 255f;
            }
        }
        float[][] output = new float[2][128];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, output);
        model.runForMultipleInputsOutputs(new Object[]{input, input}, outputs);
        return output[0];
    }

    // Cosine similarity: 1 means the same direction, 0 means unrelated.
    private static double similarity(float[] first, float[] second) {
        double dot = 0;
        double firstLength = 0;
        double secondLength = 0;
        for (int i = 0; i < first.length; i++) {
            dot = dot + first[i] * second[i];
            firstLength = firstLength + first[i] * first[i];
            secondLength = secondLength + second[i] * second[i];
        }
        return dot / (Math.sqrt(firstLength) * Math.sqrt(secondLength));
    }

    // Reads the model file from the app's assets folder.
    private static ByteBuffer loadModel(Context context, String fileName) throws IOException {
        InputStream in = context.getAssets().open(fileName);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] chunk = new byte[16384];
        int read = in.read(chunk);
        while (read != -1) {
            bytes.write(chunk, 0, read);
            read = in.read(chunk);
        }
        in.close();
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.size()).order(ByteOrder.nativeOrder());
        buffer.put(bytes.toByteArray());
        buffer.rewind();
        return buffer;
    }
}
