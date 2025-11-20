package com.example.donationapp.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Utility class for handling WindowInsets (notch, punch hole camera, system bars)
 * Ensures content doesn't overlap with system UI elements
 */
public class WindowInsetsHelper {

    /**
     * Apply window insets to a view, adding padding to avoid system UI overlap
     * This handles notch, punch hole camera, and system bars
     *
     * @param view The view to apply insets to
     */
    public static void applyWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Apply window insets to a view, but only apply top and bottom padding
     * Useful for full-width layouts that should extend to screen edges horizontally
     *
     * @param view The view to apply insets to
     */
    public static void applyWindowInsetsVertical(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Apply window insets to a view, but only apply top padding
     * Useful for layouts that need to avoid the status bar/notch but extend to bottom
     *
     * @param view The view to apply insets to
     */
    public static void applyWindowInsetsTop(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Apply window insets to a view, but only apply bottom padding
     * Useful for layouts that need to avoid the navigation bar but extend to top
     *
     * @param view The view to apply insets to
     */
    public static void applyWindowInsetsBottom(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Apply window insets to a view group's children by adding margins
     * Useful when you want to preserve the parent's padding but adjust children
     *
     * @param viewGroup The view group to apply insets to
     */
    public static void applyWindowInsetsToChildren(ViewGroup viewGroup) {
        ViewCompat.setOnApplyWindowInsetsListener(viewGroup, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply margins to first and last child
            int childCount = viewGroup.getChildCount();
            if (childCount > 0) {
                View firstChild = viewGroup.getChildAt(0);
                ViewGroup.MarginLayoutParams firstParams = (ViewGroup.MarginLayoutParams) firstChild.getLayoutParams();
                firstParams.topMargin = systemBars.top;
                
                if (childCount > 1) {
                    View lastChild = viewGroup.getChildAt(childCount - 1);
                    ViewGroup.MarginLayoutParams lastParams = (ViewGroup.MarginLayoutParams) lastChild.getLayoutParams();
                    lastParams.bottomMargin = systemBars.bottom;
                }
            }
            
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Enable edge-to-edge display by making the window draw behind system bars
     * Call this in Activity.onCreate() before setContentView()
     *
     * @param view The root view of the activity
     */
    public static void enableEdgeToEdge(View view) {
        view.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }
}



