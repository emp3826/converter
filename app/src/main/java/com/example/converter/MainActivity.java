package com.example.converter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.FFmpeg;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_FILE = 1;
    private Uri selectedFileUri;
    private Spinner formatSpinner;
    private TextView selectedFilePathTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pickFileButton = findViewById(R.id.pick_file_button);
        formatSpinner = findViewById(R.id.format_spinner);
        selectedFilePathTextView = findViewById(R.id.selected_file_path);
        Button convertButton = findViewById(R.id.convert_button);

        // 设置 Spinner 选项
        ArrayAdapter<VideoFormat> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, VideoFormat.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        pickFileButton.setOnClickListener(v -> openFileChooser());
        convertButton.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                VideoFormat selectedFormat = (VideoFormat) formatSpinner.getSelectedItem();
                convertVideo(selectedFileUri, Environment.getExternalStorageDirectory().getPath(), selectedFormat);
                selectedFilePathTextView.setText("正在转换: " + getRealPathFromURI(selectedFileUri));
            } else {
                Toast.makeText(this, "请先选择一个文件", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            selectedFilePathTextView.setText("选择的文件: " + getRealPathFromURI(selectedFileUri));
        }
    }

    public void convertVideo(Uri inputUri, String outputPath, VideoFormat targetFormat) {
        String inputFilePath = getRealPathFromURI(inputUri);
        String outputFilePath = outputPath + "/output." + targetFormat.toString().toLowerCase();

        // 生成转换命令
        String command = String.format("-i %s -c:v copy -c:a copy %s", inputFilePath, outputFilePath);

        FFmpeg.executeAsync(command, (executionId, returnCode) -> {
            if (returnCode == 0) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "转换成功", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "转换失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return null;
        }
    }
}
