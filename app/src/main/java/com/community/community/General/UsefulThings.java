package com.community.community.General;

import android.graphics.Bitmap;

import com.bumptech.glide.util.LruCache;

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

    public static final int CAUSE_INTERMEDIATE_IDS = 5;
    public static final int PROPOSALS_INTERMEDIATE_IDS = 5;
    public static final int NGO_INTERMEDIATE_IDS = 7;
    public static final int USERS_INTERMEDIATE_IDS = 7;

    public static int causeCacheSize = 8 * 1024 * 1024; // 8MiB
    public static int proposalsCacheSize = 4 * 1024 * 1024; // 4MiB
    public static int ngoCacheSize = 4 * 1024 * 1024; // 4MiB
//    public static LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
//        protected int sizeOf(String key, Bitmap value) {
//            return value.getByteCount();
//        }
//    };

}
