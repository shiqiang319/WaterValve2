package com.google.zxing.decoding;

import android.app.Activity;
import android.content.DialogInterface;

/**
 * Created by 14592 on 2020/6/5.
 */

public final class FinishListener implements DialogInterface.OnClickListener,DialogInterface.OnCancelListener,Runnable{
    private final Activity activityToFinish;

    public FinishListener(Activity activityToFinish) {
        this.activityToFinish = activityToFinish;
    }

    public void onCancel(DialogInterface dialogInterface) {
        run();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        run();
    }

    public void run() {
        activityToFinish.finish();
    }
}
