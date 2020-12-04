package com.carden.videoedit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.forevas.videoeditor.activity.RecordActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public static final int REQUEST_CODE_PERMISSIONS_CAMERA = 0x002;
    public static final String[] PERMISSIONS_CAMERA = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int REQUEST_CODE = 0x003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (EasyPermissions.hasPermissions(MainActivity.this, PERMISSIONS_CAMERA)) {
                    goToMediaRecorder();
                } else {
                    EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.request_read_storage_load_video),
                            REQUEST_CODE_PERMISSIONS_CAMERA,
                            PERMISSIONS_CAMERA);
                }
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS_CAMERA:
                goToMediaRecorder();
                break;
        }
    }

    private void goToMediaRecorder() {
        startActivityForResult(new Intent(MainActivity.this, RecordActivity.class), REQUEST_CODE);

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(MainActivity.this, "无权限", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (data != null) {
                    String videoPath = data.getStringExtra("path");
                    long videoTime = data.getLongExtra("recordTime", 5000);
                    Toast.makeText(MainActivity.this, "--videoPath--" + videoPath + "-videoTime--" + videoTime, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
}
