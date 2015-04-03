package ru.toxuin.sellflip;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import ru.toxuin.sellflip.fragments.NativeCameraFragment;

public class VideoCameraActivity extends FragmentActivity {
    public static final String TAG = "VideoActivity";
    private static final String CAMERA_FRAGMENT_TAG = "CameraFragment";
    private static VideoCameraActivity self;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.activity_camera);
        Fragment cameraFragment;
        if (savedInstanceState == null) {
           cameraFragment = new NativeCameraFragment();
        } else {
            cameraFragment = fragmentManager.findFragmentByTag(CAMERA_FRAGMENT_TAG);
        }

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, cameraFragment, CAMERA_FRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
