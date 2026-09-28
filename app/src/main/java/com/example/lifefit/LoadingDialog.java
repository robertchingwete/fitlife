package com.example.lifefit;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class LoadingDialog {

    private Dialog dialog;
    private TextView tvLoadingTitle;
    private TextView tvLoadingMessage;

    public LoadingDialog(@NonNull Context context) {
        // Create dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate custom layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);

        // Initialize views
        tvLoadingTitle = view.findViewById(R.id.tvLoadingTitle);
        tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);

        // Set dialog properties
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Set transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * Show the loading dialog with default messages
     */
    public void show() {
        show("Adding Workout", "Please wait while we add your workout...");
    }

    /**
     * Show the loading dialog with custom messages
     */
    public void show(String title, String message) {
        if (tvLoadingTitle != null) {
            tvLoadingTitle.setText(title);
        }
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Update the loading message while dialog is showing
     */
    public void updateMessage(String message) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }
    }

    /**
     * Update the loading title while dialog is showing
     */
    public void updateTitle(String title) {
        if (tvLoadingTitle != null) {
            tvLoadingTitle.setText(title);
        }
    }

    /**
     * Dismiss the loading dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Check if dialog is showing
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}