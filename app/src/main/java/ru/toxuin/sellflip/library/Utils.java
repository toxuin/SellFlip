package ru.toxuin.sellflip.library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.coremedia.iso.boxes.Container;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.google.gson.Gson;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.entities.Coordinates;


public class Utils {
    public static final String TAG = "Utils";
    public static boolean isFullScreen = false;
    public static List<String> fileNames = new ArrayList<>();
    public static Handler saveFileHandler;

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
            @Override
            public void run() {
                final String filePath = Utils.mergeVideos(context);
                saveFileHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SuperToast superToast = new SuperToast(context, Style.getStyle(Style.BLUE, SuperToast.Animations.POPUP));
                        superToast.setDuration(SuperToast.Duration.LONG);
                        superToast.setText("Video saved in: " + filePath);
                        superToast.setIcon(SuperToast.Icon.Dark.SAVE, SuperToast.IconPosition.LEFT);
                        superToast.show();
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
        Log.d(TAG, "Getting frame from: " + time);
        try {
            retriever.setDataSource(videoName);
            return retriever.getFrameAtTime(time * 1000);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            retriever.release();
        }
        return null;
    }

    public static void saveCoordinatesToPreferences(Context cnt, Coordinates coord, String locationName, String key) {
        SharedPreferences sPref = cnt.getSharedPreferences(cnt.getString(R.string.location_preference_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sPref.edit();

        Set<String> savedLocations = sPref.getStringSet("SAVED_LOCATIONS_KEYS", null);
        if (savedLocations == null) savedLocations = new HashSet<>();

        if (coord == null) {
            edit.remove(key);
            savedLocations.remove(key);
            edit.remove("LOCATION_NAME_" + key);
        } else {
            Gson gson = new Gson();
            String json = gson.toJson(coord);
            edit.putString(key, json);
            savedLocations.add(key);
            edit.putString("LOCATION_NAME_" + key, locationName);
        }
        edit.putStringSet("SAVED_LOCATIONS_KEYS", savedLocations);
        edit.apply();
    }

    public static Coordinates getCoordinatesFromPreferences(Context cnt, String key) {
        SharedPreferences sPref = cnt.getSharedPreferences(cnt.getString(R.string.location_preference_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sPref.getString(key, "");
        return gson.fromJson(json, Coordinates.class);
    }

    public static void toggleFullScreen(Activity activity) {
        if (!isFullScreen) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            isFullScreen = true;
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            isFullScreen = false;
        }
    }

    public static long getVideoDuration(String filename) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filename);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(time) / 1000;
    }

    public static String generateKey(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
