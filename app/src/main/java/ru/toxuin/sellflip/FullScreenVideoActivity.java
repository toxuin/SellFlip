package ru.toxuin.sellflip;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.MediaController;
import android.widget.VideoView;


public class FullScreenVideoActivity extends ActionBarActivity {
    private VideoView videoView;
    private ProgressDialog progressDialog;

    private int position;
    private boolean retrieved;

    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);
        videoView = (VideoView) findViewById(R.id.videoView);

        if (mediaController == null) mediaController = new MediaController(this);
        if (!retrieved)
            position = getIntent().getIntExtra("position", 0); // get position from the caller
        retrieved = true;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading the video...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(SingleAdFragment.VIDEO_URL));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override public void onPrepared(MediaPlayer mp) {
                    progressDialog.dismiss();
                    progressDialog = null;
                    videoView.requestFocus();
                    if (position != 0) videoView.seekTo(position);
                    videoView.start();
                }
            });
        } catch (Exception e) { // TODO: handle it properly
            e.printStackTrace();
        }

    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", videoView.getCurrentPosition());
        videoView.pause();
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt("position");
    }
}