package com.zhoorta.android.pdf.signer.pdftools;

import android.content.Context;
import android.util.Log;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;

public class PDFTools {

    private PDDocument doc;
    private File source;

    public PDFTools(Context context) {
        PDFBoxResourceLoader.init(context);
    }

    public void open(File file) {
        try {
            doc = PDDocument.load(file);
            source = file;
            Log.d("signer","open pdf done");
        } catch (Exception ex) {
            Log.e("signer","open pdf exception");
            Log.e("signer",ex.toString());
        }
    }


    public void insertImage(String imagePath, int xpos, int ypos, int width, int height, int pagenumber) {
        try {
            PDPage page = doc.getPage(pagenumber - 1);
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page,true,false);
            contentStream.drawImage(pdImage, xpos, ypos, width, height);
            contentStream.close();
            doc.save(source.getPath());
            doc.close();
            Log.d("signer","insertImage done");
        } catch (Exception ex) {
            Log.e("signer","insertImage exception");
            Log.e("signer",ex.toString());
        }
    }
}
