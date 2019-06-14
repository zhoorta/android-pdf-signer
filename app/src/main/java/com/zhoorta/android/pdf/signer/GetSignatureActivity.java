package com.zhoorta.android.pdf.signer;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.zhoorta.android.pdf.signer.pdftools.PDFTools;
import com.zhoorta.android.pdf.signer.smbtools.SMBCopyLocalFile;
import com.zhoorta.android.pdf.signer.smbtools.SMBDeleteRemoteFile;
import com.zhoorta.android.pdf.signer.smbtools.SMBServerConnect;
import com.zhoorta.android.pdf.signer.smbtools.SMBTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class GetSignatureActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;

    private SharedPreferences config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_signature);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        config = getSharedPreferences("SignerConfig", 0);


        mSignaturePad = findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Toast.makeText(GetSignatureActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }
        });

        mClearButton = findViewById(R.id.clear_button);
        mSaveButton = findViewById(R.id.save_button);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });


        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert("Saving document ...");

                new SMBServerConnect(new SMBServerConnect.AsyncResponse(){
                    @Override
                    public void processFinish(final SMBTools smb, String error){

                        if(smb==null) { alert(error); return;}

                        try {
                            int xpos = Integer.parseInt(config.getString("xpos", "110"));
                            int ypos = Integer.parseInt(config.getString("ypos", "170"));
                            int width = Integer.parseInt(config.getString("width", "200"));
                            int height = Integer.parseInt(config.getString("height", "50"));
                            int page = Integer.parseInt(config.getString("page", "1"));


                            PDFTools pdf = new PDFTools(getApplicationContext());
                            Bitmap signatureBitmap = mSignaturePad.getTransparentSignatureBitmap();
                            final File outputSignature = saveSignature(signatureBitmap);

                            final File tmpLocalFile = new File(getIntent().getExtras().getString("file"));
                            pdf.open(tmpLocalFile);
                            pdf.insertImage(outputSignature.getPath(),xpos,ypos,width,height,page);


                            new SMBCopyLocalFile(smb, GetSignatureActivity.this.config.getString("outboundPath", "out") + "\\" + getIntent().getExtras().getString("source"), new SMBCopyLocalFile.AsyncResponse() {
                                @Override
                                public void processFinish(boolean success, String error) {
                                    Log.e("SMBCopyLocalFile","SMBCopyLocalFile");
                                    Log.e("SMBCopyLocalFile",error);

                                    tmpLocalFile.delete();
                                    outputSignature.delete();

                                    new SMBDeleteRemoteFile(smb, new SMBDeleteRemoteFile.AsyncResponse() {
                                        @Override
                                        public void processFinish(boolean success, String error) {
                                            finish();
                                        }
                                    }).execute(GetSignatureActivity.this.config.getString("inboundPath", "in") + "\\" + getIntent().getExtras().getString("source"));

                                }
                            }).execute(tmpLocalFile);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }).execute(getApplicationContext());

            }
        });
    }

    private void alert(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        //alertDialog.setTitle("Success");
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(GetSignatureActivity.this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void saveBitmapToPNG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.PNG , 100, stream);
        stream.close();
    }

    private File saveSignature(Bitmap signature) {
        try {
            File output = File.createTempFile("signer", ".png", getApplicationContext().getCacheDir());
            saveBitmapToPNG(signature, output);
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
