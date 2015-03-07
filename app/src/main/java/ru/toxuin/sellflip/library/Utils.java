package ru.toxuin.sellflip.library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class Utils {
    public static final String TAG = "Utils";
    public static List<String> fileNames = new ArrayList<>();

    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static void removeTempFiles() {
        for (String file : fileNames) {
            new File(file).delete();
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Create a File for saving a final video
     */
    public static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        if (checkStorageWritable()) {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES), "SellFlip");

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");

            fileNames.add(mediaFile.toString());
            return mediaFile;
        }
        return null;

    }

    /**
     * Create a temp File for saving a video chunk
     */
    public static File createTempFile(String part, String ext) {
        if (checkStorageWritable()) {
            File tempDir = Environment.getExternalStorageDirectory();
            tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
            if (!tempDir.exists()) {
                if (!tempDir.mkdir()) {
                    Log.e("MyCameraApp", "failed to create directory");
                    return null;
                }

            }
            try {
                File result = File.createTempFile(part, ext, tempDir);
                fileNames.add(result.toString());
                return result;
            } catch (IOException e) {
                Log.e(TAG, "Error when creating temp file: " + e.getMessage());
            }

        }

        return null;

    }

    /**
     * @return String containing the filename of a merged Video
     * Saves the final video in the public directory for videos on the device
     */
    public static String mergeVideos() {

        Movie video;
        List<Track> videoTracks = new LinkedList<>();
        List<Track> audioTracks = new LinkedList<>();


        for (String videoFile : fileNames) {
            try {
                video = MovieCreator.build(videoFile);
                for (int i = 0; i < video.getTracks().size(); i++) {
                    if (i % 2 == 0)
                        videoTracks.add(video.getTracks().get(i));
                    else
                        audioTracks.add(video.getTracks().get(i));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Movie newVideo = new Movie();
        try {
            if (!videoTracks.isEmpty()) {
                newVideo.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }
            if (!audioTracks.isEmpty()) {
                newVideo.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Container out = new DefaultMp4Builder().build(newVideo);
        File outFile = getOutputMediaFile();
        try {
            FileOutputStream fos = new FileOutputStream(outFile);
            out.writeContainer(fos.getChannel());
            fos.close();
            // TODO: write TOAST to the user
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outFile.toString();
    }

    private static boolean checkStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
