package com.sdcardscanner.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.sdcardscanner.R;
import com.sdcardscanner.models.CustomFile;
import com.sdcardscanner.ui.MainActivity;
import com.sdcardscanner.utils.Constants;
import com.sdcardscanner.utils.MapUtils;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SdCardScanService extends Service {
    //Defining max capacity of the ArrayList
    private List<CustomFile> mFiles = new ArrayList<>();

    //For calculating the frequencies of extensions
    private Map<String, Integer> freqExtensions = new HashMap<>();

    private NotificationCompat.Builder mBuilder;

    public SdCardScanService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

            createNotification();
            startFileScanning();

        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }


    private void createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notificationBuilder(pendingIntent));
    }

    private Notification notificationBuilder(PendingIntent intent) {

        //Todo Move Strings to strings.xml
        mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("SdCard Scanner")
                .setTicker("SdCard Scanner")
                .setContentText("Scanning Files")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(intent).setAutoCancel(true).setProgress(0, 0, true)
                .setOngoing(true);

        return mBuilder.build();

    }


    private void startFileScanning() {

        File path = Environment.getExternalStorageDirectory();
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            getAllFilesOfDir(path);
            Collections.sort(mFiles);
            getFilesBasedOnSize(10);
            getFrequency(5);
            mBuilder.setProgress(0, 0, false);
            calculateAverage();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * recursively traverse directories to get files
     *
     * @param directory
     */
    private void getAllFilesOfDir(File directory) {

        final File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file != null) {
                    if (file.isDirectory()) {  // This is a folder
                        getAllFilesOfDir(file);
                    } else {  //This is a file
                        float fileSize = (file.length() / 1024);
                        String fileName = file.getName();
                        String fileExtension = "";
                        if (fileName.contains(".")) {
                            fileExtension = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
                            if (freqExtensions.get(fileExtension) != null) {
                                freqExtensions.put(fileExtension, freqExtensions.get(fileExtension) + 1);
                            } else {
                                freqExtensions.put(fileExtension, 1);
                            }
                        }
                        mFiles.add(new CustomFile(file.getName(), file.getAbsolutePath(), fileSize, fileExtension));
                    }
                }
            }
        }
    }


    /**
     * get frequency of extension type based on order. Here it is top 5 extensions
     *
     * @param order
     */
    private void getFrequency(int order) {
        List<Map.Entry<String, Integer>> commonFileExtensions = MapUtils.entriesSortedByValues(freqExtensions);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.FREQUENT_FILE_EXTENSIONS, Parcels.wrap(commonFileExtensions));
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
                new Intent(MainActivity.FILTER).putExtras(bundle)
        );
    }

    /**
     * Get Names and Sizes of biggest files based on order
     *
     * @param order
     * @return
     */
    private void getFilesBasedOnSize(int order) {
        List<CustomFile> files = new ArrayList<>(mFiles.subList(0, order));
        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.BIGGEST_FILES, Parcels.wrap(files));
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
                new Intent(MainActivity.FILTER).putExtras(bundle)
        );
    }

    /**
     * calculate average file size
     *
     * @return
     */
    private void calculateAverage() {
        long sum = 0;
        for (CustomFile file : mFiles) {
            sum += file.getFileSize();
        }
        Bundle bundle = new Bundle();
        double average = mFiles.isEmpty() ? 0 : 1.0 * sum / mFiles.size();
        bundle.putDouble(MainActivity.AVERAGE_FILE_SIZE, average);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
                new Intent(MainActivity.FILTER).putExtras(bundle)
        );
    }


}
