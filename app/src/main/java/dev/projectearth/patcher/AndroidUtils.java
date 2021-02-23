package dev.projectearth.patcher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class AndroidUtils {
    /**
     * Open the default browser at a given URL
     *
     * @param ctx The app context
     * @param url The URL to show
     */
    public static void showURL(Context ctx, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        ctx.startActivity(browserIntent);
    }


    /**
     * Show a toast message with Toast.LENGTH_SHORT
     *
     * @param ctx The app context
     * @param message The message to show
     */
    public static void showToast(Context ctx, String message) {
        showToast(ctx, message, Toast.LENGTH_SHORT);
    }

    /**
     * Show a toast message with the given length
     *
     * @param ctx The app context
     * @param message The message to show
     * @param length The length to show the toast for
     */
    public static void showToast(Context ctx, String message, int length) {
        Toast toast = Toast.makeText(ctx, message, length);
        toast.show();
    }
}
