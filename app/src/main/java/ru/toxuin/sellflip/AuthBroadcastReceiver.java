package ru.toxuin.sellflip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AuthBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BaseActivity.setContent(new LogInFragment());
    }
}
