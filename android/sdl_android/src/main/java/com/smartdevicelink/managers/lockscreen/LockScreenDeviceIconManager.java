package com.smartdevicelink.managers.lockscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.smartdevicelink.util.AndroidTools;
import com.smartdevicelink.util.DebugTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <strong>LockScreenDeviceIconManager</strong> <br>
 *
 * The LockScreenDeviceIconManager handles the logic of caching and retrieving cached lock screen icons <br>
 *
 */
class LockScreenDeviceIconManager {

    private Context context;
    private static final String SDL_DEVICE_STATUS_SHARED_PREFS = "sdl.lockScreenIcon";
    private static final String STORED_ICON_DIRECTORY_PATH = "sdl/lock_screen_icon/";

    interface OnIconRetrievedListener {
        void onImageRetrieved(Bitmap icon);
        void onError(String info);
    }

    LockScreenDeviceIconManager(Context context) {
        this.context = context;
        File lockScreenDirectory = new File(context.getCacheDir(), STORED_ICON_DIRECTORY_PATH);
        lockScreenDirectory.mkdirs();
    }

    /**
     * Will try to return a lock screen icon either from cache or downloaded
     * if it fails iconRetrievedListener.OnError will be called with corresponding error message
     * @param iconURL url that the lock screen icon is downloaded from
     * @param iconRetrievedListener an interface that will implement onIconReceived and OnError methods
     */
    void retrieveIcon(String iconURL, OnIconRetrievedListener iconRetrievedListener) {
        Bitmap icon = null;
        try {
            if (isIconCachedAndValid(iconURL)) {
                DebugTool.logInfo("Icon Is Up To Date");
                icon = getFileFromCache(iconURL);
                if (icon == null) {
                    DebugTool.logInfo("Icon from cache was null, attempting to re-download");
                    icon = AndroidTools.downloadImage(iconURL);
                    if (icon != null) {
                        saveFileToCache(icon, iconURL);
                    } else {
                        iconRetrievedListener.onError("Icon downloaded was null");
                        return;
                    }
                }
                iconRetrievedListener.onImageRetrieved(icon);
            } else {
                // The icon is unknown or expired. Download the image, save it to the cache, and update the archive file
                DebugTool.logInfo("Lock Screen Icon Update Needed");
                icon = AndroidTools.downloadImage(iconURL);
                if (icon != null) {
                    saveFileToCache(icon, iconURL);
                    iconRetrievedListener.onImageRetrieved(icon);
                } else {
                    iconRetrievedListener.onError("Icon downloaded was null");
                }
            }
        } catch (IOException e) {
            iconRetrievedListener.onError("device Icon Error Downloading, Will attempt to grab cached Icon even if expired: \n" + e.toString());
            icon = getFileFromCache(iconURL);
            if (icon != null) {
                iconRetrievedListener.onImageRetrieved(icon);
            } else {
                iconRetrievedListener.onError("Unable to retrieve icon from cache");
            }
        }
    }

    /**
     * Will decide if a cached icon is available and up to date
     * @param iconUrl url will be hashed and used to look up last updated timestamp in shared preferences
     * @return True when icon details are in shared preferences and less than 30 days old, False if icon details are too old or not found
     */
    private boolean isIconCachedAndValid(String iconUrl) {
        String iconHash = getMD5HashFromIconUrl(iconUrl);
        SharedPreferences sharedPref = this.context.getSharedPreferences(SDL_DEVICE_STATUS_SHARED_PREFS, Context.MODE_PRIVATE);
        String iconLastUpdatedTime = sharedPref.getString(iconHash, null);
        if(iconLastUpdatedTime == null) {
            DebugTool.logInfo("No Icon Details Found In Shared Preferences");
            return false;
        } else {
            DebugTool.logInfo("Icon Details Found");
            long lastUpdatedTime = 0;
            try {
                lastUpdatedTime = Long.parseLong(iconLastUpdatedTime);
            } catch (NumberFormatException e) {
                DebugTool.logInfo("Invalid time stamp stored to shared preferences, clearing cache and share preferences");
                clearIconDirectory();
                sharedPref.edit().clear().commit();
            }
            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastUpdatedTime;
            long daysBetweenLastUpdate = timeDifference / (1000 * 60 * 60 * 24);
            return daysBetweenLastUpdate < 30;
        }
    }

    /**
     * Will try to save icon to cache
     * @param icon the icon bitmap that should be saved to cache
     * @param iconUrl the url where the icon was retrieved will be hashed and used for file and file details lookup
     */
    private void saveFileToCache(Bitmap icon, String iconUrl) {
        String iconHash = getMD5HashFromIconUrl(iconUrl);
        File f = new File(this.context.getCacheDir() + "/" + STORED_ICON_DIRECTORY_PATH, iconHash);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            writeDeviceIconParametersToSharedPreferences(iconHash);
        } catch (Exception e) {
            DebugTool.logError("Failed to save icon to cache");
            e.printStackTrace();
        }
    }

    /**
     * Will try to retrieve icon bitmap from cached directory
     * @param iconUrl the url where the icon was retrieved will be hashed and used to look up file location
     * @return bitmap of device icon or null if it fails to find the icon or read from shared preferences
     */
    private Bitmap getFileFromCache(String iconUrl) {
        String iconHash = getMD5HashFromIconUrl(iconUrl);
        SharedPreferences sharedPref = this.context.getSharedPreferences(SDL_DEVICE_STATUS_SHARED_PREFS, Context.MODE_PRIVATE);
        String iconLastUpdatedTime = sharedPref.getString(iconHash, null);

        if (iconLastUpdatedTime != null) {
            Bitmap cachedIcon = BitmapFactory.decodeFile(this.context.getCacheDir() + "/" + STORED_ICON_DIRECTORY_PATH + "/" + iconHash);
            if(cachedIcon == null) {
                DebugTool.logError("Failed to get Bitmap from decoding file cache");
                clearIconDirectory();
                sharedPref.edit().clear().commit();
                return null;
            } else {
                return cachedIcon;
            }
        } else {
            DebugTool.logError("Failed to get shared preferences");
            return null;
        }
    }

    /**
     * Will write information about the icon to shared preferences
     * icon information will have a look up key of the hashed icon url and the current timestamp to indicated when the icon was last updated.
     * @param iconHash the url where the icon was retrieved will be hashed and used lookup key
     */
    private void writeDeviceIconParametersToSharedPreferences(String iconHash) {
        SharedPreferences sharedPref = this.context.getSharedPreferences(SDL_DEVICE_STATUS_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(iconHash, String.valueOf(System.currentTimeMillis()));
        editor.commit();
    }

    /**
     * Create an MD5 hash of the icon url for file storage and lookup/shared preferences look up
     * @param iconUrl the url where the icon was retrieved
     * @return MD5 hash of the icon URL
     */
    private String getMD5HashFromIconUrl(String iconUrl) {
        String iconHash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(iconUrl.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            iconHash = hashtext;
        } catch (NoSuchAlgorithmException e) {
            DebugTool.logError("Unable to hash icon url");
            e.printStackTrace();
        }
        return iconHash;
    }

    /**
     * Clears all files in the directory where lock screen icons are cached
     */
    private void clearIconDirectory() {
        File iconDir = new File(context.getCacheDir() + "/" + STORED_ICON_DIRECTORY_PATH);
        if (iconDir.listFiles() != null) {
            for (File child : iconDir.listFiles()) {
                child.delete();
            }
        }
    }
}
