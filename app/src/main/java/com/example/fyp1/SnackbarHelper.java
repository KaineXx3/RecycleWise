package com.example.fyp1;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public final class SnackbarHelper {
    private static final int BACKGROUND_COLOR = 0xbf323232;
    private static final String TAG = "SnackbarHelper";
    private Snackbar messageSnackbar;
    private enum DismissBehavior { HIDE, SHOW, FINISH }
    private int maxLines = 5;
    private String lastMessage = "";
    private View snackbarView;

    public boolean isShowing() {
        return messageSnackbar != null;
    }

    public void showMessage(Activity activity, String message) {
        if (!message.isEmpty() && (!isShowing() || !lastMessage.equals(message))) {
            Log.d(TAG, "Showing message: " + message);
            lastMessage = message;
            show(activity, message, DismissBehavior.HIDE);
        } else {
            Log.d(TAG, "Message not shown (either empty or same as last): " + message);
        }
    }

    public void showMessageWithDismiss(Activity activity, String message) {
        Log.d(TAG, "Showing message with dismiss: " + message);
        show(activity, message, DismissBehavior.SHOW);
    }

    public void showMessageForShortDuration(Activity activity, String message) {
        Log.d(TAG, "Showing message for short duration: " + message);
        show(activity, message, DismissBehavior.SHOW, Snackbar.LENGTH_SHORT);
    }

    public void showMessageForLongDuration(Activity activity, String message) {
        Log.d(TAG, "Showing message for long duration: " + message);
        show(activity, message, DismissBehavior.SHOW, Snackbar.LENGTH_LONG);
    }

    public void showError(Activity activity, String errorMessage) {
        Log.d(TAG, "Showing error message: " + errorMessage);
        show(activity, errorMessage, DismissBehavior.FINISH);
    }

    public void hide(Activity activity) {
        if (!isShowing()) {
            return;
        }
        lastMessage = "";
        Snackbar messageSnackbarToHide = messageSnackbar;
        messageSnackbar = null;
        activity.runOnUiThread(() -> {
            if (messageSnackbarToHide != null) {
                messageSnackbarToHide.dismiss();
            }
        });
    }

    public void setMaxLines(int lines) {
        maxLines = lines;
    }

    public void setParentView(View snackbarView) {
        this.snackbarView = snackbarView;
    }

    private void show(Activity activity, String message, DismissBehavior dismissBehavior) {
        show(activity, message, dismissBehavior, Snackbar.LENGTH_INDEFINITE);
    }

    private void show(Activity activity, String message, DismissBehavior dismissBehavior, int duration) {
        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) {
                Log.e(TAG, "Cannot show Snackbar, activity is finishing or destroyed.");
                return;
            }

            if (isShowing()) {
                Log.d(TAG, "Dismissing current Snackbar.");
                messageSnackbar.dismiss();
                messageSnackbar = null;
            }

            View parentView = snackbarView != null ? snackbarView : activity.findViewById(android.R.id.content);
            if (parentView == null) {
                Log.e(TAG, "Parent view is null, cannot show Snackbar.");
                return;
            }

            messageSnackbar = Snackbar.make(parentView, message, duration);
            messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);

            if (dismissBehavior != DismissBehavior.HIDE && duration == Snackbar.LENGTH_INDEFINITE) {
                messageSnackbar.setAction("Dismiss", v -> {
                    Log.d(TAG, "Snackbar dismissed manually.");
                    messageSnackbar.dismiss();
                });

                if (dismissBehavior == DismissBehavior.FINISH) {
                    messageSnackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            Log.d(TAG, "Snackbar dismissed, finishing activity.");
                            activity.finish();
                        }
                    });
                }
            }

            TextView snackbarText = messageSnackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            if (snackbarText != null) {
                snackbarText.setMaxLines(maxLines);
            } else {
                Log.e(TAG, "Snackbar text view not found.");
            }

            Log.d(TAG, "Showing Snackbar with message: " + message);
            messageSnackbar.show();
        });
    }
}
