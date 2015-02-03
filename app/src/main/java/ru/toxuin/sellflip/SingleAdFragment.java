package ru.toxuin.sellflip;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class SingleAdFragment extends Fragment {
    private View rootView;

    public SingleAdFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_singlead, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        // Testing video
        VideoView videoView = (VideoView) rootView.findViewById(R.id.videoView);
        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.small;
        videoView.setVideoURI(Uri.parse(path));
        videoView.requestFocus();
        videoView.setMediaController(new MediaController(getActivity()));
        videoView.start();

        return rootView;
    }
}
