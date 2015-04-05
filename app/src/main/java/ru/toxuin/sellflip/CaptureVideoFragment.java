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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import android.widget.RelativeLayout;
import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.facebook.Session;
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
import ru.toxuin.sellflip.library.views.CameraImageButton;
import ru.toxuin.sellflip.restapi.ApiHeaders;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.AuthRequest;

public class CaptureVideoFragment extends SpiceFragment implements SurfaceHolder.Callback {
    public static final String TAG = "CaptureVideoFrag";
    public static int VIDEO_MINIMUM_LENGTH = 1; // easier debugging
    public static int VIDEO_MAXIMUM_LENGTH = 15;
    private static int lookingDegrees = 90;

    private Camera mCamera;
    private static int finalDegree = -1;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private ProgressBar progressBar;
    private ProgressBar progressBarSecond;

    private View rootView;
    private CameraImageButton capture;
    private MagneticOrientationChangeListener rotationSensorListener;
    private SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);

    LinearLayout rightPanel;
    LinearLayout leftPanel;

    public CaptureVideoFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_capture_video, container, false);

        final FontAwesomeText nextArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.nextArrowBtn);
        final FontAwesomeText nextArrowBtnSecond = (FontAwesomeText) rootView.findViewById(R.id.nextArrowBtn_second);
        final FontAwesomeText closeXBtn = (FontAwesomeText) rootView.findViewById(R.id.closeXBtn);
        final FontAwesomeText closeXBtnSecond = (FontAwesomeText) rootView.findViewById(R.id.closeXBtn_second);
        //final FontAwesomeText recordIndicator = (FontAwesomeText) rootView.findViewById(R.id.recordIndicator);

        capture = (CameraImageButton) rootView.findViewById(R.id.button_capture);
        capture.beep();

        capture.setEnabled(false);
        nextArrowBtn.setEnabled(false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBarSecond = (ProgressBar) rootView.findViewById(R.id.progressBar_second);
        mPreview = (SurfaceView) rootView.findViewById(R.id.surface_preview);
        mHolder = mPreview.getHolder();
        progressBar.setProgress(0);

        rightPanel = (LinearLayout) rootView.findViewById(R.id.camera_right_panel);
        leftPanel = (LinearLayout) rootView.findViewById(R.id.camera_left_panel);

        // CHECK AUTH
        AuthRequest authRequest = new AuthRequest(Session.getActiveSession().getAccessToken()); // need to pass FB token
        spiceManager.execute(authRequest, new RequestListener<AuthRequest.AccessToken>() {
            // WE DO NOTHING SINCE THERE WILL BE A BROADCAST IF UNAUTHORIZED
            @Override
            public void onRequestSuccess(AuthRequest.AccessToken accessToken) {
                Log.d(TAG, "*** AUTH SUCCESS ***");
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                Log.d(TAG, "*** AUTH FAILED ***");
            }
        });
        mHolder.addCallback(this);

        final Handler progressHandler = new Handler();
        final Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getProgress() + 1);
                progressBarSecond.setProgress(progressBarSecond.getProgress() + 1);

                if (progressBar.getProgress() >= VIDEO_MINIMUM_LENGTH) {
                    nextArrowBtn.setEnabled(true);
                    nextArrowBtnSecond.setEnabled(true);
                }

                if (progressBar.getProgress() >= progressBar.getMax() && isRecording) {  // user has reached the limit
                    /*
                    * Stop recording, release resources and write to the file
                    * */
                    if (mMediaRecorder == null) return;
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    capture.beep(false);
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
                capture.beep();
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
                        } else {
                            mCamera.stopPreview();
                            prepareVideoRecorder();
                            mMediaRecorder.start();
                            // inform the user that recording has started
                            isRecording = true;
                            closeXBtn.setEnabled(false);
                            progressHandler.postDelayed(progressRunnable, 0);
                            capture.beep(false);
                        }
                    }
                }
        );

        View.OnClickListener nextListener = new View.OnClickListener() {
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
        };
        nextArrowBtn.setOnClickListener(nextListener);

        View.OnClickListener closeListener = new View.OnClickListener() {
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
        };
        closeXBtn.setOnClickListener(closeListener);
        closeXBtnSecond.setOnClickListener(closeListener);

        // ORIENTATION STUFF
        rotationSensorListener = new MagneticOrientationChangeListener(getActivity().getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                setCameraDisplayOrientation(orientation);
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

        //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
//        mMediaRecorder.setVideoFrameRate(25);

        // Step 4: Set output file
        File tempFile = Utils.createTempFile("video", ".mp4");
        if (tempFile == null) return false;
        mMediaRecorder.setOutputFile(tempFile.getAbsolutePath());
        // Step 5: Set the preview output

        if (finalDegree == -1) finalDegree = lookingDegrees;
        if (finalDegree == -1) finalDegree = 90; // if still -1
        rotationSensorListener.disable();
        mMediaRecorder.setOrientationHint(finalDegree-90);

        //mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

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

    public void setCameraDisplayOrientation(int degrees) {
        if (mCamera == null) return;
        if (degrees < 0) return;
        int result;
        if (degrees >= 0 && degrees < 45) {
            result = 0;
        } else if (degrees >= 45 && degrees < 135) {
            //result = 90;
            result = 270;
        } else if (degrees >= 135 && degrees < 225) {
            result = 180;
        } else if (degrees >= 225 && degrees < 315) {
            //result = 270;
            result = 90;
        } else result = 0;
        if (lookingDegrees != result) changeLayout(result);

        lookingDegrees = result;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        releaseMediaRecorder();
        BaseActivity.showActionBar();
        if (rotationSensorListener != null) rotationSensorListener.disable();
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setFullScreen(getActivity(), true);
        BaseActivity.hideActionBar();
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

        int offset = rightPanel.getHeight();
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -offset);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(0);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        rightPanel.startAnimation(anim);
        capture.setEnabled(true);
        try {
            mCamera.setPreviewDisplay(holder);
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

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override public void onDestroy() {
        super.onDestroy();
        Utils.removeTempFiles();
        Utils.setFullScreen(getActivity(), false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    int wasAngle = 0;
    int sidePanelHeight = 0;
    boolean isRightPanelVisible = false;
    boolean isLeftPanelVisible = true;
    private void changeLayout(final int angle) {
    /*           270 (-90)
               |------------|
           180 |            | 0
         (-180)|____________|
                  90 (-270)            */

        //Log.d(TAG, "WAS ANGLE: " + wasAngle + ", NOW ANGLE: " + angle);

        if (capture != null) {
            boolean clockWise = true;
            if (wasAngle < angle) clockWise = false;
            if (wasAngle == 270 && angle == 0) clockWise = false;
            if (wasAngle == 0 && angle == 270) clockWise = true;

            int toAngle = angle - 90;
            int fromAngle = wasAngle -90;

            if (fromAngle == toAngle && fromAngle == -90) fromAngle = 0; // INITIAL CONDITION, ROTATED CW
            if (fromAngle == -90 && toAngle == 90) fromAngle = 0;  // INITIAL CONDITION, ROTATED CCW
            if (clockWise && fromAngle == -90 && toAngle == 180) {
                fromAngle = 270;
            } else if (!clockWise && fromAngle == 180 && toAngle == -90) {
                toAngle = 270;
            }
            RotateAnimation anim = new RotateAnimation(fromAngle, toAngle,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(300);
            anim.setFillEnabled(true);
            anim.setFillAfter(true);
            capture.startAnimation(anim);
        }







        if (rightPanel != null) {
            sidePanelHeight = rightPanel.getHeight();
            Animation anim = null;
            if (angle == 270 && !isRightPanelVisible) {
                anim = new TranslateAnimation(0, 0, -sidePanelHeight, 0);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(300);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {
                        rightPanel.setVisibility(View.VISIBLE);
                        isRightPanelVisible = true;
                    }
                    @Override public void onAnimationEnd(Animation animation) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rightPanel.getLayoutParams();
                        params.setMargins(0, 0, 0, 0);
                        rightPanel.setLayoutParams(params);
                        rightPanel.requestLayout();
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
            } else if (isRightPanelVisible) {
                anim = new TranslateAnimation(0, 0, 0, -sidePanelHeight);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(300);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {
                        rightPanel.setVisibility(View.INVISIBLE);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rightPanel.getLayoutParams();
                        params.setMargins(0, -sidePanelHeight, 0, 0);
                        rightPanel.setLayoutParams(params);
                        rightPanel.requestLayout();
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                isRightPanelVisible = false;
            }
            if (anim != null) rightPanel.startAnimation(anim);
        }







        if (leftPanel != null) {
            Animation anim = null;
            if (angle == 270 && isLeftPanelVisible) {
                anim = new TranslateAnimation(0, 0, 0, sidePanelHeight);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(300);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {
                        leftPanel.setVisibility(View.INVISIBLE);
                        isLeftPanelVisible = false;
                    }
                    @Override public void onAnimationEnd(Animation animation) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftPanel.getLayoutParams();
                        params.setMargins(0, 0, 0, -sidePanelHeight);
                        leftPanel.setLayoutParams(params);
                        leftPanel.requestLayout();
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
            } else if (!isLeftPanelVisible) {
                anim = new TranslateAnimation(0, 0, sidePanelHeight, 0);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(300);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        leftPanel.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftPanel.getLayoutParams();
                        params.setMargins(0, 0, 0, 0);
                        leftPanel.setLayoutParams(params);
                        leftPanel.requestLayout();
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                isLeftPanelVisible = true;
            }
            if (anim != null) leftPanel.startAnimation(anim);
        }




        wasAngle = angle;
    }
}
