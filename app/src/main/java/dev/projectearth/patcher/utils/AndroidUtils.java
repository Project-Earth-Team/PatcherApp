package dev.projectearth.patcher.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
        } catch(IOException ignored) { }
    }

    /**
     * Gets a stacktrace and returns it as a string
     *
     * @param e {@link Throwable} to get the stacktrace for
     * @return The {@link String} of the stacktrace
     */
    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Set the value of a final static variable and flag it as not final
     *
     * @param field {@link Field} to alter
     * @param newValue The value to set the field to
     * @throws Exception
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("accessFlags");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
