package com.teebz.hrf;

import com.teebz.hrf.activities.SingleImageActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class Helpers {

    public static final String CALL_IMGS_FOLDER = "call_imgs/";
    public static final String OFFICIAL_IMGS_FOLDER = "official_imgs/";
    public static final String DIAGRAM_IMGS_FOLDER = "diagram_imgs/";

    public static Drawable getDrawableFromAssets(Context context, String folder, String fileName, String fileType) {
        InputStream ims = null;
        try {
            ims = context.getAssets().open(folder + fileName + fileType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Drawable.createFromStream(ims, null);
    }

    public static boolean startFeedbackEmail(Context context) {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("message/rfc822");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "hrf.feedback@gmail.com" });
        Email.putExtra(Intent.EXTRA_SUBJECT, "Hockey Rule Finder Feedback");
        try {
            context.startActivity(Intent.createChooser(Email, "Send Feedback:"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    public static void showLargeImage(Context context, String imgName, String title, String folder) {
        Intent newActivity = new Intent(context, SingleImageActivity.class);
        newActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_KEY, imgName);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_TITLE, title);
        newActivity.putExtra(SingleImageActivity.SINGLE_IMAGE_FOLDER, folder);
        context.startActivity(newActivity);
    }
}
