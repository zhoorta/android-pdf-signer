package com.zhoorta.android.pdf.signer.smbtools;

import android.content.Context;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.utils.SmbFiles;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN;

public class SMBTools {

    private final Context context;
    private final SMBClient client;
    private Session session;
    private DiskShare share;


    public SMBTools(Context context) {
        this.client = new SMBClient();
        this.context = context;
    }

    public void close() {
        this.client.close();
    }

    public Session connect(String hostname,String username,String password,String domain) throws IOException {

        try {
            Connection connection = client.connect(hostname);
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), domain);
            this.session = connection.authenticate(ac);
            Log.d("SMBTools","connected");
            return this.session ;
        } catch (IOException ex) {
            Log.d("SMBTools","connect exception", ex);
            throw new IOException(ex);
        }
    }

    public DiskShare openShare(String shareName) throws IOException {
        try {
            this.share = (DiskShare) this.session.connectShare(shareName);
            Log.d("SMBTools","share opened");
            return this.share;
        } catch (Exception ex) {
            Log.d("SMBTools","openshare exception");
            Log.d("SMBTools",ex.toString());
            throw new IOException(ex);
        }
    }

    public void copyToRemote(java.io.File source, String destination) throws IOException {
        try {
            SmbFiles.copy(source,share,destination,true);
            Log.d("SMBTools","copyToRemote done");
        } catch (Exception ex) {
            Log.d("SMBTools","copyToRemote exception");
            Log.d("SMBTools",ex.toString());
            throw new IOException(ex);
        }
    }

    public java.io.File copyToLocal(String remoteFile,String extension) throws IOException {
        try {
            File smbInFile = share.openFile(remoteFile, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, FILE_OPEN, null);
            java.io.File localOutFile = java.io.File.createTempFile("signer", extension, context.getCacheDir());
            copyStream(smbInFile,localOutFile);
            smbInFile.close();
            return localOutFile;
        } catch (Exception ex) {
            Log.d("SMBTools","copyToLocal exception");
            Log.d("SMBTools",ex.toString());
            throw new IOException(ex);
        }

    }

    public void deleteRemote(String file) throws IOException {
        try {
            share.rm(file);
        } catch (Exception ex) {
            Log.e("SMBTools","deleteRemote exception");
            Log.e("SMBTools",ex.toString());
            throw new IOException(ex);
        }
    }

    public void listShareFolder(String folder) throws IOException {
        try {
            for (FileIdBothDirectoryInformation f : share.list(folder, "*")) {
                Log.d("SMBTools","File : " + f.getFileName());
            }
        } catch (Exception ex) {
            Log.d("SMBTools","listShare exception");
            Log.d("SMBTools",ex.toString());
            throw new IOException(ex);
        }
    }

    private void copyStream(File source, java.io.File destination) throws IOException {

        // This is the maximum number of bytes the server will allow you to read in a single request
        int maxReadSize = share.getTreeConnect().getSession().getConnection().getNegotiatedProtocol().getMaxReadSize();
        byte[] buffer = new byte[maxReadSize];

        long offset = 0;
        long remaining = source.getFileInformation(FileStandardInformation.class).getEndOfFile();

        try {
            FileOutputStream out = new FileOutputStream(destination);
            while (remaining > 0) {
                int amount = remaining > buffer.length ? buffer.length : (int) remaining;
                int amountRead = source.read(buffer, offset, 0, amount);
                if (amountRead == -1) {
                    remaining = 0;
                } else {
                    out.write(buffer, 0, amountRead);
                    remaining -= amountRead;
                    offset += amountRead;
                }
            }

            out.flush();
            out.close();

            Log.d("SMBTools", "copystream finished");
        }  catch (Exception ex) {
            Log.e("SMBTools","copystream exception");
            Log.e("SMBTools",ex.toString());
            throw new IOException(ex);
        }
    }


}
