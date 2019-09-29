package com.appman.appmanager.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppRuntimeException implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "AppRuntimeException";
    private static final String SEND_LOG_TO_EMAIL_TEXT = "rudraksh3011@gmail.com";
    private static final String SEND_LOG_TO_EMAIL_SUBJECT = "AppManager log file";
    private static final String SEND_LOG_TO_EMAIL_EXTRA_TEXT = "Log file attached.";
    private static final String REPORT_CONTENT_TITLE = "Unexpected Error occurred";
    private static final String REPORT_CONTENT_TEXT = "Send error report to developer to help this get fixed. " +
            "This won't take your much time. Also includes crash reports.";
    private static String CRASH_FILE_PATH_NAME = "";
    private Activity currentActivity;

    public AppRuntimeException(Thread t, Throwable e, Activity a) {
        this.currentActivity = a;
        uncaughtException(t, e);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        // not all Android versions will print the stack trace automatically
        if (isUiThread()) {
            writeLogToFile(thread, e);
        } else {
            //handle non UI thread throw uncaught exception
            new Handler(Looper.getMainLooper()).post(() ->
                    writeLogToFile(thread, e));
        }
    }

    private boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private void writeLogToFile(final Thread t, final Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        final StringBuffer report = new StringBuffer(e.toString());
        final String lineSeperator = "-------------------------------\n\n";
        final String DOUBLE_LINE_SEP = "\n\n";
        final String SINGLE_LINE_SEP = "\n";
        report.append(DOUBLE_LINE_SEP);
        report.append("--------- Stack trace ---------\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
            report.append(SINGLE_LINE_SEP);
        }
        report.append(lineSeperator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(DOUBLE_LINE_SEP);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        // Getting the Device brand, model and sdk version details.
        report.append(lineSeperator);
        report.append("--------- Device ---------\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(SINGLE_LINE_SEP);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(SINGLE_LINE_SEP);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(SINGLE_LINE_SEP);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(SINGLE_LINE_SEP);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        report.append("--------- Firmware ---------\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK_INT);
        report.append(SINGLE_LINE_SEP);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(SINGLE_LINE_SEP);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        Log.e(TAG ,"Crash Report :: " + report.toString());

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String fileNameForNougat = "crash_log_file_" + System.currentTimeMillis() + ".txt";
        CRASH_FILE_PATH_NAME = FileUtil.getInstance().getDefaultCrashLogFolder().getPath() + "/" + fileNameForNougat;

        // Extract to file.
        File file = new File(CRASH_FILE_PATH_NAME);
        FileWriter writer = null;

        try {
            // write output stream
            writer = new FileWriter(file);
            writer.write(report.toString());

            writer.close();
        } catch (IOException iOex) {
            Log.e(TAG, "Exception while writing crash file: " + iOex.getMessage());
        }

        invokeCrashedDialog(CRASH_FILE_PATH_NAME, fileNameForNougat);
    }

    private void restartApplication() {
        // make sure we die, otherwise the app will hang ...
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void invokeCrashedDialog(final String fullName, final String fileNameForNougat) {
        new Thread(() -> {
            Looper.prepare();
            new AlertDialog.Builder(currentActivity)
                    .setTitle(REPORT_CONTENT_TITLE)
                    .setMessage(REPORT_CONTENT_TEXT)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        sendLogFile(fullName, fileNameForNougat);
                        restartApplication();
                    }).setNegativeButton(android.R.string.cancel, (dialogInterface, i)
                    -> restartApplication())
                    .show();
            Looper.loop();
        }).start();
    }

    private void sendLogFile(String fullName, String fileNameForNougat) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SEND_LOG_TO_EMAIL_TEXT});
            intent.putExtra(Intent.EXTRA_SUBJECT, SEND_LOG_TO_EMAIL_SUBJECT);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
            intent.putExtra(Intent.EXTRA_TEXT, SEND_LOG_TO_EMAIL_EXTRA_TEXT); // do this so some email clients don't complain about empty body.
            currentActivity.startActivity(intent);
        } else {
            intent.setType("text/plain");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SEND_LOG_TO_EMAIL_TEXT});
            intent.putExtra(Intent.EXTRA_SUBJECT, SEND_LOG_TO_EMAIL_SUBJECT);
            String logFileName = Environment.getExternalStorageDirectory()
                    + AppConstants.DEFAULT_CRASH_LOG_FOLDER + fileNameForNougat;
            File shareFile = new File(logFileName);
            Uri contentUri = FileProvider.getUriForFile(currentActivity,
                    currentActivity.getPackageName() + ".provider", shareFile);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.putExtra(Intent.EXTRA_TEXT, SEND_LOG_TO_EMAIL_EXTRA_TEXT); // do this so some email clients don't complain about empty body.
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            currentActivity.startActivity(intent);
        }
    }
}
