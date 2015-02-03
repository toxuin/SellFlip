package ru.toxuin.sellflip;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class SearchResultFragment extends Fragment {
    private View rootView;
    private VideoView videoView;
    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.singlead_element, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);
        // DO STUFF

        // Testing video
        videoView = (VideoView) rootView.findViewById(R.id.videoView);
        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.small;
        videoView.setVideoURI(Uri.parse(path));
        videoView.requestFocus();
        videoView.setMediaController(new MediaController(getActivity()));
        videoView.start();
        //
        return rootView;
    }
}
