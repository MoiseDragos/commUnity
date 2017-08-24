package com.community.community.General;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.bumptech.glide.util.LruCache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UsefulThings {

    public static final String FB_STORAGE_PATH = "images/causes/";
    public static final String PROFILE_URI_KEY = "Profile1";
    public static final String OPTIONAL_URI_1_KEY = "Optional1";
    public static final String OPTIONAL_URI_2_KEY = "Optional2";
    public static final String LAT = "Latitude";
    public static final String LNG = "Longitude";

    public static final String MY_CAUSES = "MyCauses";
    public static final String MY_SUPPORTED_CAUSES = "MySupportedCauses";
    public static final String MY_AGE = "MyAge";
    public static final String MY_ADDRESS = "MyAddress";
    public static final String MY_DESCRIPTION = "MyDescription";

    public static final String MY_CAUSES_ACTIVITY = "MyCau";
    public static final String MY_SUPPORTED_CAUSES_ACTIVITY = "MySupp";
    public static final String ALL_CAUSES_ACTIVITY = "AllCau";

    public static final String FB_STORAGE_USERS_PATH = "images/users/";

    public static final int CAUSE_INTERMEDIATE_IDS = 5;
    public static final int PROPOSALS_INTERMEDIATE_IDS = 5;
    public static final int NGO_INTERMEDIATE_IDS = 7;
    public static final int USERS_INTERMEDIATE_IDS = 7;

    public static int causeCacheSize = 8 * 1024 * 1024; // 8MiB
    public static int proposalsCacheSize = 4 * 1024 * 1024; // 4MiB
    public static User currentUser;

    public static LruCache<String, Cause> causeCaches;
//    public static int ngoCacheSize = 4 * 1024 * 1024; // 4MiB


//    public static LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
//        protected int sizeOf(String key, Bitmap value) {
//            return value.getByteCount();
//        }
//    };

    /* Functions */

    public static Bitmap getThumbnail(Uri uri, Context context) throws IOException {
        Bitmap bitmap;
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();
            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
                return null;
            }

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth)
                    ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int displayWidth = displayMetrics.widthPixels;
            int displayHeight = displayMetrics.heightPixels;

            if(onlyBoundsOptions.outWidth < 150 || onlyBoundsOptions.outHeight < 150) {
                Toast.makeText(context, "Imaginea încărcată este prea mică", Toast.LENGTH_SHORT).show();
                return null;
            }

            double widthRatio = onlyBoundsOptions.outWidth / (1.0 * displayMetrics.widthPixels);
            double heightRatio = onlyBoundsOptions.outHeight / (1.0 * displayMetrics.heightPixels);

            double THUMBNAIL_SIZE = widthRatio > heightRatio ? displayHeight : displayWidth;

            double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = UsefulThings.getPowerOfTwoForSampleRatio(ratio);
            bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
            input = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Schimbarea imaginii de profil nereușită", Toast.LENGTH_SHORT).show();
            return null;
        }
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k == 0) return 1;
        else return k;
    }

    public static Uri getCompressedImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPath(Uri uri, Context context){
        String realPath;

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            realPath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            realPath = uri.getPath();
        }
        return realPath;
    }

}
