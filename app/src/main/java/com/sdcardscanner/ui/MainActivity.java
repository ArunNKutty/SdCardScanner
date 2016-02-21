package com.sdcardscanner.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sdcardscanner.R;
import com.sdcardscanner.models.CustomFile;
import com.sdcardscanner.models.FileExtension;
import com.sdcardscanner.service.SdCardScanService;
import com.sdcardscanner.utils.Constants;
import com.sdcardscanner.utils.ListUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String FILTER = "sdcard_scanner_filter";
    public static final String BIGGEST_FILES = "biggest_files";
    public static final String FREQUENT_FILE_EXTENSIONS = "frequent_extensions";
    public static final String AVERAGE_FILE_SIZE = "average_file_size";
    private FloatingActionButton mfloatingActionButton, mShareActionButton;
    private ListView mFilesListView;
    private ListView mFrequentFiles;
    private TextView mAverageFileSize;
    private double mFileSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFilesListView = (ListView) findViewById(R.id.files);
        mAverageFileSize = (TextView) findViewById(R.id.averageFileSize);
        mFrequentFiles = (ListView) findViewById(R.id.frequentFiles);
        mShareActionButton = (FloatingActionButton) findViewById(R.id.share);
        mfloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mfloatingActionButton.setSelected(true);
        mfloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mfloatingActionButton.isSelected()) {
                    mfloatingActionButton.setImageResource(android.R.drawable.ic_media_pause);
                    mfloatingActionButton.setSelected(false);
                    startScanningService(true);

                } else {
                    mfloatingActionButton.setImageResource(android.R.drawable.ic_media_play);
                    mfloatingActionButton.setSelected(true);
                    startScanningService(false);
                }
            }
        });


        mShareActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareStatistics();
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        }, new IntentFilter(FILTER));
    }


    /**
     * Handle intent for various callbacks from background service
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (Parcels.unwrap(bundle.getParcelable(BIGGEST_FILES)) != null) {
            List<CustomFile> files = Parcels.unwrap(bundle.getParcelable(BIGGEST_FILES));
            displayFileData(files);
        } else if (Parcels.unwrap(bundle.getParcelable(FREQUENT_FILE_EXTENSIONS)) != null) {
            List<Map.Entry<String, Integer>> commonFileExtensions = Parcels.unwrap(bundle.getParcelable(FREQUENT_FILE_EXTENSIONS));
            displayFrequentFileExtensions(commonFileExtensions);
        } else if (bundle.getDouble(AVERAGE_FILE_SIZE) > 0) {
            mFileSize = bundle.getDouble(AVERAGE_FILE_SIZE);
            mAverageFileSize.setText("Average File Size : " + mFileSize + " kb");
            mShareActionButton.setVisibility(View.VISIBLE);
            mfloatingActionButton.setImageResource(android.R.drawable.ic_media_play);
            mfloatingActionButton.setSelected(true);
            startScanningService(false);
        }
    }

    private void displayFileData(List<CustomFile> files) {
        ArrayAdapter<CustomFile> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, files);
        mFilesListView.setAdapter(adapter);
        ListUtils.setListViewHeightBasedOnChildren(mFilesListView);
    }

    private void displayFrequentFileExtensions(List<Map.Entry<String, Integer>> commonFileExtensions) {

        List<FileExtension> fileExtensions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FileExtension extension = new FileExtension();
            extension.setFrequency(commonFileExtensions.get(i).getValue());
            extension.setFileExtension(commonFileExtensions.get(i).getKey());
            fileExtensions.add(extension);
        }
        ArrayAdapter<FileExtension> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, fileExtensions);
        mFrequentFiles.setAdapter(adapter);
        ListUtils.setListViewHeightBasedOnChildren(mFrequentFiles);
    }


    private void startScanningService(boolean isStart) {
        Intent intent = new Intent(MainActivity.this, SdCardScanService.class);
        intent.setAction(isStart ? Constants.ACTION.STARTFOREGROUND_ACTION : Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(intent);
    }

    private void shareStatistics() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Sharing Average File Size via SdCardScanner :" + mFileSize);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startScanningService(false);
    }
}


