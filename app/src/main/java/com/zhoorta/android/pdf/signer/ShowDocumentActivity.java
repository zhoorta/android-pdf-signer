package com.zhoorta.android.pdf.signer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.Objects;

public class ShowDocumentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_document);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Button buttonGetSignature = findViewById(R.id.buttonGetSignature);
        PDFView pdfView = findViewById(R.id.pdfView);

        try {
            File f = new File(getIntent().getExtras().getString("file"));
            pdfView.fromFile(f).load();
        } catch (Exception ex) {
            Log.e("new activity error", getIntent().getExtras().getString("file"));
        }


        buttonGetSignature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ShowDocumentActivity.this, GetSignatureActivity.class);
                intent.putExtra("file", getIntent().getExtras().getString("file"));
                intent.putExtra("source", getIntent().getExtras().getString("source"));
                startActivity(intent);
                finish();
            }
        });


    }




}
