package ru.toxuin.sellflip.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;

public class PrivacyPolicyFragment extends Fragment {
    public static final String TAG = "PolicyDialog";

    private View rootView;
    private ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Please wait Loading...");
        pd.show();

        final WebView webView = (WebView) rootView.findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                if (progress == 100) {
                    pd.dismiss();
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.RED, SuperToast.Animations.POPUP));
                superToast.setDuration(SuperToast.Duration.SHORT);
                superToast.setText("Oh no: " + description);
                superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                superToast.show();
            }
        });

        webView.loadUrl(SellFlipSpiceService.API_ENDPOINT_URL + "/policy_mobile");
        return rootView;
    }

}
