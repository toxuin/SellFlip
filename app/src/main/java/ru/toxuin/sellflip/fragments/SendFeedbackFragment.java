package ru.toxuin.sellflip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.entities.SendFeedBackForm;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.library.Utils;
import ru.toxuin.sellflip.restapi.AppygramSpiceServices;
import ru.toxuin.sellflip.restapi.spicerequests.SendFeedBackRequest;

public class SendFeedbackFragment extends SpiceFragment {
    protected SpiceManager spiceManager = new SpiceManager(AppygramSpiceServices.class);
    private View rootView;

    @Override public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_send_feedback, container, false);
        final EditText subject = (EditText) rootView.findViewById(R.id.subject);
        final EditText email = (EditText) rootView.findViewById(R.id.emailAddr);
        final BootstrapEditText messageText = (BootstrapEditText) rootView.findViewById(R.id.messageText);
        final BootstrapButton sendBtn = (BootstrapButton) rootView.findViewById(R.id.sendBtn);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                subject.setError(null);
                email.setError(null);
                messageText.setError(null);
                boolean valid = true;


                if (subject.getText().length() == 0) {
                    subject.setError(getString(R.string.error_empty_field));
                    valid = false;
                }
                if (messageText.getText().length() == 0) {
                    messageText.setError(getString(R.string.error_empty_field));
                    valid = false;
                }
                if (email.getText().length() == 0) {
                    email.setError(getString(R.string.error_empty_field));
                    valid = false;
                }

                if (valid) {  // POST the form
                    Utils.hideKeyboard(getActivity());
                    SendFeedBackForm feedBackForm = new SendFeedBackForm(email.getText().toString(),
                            email.getText().toString(), messageText.getText().toString());
                    SendFeedBackRequest request = new SendFeedBackRequest(feedBackForm);
                    spiceManager.execute(request, new RequestListener<String>() {
                        @Override public void onRequestFailure(SpiceException spiceException) {
                            SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.RED, SuperToast.Animations.POPUP));
                            superToast.setDuration(SuperToast.Duration.SHORT);
                            superToast.setText("Oh no, there was an error sending the data" + spiceException);
                            superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                            superToast.show();
                        }

                        @Override public void onRequestSuccess(String aVoid) {
                            getActivity().getSupportFragmentManager().popBackStack();// close this frag
                            SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP));
                            superToast.setDuration(SuperToast.Duration.SHORT);
                            superToast.setText("We received your feedback and will get back to you soon!");
                            superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                            superToast.show();

                        }
                    });
                }

            }
        });


        return rootView;

    }
}
