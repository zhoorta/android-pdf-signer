package com.zhoorta.android.pdf.signer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.Objects;

public class ShowFinalDocumentActivity extends AppCompatActivity {

    File tmpLocalFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_final_document);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Button buttonFinish = findViewById(R.id.buttonFinish);
        PDFView pdfView = findViewById(R.id.pdfFinalView);

        try {
            tmpLocalFile = new File(getIntent().getExtras().getString("file"));
            pdfView.fromFile(tmpLocalFile).load();
        } catch (Exception ex) {
            Log.e("new activity error", getIntent().getExtras().getString("file"));
        }


        buttonFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tmpLocalFile.delete();
                finish();
            }
        });


    }




}
