package ru.toxuin.sellflip;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import java.io.File;
import java.io.IOException;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import ru.toxuin.sellflip.library.MagneticOrientationChangeListener;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.library.Utils;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.AuthRequest;

public class CaptureVideoFragment extends SpiceFragment implements SurfaceHolder.Callback {
    public static final String TAG = "CaptureVideoFrag";
    public static int VIDEO_MINIMUM_LENGTH = 1; // easier debugging
    public static int VIDEO_MAXIMUM_LENGTH = 15;
    private static int lookingDegrees = 90;
    private static boolean shouldReorientCamera = true;

    private Camera mCamera;
    private static int cameraId;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private ProgressBar progressBar;

    private View rootView;
    private Button capture;
    private MagneticOrientationChangeListener rotationSensorListener;
    private SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);

    public CaptureVideoFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_capture_video, container, false);

        final FontAwesomeText nextArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.nextArrowBtn);
        final FontAwesomeText closeXBtn = (FontAwesomeText) rootView.findViewById(R.id.closeXBtn);
        final FontAwesomeText recordIndicator = (FontAwesomeText) rootView.findViewById(R.id.recordIndicator);

        capture = (Button) rootView.findViewById(R.id.button_capture);

        capture.setEnabled(false);
        nextArrowBtn.setEnabled(false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mPreview = (SurfaceView) rootView.findViewById(R.id.surface_preview);
        mHolder = mPreview.getHolder();
        progressBar.setProgress(0);

        // CHECK AUTH
        AuthRequest authRequest = new AuthRequest(SellFlipSpiceService.getAuthHeaders().getAccessToken());
        spiceManager.execute(authRequest, new RequestListener<AuthRequest.AccessToken>() {
            // WE DO NOTHING SINCE THERE WILL BE A BROADCAST IF UNAUTHORIZED
            @Override
            public void onRequestSuccess(AuthRequest.AccessToken accessToken) {
                Log.d(TAG, "*** AUTH SUCCESS ***");
                mHolder.addCallback(CaptureVideoFragment.this);
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                Log.d(TAG, "*** AUTH FAILED ***");
            }
        });

        final Handler progressHandler = new Handler();
        final Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getProgress() + 1);

                if (progressBar.getProgress() >= VIDEO_MINIMUM_LENGTH) {
                    nextArrowBtn.setEnabled(true);
                }

                if (progressBar.getProgress() >= progressBar.getMax() && isRecording) {  // user has reached the limit
                    /*
                    * Stop recording, release resources and write to the file
                    * */
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    capture.setText("Capture");
                    isRecording = false;
                    progressHandler.removeCallbacks(this);

                    String filename = Utils.mergeVideos(getActivity());
                    CreateAdFragment createAdFragment = new CreateAdFragment();
                    Bundle args = new Bundle();
                    args.putString("filename", filename);
                    createAdFragment.setArguments(args);
                    BaseActivity.setContent(createAdFragment);
                    // go to the next activity
                }
                progressHandler.postDelayed(this, 500);
            }
        };

        capture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording) {
                            mMediaRecorder.stop();  // stop the recording
                            mMediaRecorder.reset();   // clear recorder configuration
//                            mCamera.lock();         // take camera access back from MediaRecorder
                            closeXBtn.setEnabled(true);
                            // inform the user that recording has stopped
                            isRecording = false;
                            progressHandler.removeCallbacks(progressRunnable);
                            capture.setText("Capture");
                        } else {
                            mCamera.stopPreview();
                            prepareVideoRecorder();
                            mMediaRecorder.start();
                            // inform the user that recording has started
                            isRecording = true;
                            closeXBtn.setEnabled(false);
                            progressHandler.postDelayed(progressRunnable, 0);
                            capture.setText("Stop");
                        }
                    }
                }
        );

        nextArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (progressBar.getProgress() > VIDEO_MINIMUM_LENGTH) { // user has minimum length
                    releaseMediaRecorder();

                    String filename = Utils.mergeVideos(getActivity());
                    CategorySelectFragment content = new CategorySelectFragment();
                    Bundle args = new Bundle();
                    args.putString("filename", filename);
                    content.setArguments(args);
                    BaseActivity.setContent(content);
                } else {
                    // INFORM that the user needs to record more

                    SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.RED, SuperToast.Animations.POPUP));
                    superToast.setDuration(SuperToast.Duration.VERY_SHORT);
                    superToast.setText("Video has to be " + VIDEO_MINIMUM_LENGTH + " seconds short");
                    superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                    superToast.show();
                }

            }
        });

        closeXBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (progressBar.getProgress() > 0) {
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.quit_str)
                            .setMessage(R.string.video_cancel)
                            .setPositiveButton(R.string.yes_str, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }

                            })
                            .setNegativeButton(R.string.no_str, null)
                            .show();

                } else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }

            }
        });

        // ORIENTATION STUFF
        rotationSensorListener = new MagneticOrientationChangeListener(getActivity().getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d(TAG, "ORIENTATION " + orientation);
            }
        };
        rotationSensorListener.enable();

        return rootView;
    }

    private boolean prepareVideoRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //TODO: tweak video profile
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoEncodingBitRate(15000000);

//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
//        mMediaRecorder.setVideoFrameRate(25);

        // Step 4: Set output file
        File tempFile = Utils.createTempFile("video", ".mp4");
        if (tempFile == null) return false;
        mMediaRecorder.setOutputFile(tempFile.getAbsolutePath());
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        mMediaRecorder.setOrientationHint(lookingDegrees);
        // mMediaRecorder.setVideoEncodingBitRate();

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void setCameraDisplayOrientation() {
        if (mCamera == null) return;
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        lookingDegrees = result;
        shouldReorientCamera = false;
        //mCamera.setDisplayOrientation(lookingDegrees);
    }

    @Override
    public void onPause() {
        super.onPause();
        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        releaseMediaRecorder();
        if (rotationSensorListener != null) rotationSensorListener.disable();
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        Utils.toggleFullScreen(getActivity());
        if (rotationSensorListener != null) rotationSensorListener.enable();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Utils.getCameraInstance();
        capture.setEnabled(true);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        if (shouldReorientCamera) setCameraDisplayOrientation();
        // start preview with new settings
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override public void onDestroy() {
        super.onDestroy();
        Utils.removeTempFiles();
        Utils.toggleFullScreen(getActivity());
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static void setCameraId(int cameraId) {
        CaptureVideoFragment.cameraId = cameraId;
    }

    @Override
    public SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
