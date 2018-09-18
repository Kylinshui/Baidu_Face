package com.bshui.baidu_face;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;


import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class RgbDetectActivity extends Activity {
    // 预览View;
    private PreviewView previewView;
    //用于检测人脸
    private FaceDetectManager faceDetectManager;
    // textureView用于绘制人脸框等。
    private TextureView textureView;
    // 为了方便调式。
    private ImageView testView;
    private Handler handler = new Handler();
    private TextView tipTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgb_detect);

        previewView = (PreviewView)findViewById(R.id.preview_view);
        textureView = (TextureView)findViewById(R.id.texture_view);
        testView = (ImageView) findViewById(R.id.test_view);
        tipTv = (TextView) findViewById(R.id.tip_tv);

        faceDetectManager = new FaceDetectManager(getApplicationContext());
        //从系统相机获取图片帧
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        //640x480   720x480 1280x720 1920x1080
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1920,1080);
        previewView.setMirrored(false);
        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // TODO 选择使用前置摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);
        // TODO 选择使用后置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//        previewView.getTextureView().setScaleX(-1);
        // TODO 选择使用usb摄像头
        //  cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        //设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        faceDetectManager.setUseDetect(true);
        //相机横屏模式
        previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
        cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        textureView.setOpaque(false);

        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);

        //设置回调,回调人脸检测结果
        addListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //开始检测
        faceDetectManager.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //结束检测
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetectManager.stop();
    }
    private void addListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                final Bitmap bitmap =
                        Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        testView.setImageBitmap(bitmap);
                    }
                });
                checkFace(retCode, infos, frame);
               // showFrame(frame, infos);

            }
        });
    }

    private void checkFace(int retCode, FaceInfo[] faceInfos, ImageFrame frame) {
        if ( retCode == FaceTracker.ErrCode.OK.ordinal() && faceInfos != null) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);
            displayTip(tip);
        } else {
            String tip = checkFaceCode(retCode);
            displayTip(tip);
        }
    }
    private String filter(FaceInfo faceInfo, ImageFrame imageFrame) {

        String tip = "";
        if (faceInfo.mConf < 0.6) {
            tip = "人脸置信度太低";
            return tip;
        }

        float[] headPose = faceInfo.headPose;
        if (Math.abs(headPose[0]) > 20 || Math.abs(headPose[1]) > 20 || Math.abs(headPose[2]) > 20) {
            tip = "人脸置角度太大，请正对屏幕";
            return tip;
        }

        int width = imageFrame.getWidth();
        int height = imageFrame.getHeight();
        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.mWidth / (float) height;
        Log.i("liveness_ratio", "ratio=" + ratio);
        if (ratio > 0.6) {
            tip = "人脸离屏幕太近，请调整与屏幕的距离";
            return tip;
        } else if (ratio < 0.2) {
            tip = "人脸离屏幕太远，请调整与屏幕的距离";
            return tip;
        } else if (faceInfo.mCenter_x > width * 3 / 4 ) {
            tip = "人脸在屏幕中太靠右";
            return tip;
        } else if (faceInfo.mCenter_x < width / 4 ) {
            tip = "人脸在屏幕中太靠左";
            return tip;
        } else if (faceInfo.mCenter_y > height * 3 / 4 ) {
            tip = "人脸在屏幕中太靠下" ;
            return tip;
        } else if (faceInfo.mCenter_x < height / 4 ) {
            tip = "人脸在屏幕中太靠上";
            return tip;
        }


            saveFace(faceInfo, imageFrame);




        return tip;
    }

    private String checkFaceCode(int errCode) {
        String tip = "";
        if (errCode == FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal() ) {
//            tip = "未检测到人脸";
        } else if (errCode == FaceTracker.ErrCode.IMG_BLURED.ordinal() ||
                errCode == FaceTracker.ErrCode.PITCH_OUT_OF_DOWN_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.PITCH_OUT_OF_UP_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.YAW_OUT_OF_LEFT_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.YAW_OUT_OF_RIGHT_MAX_RANGE.ordinal())  {
            tip = "请静止平视屏幕";
        } else if (errCode == FaceTracker.ErrCode.POOR_ILLUMINATION.ordinal()) {
            tip = "光线太暗，请到更明亮的地方";
        } else if (errCode == FaceTracker.ErrCode.UNKNOW_TYPE.ordinal()){
            tip =  "未检测到人脸";
        }
        return tip;
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTv.setText(tip);
            }
        });
    }

    private void saveFace(FaceInfo faceInfo, ImageFrame imageFrame) {
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());

            try {
                // 其他来源保存到临时目录
                final File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                // 人脸识别不需要整张图片。可以对人脸区别进行裁剪。减少流量消耗和，网络传输占用的时间消耗。
                ImageUtils.resize(bitmap, file, 300, 300);Intent intent = new Intent();
                intent.putExtra("file_path", file.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            }

    }


}
