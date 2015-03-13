package ru.toxuin.sellflip.library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class Utils {
    public static final String TAG = "Utils";
    public static List<String> fileNames = new ArrayList<>();
    public static Handler saveFileHandler;
    public static String videoName; // TODO: remove it

    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static void removeTempFiles() {
        for (String file : fileNames) {
            new File(file).delete();
            Log.d(TAG, "removed " + file);
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
            return new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");

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

    public static byte[] createSha1(String file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = new BufferedInputStream(new FileInputStream(file));
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        fis.close();
        return digest.digest();
    }

    public static String getSha1(String filename) throws Exception {
        byte[] b = createSha1(filename);
        String result = "";

        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * @return String containing the filename of a merged Video
     * Saves the final video in the public directory for videos on the device
     */
    public static String mergeVideos(Context context) {

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return outFile.toString();
    }

    public static void mergeAsync(final Context context) {
        if (saveFileHandler == null) saveFileHandler = new Handler();
        new Thread(new Runnable() {
            @Override public void run() {
                final String filePath = Utils.mergeVideos(context);
                saveFileHandler.post(new Runnable() {
                    @Override public void run() {
                        SuperToast superToast = new SuperToast(context, Style.getStyle(Style.BLUE, SuperToast.Animations.POPUP));
                        superToast.setDuration(SuperToast.Duration.LONG);
                        superToast.setText("Video saved in: " + filePath);
                        superToast.setIcon(SuperToast.Icon.Dark.SAVE, SuperToast.IconPosition.LEFT);
                        superToast.show();
                        videoName = filePath; // TODO: remove it
                    }
                });
            }
        }).start();
    }

    private static boolean checkStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static Bitmap getVideoFrame(String videoName, long time) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoName);
            return retriever.getFrameAtTime(time);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            retriever.release();
        }
        return null;
    }
}
