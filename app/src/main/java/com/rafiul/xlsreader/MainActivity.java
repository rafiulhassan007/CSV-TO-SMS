package com.rafiul.xlsreader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button button_choose_file, btn_send_sms;
    private TextView tv_file_name, tv_list_of_object;
    private static int INPUT_FILE_REQUEST_CODE = 99;
    private ArrayList<Information> infoS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_choose_file = (Button) findViewById(R.id.button_choose_file);
        btn_send_sms = (Button) findViewById(R.id.btn_send_sms);
        tv_file_name = (TextView) findViewById(R.id.tv_file_name);
        tv_list_of_object = (TextView) findViewById(R.id.tv_list_of_object);
        button_choose_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        btn_send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoS != null && infoS.size() > 0) {
                    sendToIntent(infoS);
                } else {
                    Toast.makeText(MainActivity.this, "Please select CSV file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFileChooser() {

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("text/csv|text/comma-separated-values|application/csv");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

//        Intent sendIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        sendIntent.setType("text/csv|text/comma-separated-values|application/csv");
//        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
//        sendIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//        startActivity(Intent.createChooser(sendIntent, "Export CSV"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != INPUT_FILE_REQUEST_CODE || resultCode != RESULT_OK) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            try {
                getFileURI(data.getData(),new File(data.getData().getPath()));
               // importFile(data.getData());
            } catch (Exception e) {
                Toast.makeText(this, "Can't Read CSV File", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Device Not Supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFileURI(Uri dataData, File data) {
        Log.d("TAG", "getFileURI: "+data.getAbsolutePath());
        try {
            String filename = data.getName();
            tv_file_name.setText(filename);
            importFile(dataData);
        } catch (Exception e) {
            Log.d("Err", e.toString() + "");
        }
    }

    private void importFile(Uri data) throws Exception {

        InputStream inputStream= getContentResolver().openInputStream(data);

        if (inputStream == null) {
            throw new IOException("Unable to obtain input stream from URI");
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            infoS = new ArrayList<>();
            try {
                String csvLine;
                int x = 0;
                while ((csvLine = reader.readLine()) != null) {
                    List<String> perLine = Arrays.asList(csvLine.split(","));
                    if (x != 0) {
                        infoS.add(new Information(perLine.get(0).trim(), perLine.get(1).trim(), perLine.get(2).trim()));
                    }
                    x++;
                }
                showInfoS(infoS);
            } catch (IOException | ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException("Error in reading CSV file: " + ex);
            }
        }
    }

    private void showInfoS(ArrayList<Information> infoS) {
        StringBuilder objectData = new StringBuilder();

        for (Information i : infoS) {

            objectData.append("\n").append(i.getContact()).append("==").append(i.getText());

        }
        tv_list_of_object.setText(objectData);
    }

    public String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private void sendToIntent(ArrayList<Information> in) {
        StringBuilder smsNumbers = new StringBuilder("smsto:");
        for (Information inf : in) {
            smsNumbers.append(inf.getContact()).append(";");
        }
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(smsNumbers.toString().substring(0, smsNumbers.length() - 1)));
        smsIntent.putExtra("sms_body", in.get(1).getText());
        startActivity(smsIntent);
    }

}