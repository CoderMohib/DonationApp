package com.example.donationapp.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.donationapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * Helper class for showing Material Design dialogs
 * Provides standardized dialogs for errors, success, confirmations, and loading
 */
public class DialogHelper {

    /**
     * Show error dialog
     */
    public static void showErrorDialog(Context context, String title, String message) {
        if (context == null || !(context instanceof Activity)) {
            return;
        }
        
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(title != null ? title : "Error")
                .setMessage(message != null ? message : "An error occurred")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Show error dialog with retry option
     */
    public static void showErrorDialogWithRetry(Context context, String title, String message,
                                                Runnable onRetry) {
        if (context == null || !(context instanceof Activity)) {
            return;
        }
        
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(title != null ? title : "Error")
                .setMessage(message != null ? message : "An error occurred")
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (onRetry != null) {
                        onRetry.run();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Show success dialog
     */
    public static void showSuccessDialog(Context context, String title, String message,
                                       Runnable onDismiss) {
        if (context == null || !(context instanceof Activity)) {
            return;
        }
        
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(title != null ? title : "Success")
                .setMessage(message != null ? message : "Operation completed successfully")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    /**
     * Show confirmation dialog
     */
    public static void showConfirmationDialog(Context context, String title, String message,
                                             Runnable onConfirm) {
        if (context == null || ((Activity) context).isFinishing()) {
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    /**
     * Show delete confirmation dialog
     */
    public static void showDeleteConfirmationDialog(Context context, String itemName,
                                                   Runnable onDelete) {
        if (context == null || ((Activity) context).isFinishing()) {
            return;
        }

        String message = "Are you sure you want to delete \"" + itemName + "\"? This action cannot be undone.";
        
        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Confirmation")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (onDelete != null) {
                        onDelete.run();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Show loading dialog
     * Returns the AlertDialog so it can be dismissed
     */
    public static AlertDialog showLoadingDialog(Context context, String message) {
        if (context == null || !(context instanceof Activity)) {
            return null;
        }
        
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return null;
        }

        // Create custom layout for loading dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_loading, null);
        
        TextView messageView = view.findViewById(R.id.loading_message);
        if (messageView != null && message != null) {
            messageView.setText(message);
        }
        
        // Setup progress animation
        CircularProgressIndicator progressIndicator = view.findViewById(R.id.loading_progress_indicator);
        if (progressIndicator != null) {
            progressIndicator.setProgress(0);
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressIndicator, "progress", 0, 100);
            progressAnimator.setDuration(2500); // 2.5 seconds
            progressAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            progressAnimator.setRepeatMode(ObjectAnimator.RESTART);
            progressAnimator.start();
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();
        
        dialog.show();
        return dialog;
    }

    /**
     * Show simple loading dialog with default message
     */
    public static AlertDialog showLoadingDialog(Context context) {
        return showLoadingDialog(context, "Please wait...");
    }

    /**
     * Show info dialog
     */
    public static void showInfoDialog(Context context, String title, String message) {
        if (context == null || ((Activity) context).isFinishing()) {
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}

