package com.zhoorta.android.pdf.signer.smbtools;

import android.os.AsyncTask;

import java.io.IOException;


public class SMBDeleteRemoteFile extends AsyncTask<String, String, Boolean> {

    private final AsyncResponse delegate;

    private String error = "";
    private final SMBTools smb;

    public interface AsyncResponse {
        void processFinish(boolean success, String error);
    }

    public SMBDeleteRemoteFile(SMBTools smb, AsyncResponse delegate) {
        this.smb = smb;
        this.delegate = delegate;
    }

    @Override
    protected Boolean doInBackground(String... remotePath)  {
        try {
            smb.deleteRemote(remotePath[0]);
            return true;
        } catch (IOException ex) {
            error = ex.toString();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        delegate.processFinish(success,error);
    }

}
