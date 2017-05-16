package com.justinwei.purifiedwater;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC3;


public class CameraActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "CameraActivity";
    String faceString, eyeString;           //cascade xml file path
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny;
    boolean testFaceDetect = false;
    boolean testJava = true;


    private CascadeClassifier faceDetector = null;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status) { //after cv libraries loaded, do this
            switch(status){
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }

        }
    };


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("skin-detect");
        //System.loadLibrary("MyLibs");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView)findViewById(R.id.textView)).setText(NativeClass.getMessageFromJNI());
        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setMaxFrameSize(270, 144);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableFpsMeter();


        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    public void changeImageActivity(View view) {
        Intent intent = new Intent(this, ImageActivity.class);
        startActivity(intent);
        finish();
    }


    protected void onPause() { //in background, disable view
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();

    }

    @Override
    protected void onDestroy(){ //when closed, disable view
        super.onDestroy();
        if (javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume(){ //use callback to resume camera
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "  OpenCVLoader.initDebug(), not working.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.i(TAG, "  OpenCVLoader.initDebug(), working.");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba = new  Mat(height, width, CvType.CV_8UC4);
        imgGray = new  Mat(height, width, CvType.CV_8UC1); //number of channels
        imgCanny = new  Mat(height, width, CvType.CV_8UC1);
        faceString = JWOpenCvUtilities.getFilePath("haarcascade_frontalface_alt.xml", this);
        eyeString = JWOpenCvUtilities.getFilePath("haarcascade_eye.xml", this);

        faceDetector = new CascadeClassifier(faceString);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        if (!mRgba.empty()) {



            if (testFaceDetect) {
                if (testJava) {
                    JWOpenCvUtilities.faceDetection(mRgba, faceDetector);
                } else {
                    OpencvClass.faceDetection(mRgba.getNativeObjAddr(), faceString, eyeString);
                }
            } else {
                //Imgproc.resize(mRgba,mRgba, new Size(320,280));
                return JWOpenCvUtilities.skinDetection(mRgba);
            }
        }
        return mRgba;
    }

}
