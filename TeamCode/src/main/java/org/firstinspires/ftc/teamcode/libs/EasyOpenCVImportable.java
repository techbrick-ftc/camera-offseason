// A simple computer vision algorithm

package org.firstinspires.ftc.teamcode.libs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.tests.RobotConfig;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;

public class EasyOpenCVImportable {
    private OpenCvCamera webCamera;
    private OpenCvInternalCamera phoneCamera;

    private UltimateGoalDetectionPipeline pipeline;
    private boolean detecting;

    private static int box1PosX;
    private static int box1PosY;
    private static int box2PosX;
    private static int box2PosY;
    private static int width;
    private static int height;

    public void init(CameraType cameraType, final HardwareMap hardwareMap, int box1PosX, int box1PosY, int box2PosX, int box2PosY, int width, int height) {
        if (cameraType.equals(CameraType.WEBCAM)) {
            initWebcam(hardwareMap, "Webcam 1");
        } else {
            initPhone(hardwareMap);
        }
        EasyOpenCVImportable.box1PosX = box1PosX;
        EasyOpenCVImportable.box1PosY = box1PosY;
        EasyOpenCVImportable.box2PosX = box2PosX;
        EasyOpenCVImportable.box2PosY = box2PosY;
        EasyOpenCVImportable.width = width;
        EasyOpenCVImportable.height = height;
    }

    public void initWebcam(final HardwareMap hardwareMap, final String webcamName) {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        this.webCamera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, webcamName), cameraMonitorViewId);
        this.detecting = false;
    }

    public void initPhone(final HardwareMap hardwareMap) {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        this.phoneCamera = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        this.phoneCamera.setViewportRenderingPolicy(OpenCvCamera.ViewportRenderingPolicy.OPTIMIZE_VIEW);

    }

    public void startDetection() {
        this.pipeline = new UltimateGoalDetectionPipeline();
        if (this.phoneCamera == null) {
            this.webCamera.setPipeline(this.pipeline);

            this.webCamera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
                @Override
                public void onOpened() {
                    webCamera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
                }

                @Override
                public void onError(int errorCode) {

                }
            });
            this.detecting = true;
        } else {
            this.phoneCamera.setPipeline(this.pipeline);

            this.phoneCamera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
                @Override
                public void onOpened() {
                    phoneCamera.startStreaming(320, 240, OpenCvCameraRotation.SIDEWAYS_LEFT);
                }

                @Override
                public void onError(int errorCode) {

                }
            });
        }
    }

    public void stopDetection() {
        if (this.phoneCamera == null) {
            this.webCamera.stopStreaming();
        } else {
            this.phoneCamera.stopStreaming();
        }
        this.detecting = false;
    }

    public OpenCvCamera getWebCamera() { return this.webCamera; }

    public OpenCvInternalCamera getPhoneCamera() { return this.phoneCamera; }

    public int getDetection() { return this.pipeline.position; }

    public boolean getDetecting() { return this.detecting; }

    public int[] getAnalysis() { return new int[]{this.pipeline.sum1, this.pipeline.sum2}; }

    private static class UltimateGoalDetectionPipeline extends OpenCvPipeline {


        // Color constants
        static final Scalar BLUE = new Scalar(0, 0, 255);
        static final Scalar GREEN = new Scalar(0, 255, 0);

        // Core values for position and size
        final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(RobotConfig.box1x, RobotConfig.box1y);
        final Point REGION2_TOPLEFT_ANCHOR_POINT = new Point(RobotConfig.box2x, RobotConfig.box2y);

        static final int REGION_WIDTH = width;
        static final int REGION_HEIGHT = height;

        Point region1_pointA = new Point(
                REGION1_TOPLEFT_ANCHOR_POINT.x,
                REGION1_TOPLEFT_ANCHOR_POINT.y);
        Point region1_pointB = new Point(
                REGION1_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                REGION1_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);

        Point region2_pointA = new Point(
                REGION2_TOPLEFT_ANCHOR_POINT.x,
                REGION2_TOPLEFT_ANCHOR_POINT.y);
        Point region2_pointB = new Point(
                REGION2_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                REGION2_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);

        // Working variables
        Mat region1_Cb;
        Mat region2_Cb;
        Mat YCrCb = new Mat();
        Mat Cb = new Mat();
        int sum1;
        int sum2;

        private volatile int position = -1;

        /*
            This take the RGB frame and converts it to YCrCb,
            and extracts the Cb channel to the "Cb" variable
         */
        void inputToCb(Mat input) {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cb, 0);
        }

        @Override
        public void init(Mat firstFrame) {
            inputToCb(firstFrame);
            region1_Cb = Cb.submat(new Rect(region1_pointA, region1_pointB));
            region2_Cb = Cb.submat(new Rect(region2_pointA, region2_pointB));
        }

        @Override
        public Mat processFrame(Mat input) {
            inputToCb(input);

            sum1 = (int) Core.mean(region1_Cb).val[0];
            sum2 = (int) Core.mean(region2_Cb).val[0];

            if (sum1 < 110) {
                this.position = 0;
            } else if (sum2 < 110) {
                this.position = 1;
            } else if (sum1 > 78 && sum2 > 78) {
                this.position = 2;
            }

            Imgproc.rectangle(
                    input,
                    region1_pointA,
                    region1_pointB,
                    BLUE, 2);

            Imgproc.rectangle(
                    input,
                    region2_pointA,
                    region2_pointB,
                    GREEN, 2);

            return input;
        }
    }

    public enum CameraType {
        PHONE,
        WEBCAM
    }
}