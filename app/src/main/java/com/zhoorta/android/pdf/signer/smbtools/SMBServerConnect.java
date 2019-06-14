package com.zhoorta.android.pdf.signer.smbtools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class SMBServerConnect extends AsyncTask<Context, String, SMBTools> {

    private final AsyncResponse delegate;

    private String error = "";

    public interface AsyncResponse {
        void processFinish(SMBTools smb, String error);
    }

    public SMBServerConnect(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected SMBTools doInBackground(Context... context)  {
        try {
            SMBTools smb = new SMBTools(context[0]);

            SharedPreferences config = context[0].getSharedPreferences("SignerConfig", 0);

            String server = config.getString("server", null);
            String username = config.getString("username", null);
            String password = config.getString("password", null);
            String share = config.getString("share", null);
            String domain = config.getString("domain", null);


            smb.connect(server, username, password, domain);
            smb.openShare(share);

            return smb;
        } catch (Exception ex) {
            Log.e("catch",ex.toString());
            error = ex.toString();
            return null;
        }
    }

    @Override
    protected void onPostExecute(SMBTools result) {
        delegate.processFinish(result,error);
    }

}
