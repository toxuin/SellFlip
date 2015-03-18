package ru.toxuin.sellflip;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import java.io.IOException;

import ru.toxuin.sellflip.library.Utils;

public class CaptureVideoFragment extends Fragment implements SurfaceHolder.Callback {
    public static final String TAG = "CaptureVideoFrag";
    public static int VIDEO_MINIMUM_LENGTH = 10;
    public static int VIDEO_MAXIMUM_LENGTH = 15;

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private ProgressBar progressBar;

    private View rootView;

    public CaptureVideoFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_capture_video, container, false);

        final Button capture = (Button) rootView.findViewById(R.id.button_capture);
        final FontAwesomeText nextArrowBtn = (FontAwesomeText) rootView.findViewById(R.id.nextArrowBtn);
        final FontAwesomeText closeXBtn = (FontAwesomeText) rootView.findViewById(R.id.closeXBtn);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mPreview = (SurfaceView) rootView.findViewById(R.id.surface_preview);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        progressBar.setProgress(0);

        final Handler progressHandler = new Handler();
        final Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getProgress() + 1);

                if (progressBar.getProgress() >= progressBar.getMax()) {  // user has reached the limit
                    /*
                    * Stop recording, release resources and write to the file
                    * */
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    mCamera.lock();
                    capture.setText("Capture");
                    isRecording = false;
                    progressHandler.removeCallbacks(this);
                    Utils.mergeAsync(getActivity());
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
                            releaseMediaRecorder(); // release the MediaRecorder object
                            mCamera.lock();         // take camera access back from MediaRecorder
                            closeXBtn.setEnabled(true);
                            // inform the user that recording has stopped
                            capture.setText("Capture");
                            isRecording = false;
                            progressHandler.removeCallbacks(progressRunnable);
                        } else {
                            prepareVideoRecorder();
                            mMediaRecorder.start();
                            // inform the user that recording has started
                            capture.setText("Stop");
                            isRecording = true;
                            closeXBtn.setEnabled(false);
                            progressHandler.postDelayed(progressRunnable, 0);
                        }
                    }
                }
        );

        nextArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (progressBar.getProgress() > VIDEO_MINIMUM_LENGTH) { // user has minimum length
                    String filename = Utils.mergeVideos(getActivity());
                    CreateAdFragment createAdFragment = new CreateAdFragment();
                    Bundle args = new Bundle();
                    args.putString("filename", filename);
                    createAdFragment.setArguments(args);
                    BaseActivity.setContent(createAdFragment);
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

        return rootView;
    }

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //TODO: tweak video profile
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
//        mMediaRecorder.setVideoFrameRate(25);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(Utils.createTempFile("video", ".mp4").toString());
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        mMediaRecorder.setOrientationHint(90);
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

    @Override public void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    @Override public void onResume() {
        super.onResume();
        Utils.toggleFullScreen(getActivity());
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

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // start preview with new settings
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override public void onDestroy() {
        super.onDestroy();
        Utils.removeTempFiles();
        Utils.toggleFullScreen(getActivity());
    }
}
