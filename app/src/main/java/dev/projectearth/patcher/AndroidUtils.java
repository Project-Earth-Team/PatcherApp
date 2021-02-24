package dev.projectearth.patcher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AndroidUtils {

    private static final OkHttpClient HTTP_CLIENT;

    static {
        HTTP_CLIENT = new OkHttpClient.Builder().followRedirects(true).build();
    }

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

    /**
     * Download a file to a given location
     * https://stackoverflow.com/a/1718140/5299903
     *
     * @param url Url to download
     * @param outputFile Location to download to
     */
    public static void downloadFile(String url, File outputFile) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute();
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(response.body().bytes());
        } catch(IOException e) {
            return; // swallow a 404
        }
    }
}
