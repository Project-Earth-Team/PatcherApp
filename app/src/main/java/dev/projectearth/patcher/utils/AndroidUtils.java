package dev.projectearth.patcher.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    /**
     * TODO: This javadoc
     * https://stackoverflow.com/a/9456947/5299903
     * @param f
     */
    public static void normalizeFile(File f) {
        File temp = null;
        BufferedReader bufferIn = null;
        BufferedWriter bufferOut = null;

        try {
            if (f.exists()) {
                // Create a new temp file to write to
                temp = new File(f.getAbsolutePath() + ".normalized");
                temp.createNewFile();

                // Get a stream to read from the file un-normalized file
                FileInputStream fileIn = new FileInputStream(f);
                DataInputStream dataIn = new DataInputStream(fileIn);
                bufferIn = new BufferedReader(new InputStreamReader(dataIn));

                // Get a stream to write to the normalized file
                FileOutputStream fileOut = new FileOutputStream(temp);
                DataOutputStream dataOut = new DataOutputStream(fileOut);
                bufferOut = new BufferedWriter(new OutputStreamWriter(dataOut));

                // For each line in the un-normalized file
                String line;
                while ((line = bufferIn.readLine()) != null) {
                    // Write the original line plus the operating-system dependent newline
                    bufferOut.write(line);
                    bufferOut.write("\n");
                }

                bufferIn.close();
                bufferOut.close();

                // Remove the original file
                f.delete();

                // And rename the original file to the new one
                temp.renameTo(f);
            } else {
                // If the file doesn't exist...
                throw new IOException("Could not find file to open: " + f.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Clean up, temp should never exist
            try {
                temp.delete();
            } catch (NullPointerException ignored) { }
            try {
                bufferIn.close();
            } catch (NullPointerException | IOException ignored) { }
            try {
                bufferOut.close();
            } catch (NullPointerException | IOException ignored) { }
        }
    }
}
