package com.bshui.baidu_face.utils;

public class GlobalFaceTypeModel {
    public static final String TYPE_LIVENSS = "TYPE_LIVENSS";
    public static final int TYPE_NO_LIVENSS = 1;
    public static final int TYPE_RGB_LIVENSS = 2;
    public static final int TYPE_RGB_IR_LIVENSS = 3;
    public static final int TYPE_RGB_DEPTH_LIVENSS = 4;
    public static final int TYPE_RGB_IR_DEPTH_LIVENSS = 5;

    // 模型的选择
    public static final String TYPE_MODEL = "TYPE_MODEL";
    public static final int RECOGNIZE_LIVE = 1;
    public static final int RECOGNIZE_ID_PHOTO = 2;

    // 摄像头的选择
    public static final String TYPE_CAMERA = "TYPE_CAMERA";
    public static final int ORBBEC = 1;
    public static final int IMIMECT = 2;
    public static final int ORBBECPRO = 3;

    // 摄像头的选择
    public static final String TYPE_THREAD = "TYPE_THREAD";
    public static final int SINGLETHREAD = 1;
    public static final int MULTITHREAD = 2;


}
