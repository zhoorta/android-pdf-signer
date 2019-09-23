package com.zhoorta.android.pdf.signer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.zhoorta.android.pdf.signer.smbtools.SMBServerConnect;
import com.zhoorta.android.pdf.signer.smbtools.SMBCopyRemoteFile;
import com.zhoorta.android.pdf.signer.smbtools.SMBTools;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SMBTools smb;

    private Button buttonSearch;
    private EditText editSearch;
    private SharedPreferences config;

    private boolean checkConfig() {
        config = getSharedPreferences("SignerConfig", 0);

        if(config.getString("server", null)==null) return false;
        return true;
    }

    private void openConfigActivity() {
        Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
        //startActivity(intent);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                connect();
                Log.d("mainactivity","connect");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //cancelled
                Log.d("mainactivity","cancellled");
            }
        }
    }

    private void connect() {
        new SMBServerConnect(new SMBServerConnect.AsyncResponse(){
            @Override
            public void processFinish(SMBTools smb, String error){
                MainActivity.this.smb = smb;
                if(smb==null) {
                    buttonSearch.setEnabled(false);
                    alert(error);
                }
                else buttonSearch.setEnabled(true);
            }
        }).execute(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:
                openConfigActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSearch = findViewById(R.id.buttonSearch);
        editSearch = findViewById(R.id.editSearch);

        if(checkConfig()) connect();
        else openConfigActivity();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new SMBServerConnect(new SMBServerConnect.AsyncResponse(){
                    @Override
                    public void processFinish(final SMBTools smb, String error){

                    if(smb==null) { alert(error); return;}

                    new SMBCopyRemoteFile(smb, new SMBCopyRemoteFile.AsyncResponse(){
                        @Override
                        public void processFinish(String output, boolean error){
                            if(!error) {
                                Intent intent = new Intent(MainActivity.this, ShowDocumentActivity.class);
                                intent.putExtra("source", editSearch.getText().toString() + config.getString("suffix","") + ".pdf");
                                intent.putExtra("file", output);
                                startActivity(intent);
                                editSearch.getText().clear();
                            }
                            else {
                                alert(getResources().getString(R.string.error_not_found));
                            }

                            smb.close();

                        }
                    }).execute(MainActivity.this.config.getString("inboundPath", "in") + "\\" + editSearch.getText().toString() + config.getString("suffix","")  + ".pdf");

                    }
                }).execute(getApplicationContext());
            }
        });


    }

    private void alert(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(R.string.error_title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
