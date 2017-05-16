package com.justinwei.purifiedwater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import static android.R.attr.width;
import static com.justinwei.purifiedwater.R.attr.height;

public class ImageActivity extends AppCompatActivity {

    Mat img;
    Bitmap bMap;
    ImageView imagePicture;
    private static String TAG = "ImageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        try {
            img = Utils.loadResource(this, R.drawable.shadetest, Imgcodecs.CV_LOAD_IMAGE_COLOR);
            Log.e("IMAGETEST", "Image loaded.");
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGRA);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //img = Utils.bitmapToMat(bMap, img);
        JWOpenCvUtilities.skinDetection(img);

        bMap = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bMap);
        imagePicture =((ImageView)findViewById(R.id.imagePicture));
        imagePicture.setImageBitmap(bMap);
        Log.e("IMAGETEST", "Activity loaded.");
    }

    public void applySkinDetectAlgorithm(View view){
        JWOpenCvUtilities.skinDetection(img);
        Utils.matToBitmap(img, bMap);
        imagePicture.setImageBitmap(bMap);

    }
}
