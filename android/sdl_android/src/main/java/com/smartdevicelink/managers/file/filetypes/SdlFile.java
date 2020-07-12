/*
 * Copyright (c) 2019 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.smartdevicelink.managers.file.filetypes;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.StaticIconName;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class representing data to be uploaded to core
 */
public class SdlFile{
    private String fileName;
    private int id = -1;
    private Uri uri;
    private byte[] fileData;
    private FileType fileType;
    private boolean persistentFile;
    private boolean isStaticIcon;
    private boolean shouldAutoGenerateName;
    // Overwrite property by default is set to true in SdlFile constructors indicating that a file can be overwritten
    private boolean overwrite = true;

    /**
     * Creates a new instance of SdlFile
     */
    public SdlFile() { }

    /**
     * Creates a new instance of SdlFile
     * @param fileName a String value representing the name that will be used to store the file in the head unit. You can pass null if you want the library to auto generate the name
     * @param fileType a FileType enum value representing the type of the file
     * @param id an int value representing the android resource id of the file
     * @param persistentFile a boolean value that indicates if the file is meant to persist between sessions / ignition cycles
     */
    public SdlFile(String fileName, @NonNull FileType fileType, int id, boolean persistentFile){
        setName(fileName);
        setType(fileType);
        setResourceId(id);
        setPersistent(persistentFile);
    }

    /**
     * Creates a new instance of SdlFile
     * @param fileName a String value representing the name that will be used to store the file in the head unit. You can pass null if you want the library to auto generate the name
     * @param fileType a FileType enum value representing the type of the file
     * @param uri a URI value representing a file's location. Currently, it only supports local files
     * @param persistentFile a boolean value that indicates if the file is meant to persist between sessions / ignition cycles
     */
    public SdlFile(String fileName, @NonNull FileType fileType, Uri uri, boolean persistentFile){
        setName(fileName);
        setType(fileType);
        setUri(uri);
        setPersistent(persistentFile);
    }

    /**
     * Creates a new instance of SdlFile
     * @param fileName a String value representing the name that will be used to store the file in the head unit. You can pass null if you want the library to auto generate the name
     * @param fileType a FileType enum value representing the type of the file
     * @param data a byte array representing the data of the file
     * @param persistentFile a boolean value that indicates if the file is meant to persist between sessions / ignition cycles
     */
    public SdlFile(String fileName, @NonNull FileType fileType, byte[] data, boolean persistentFile){
        setName(fileName);
        setType(fileType);
        setFileData(data);
        setPersistent(persistentFile);
    }

    /**
     * Creates a new instance of SdlFile
     * @param staticIconName a StaticIconName enum value representing the name of a static file that comes pre-shipped with the head unit
     */
    public SdlFile(@NonNull StaticIconName staticIconName){
        setName(staticIconName.toString());
        setFileData(staticIconName.toString().getBytes());
        setPersistent(false);
        setStaticIcon(true);
    }

    /**
     * Sets the name of the file
     * @param fileName a String value representing the name that will be used to store the file in the head unit. You can pass null if you want the library to auto generate the name
     */
    public void setName(String fileName) {
        if (fileName != null) {
            this.shouldAutoGenerateName = false;
            this.fileName = fileName;
        } else {
            this.shouldAutoGenerateName = true;
            if (this.getFileData() != null) {
                this.fileName = generateFileNameFromData(this.getFileData());
            } else if (this.getUri() != null) {
                this.fileName = generateFileNameFromUri(this.getUri());
            } else if (this.getResourceId() != 0) {
                this.fileName = generateFileNameFromResourceId(this.getResourceId());
            }
        }
    }

    /**
     * Gets the name of the file
     * @return a String value representing the name that will be used to store the file in the head unit
     */
    public String getName(){
        return fileName;
    }

    /**
     * Sets the resource ID of the file
     * @param id an int value representing the android resource id of the file
     */
    public void setResourceId(int id){
        this.id = id;
        if (shouldAutoGenerateName) {
            this.fileName = generateFileNameFromResourceId(id);
        }
    }

    /**
     * Gets the resource id of the file
     * @return an int value representing the android resource id of the file
     */
    public int getResourceId(){
        return id;
    }

    /**
     * Sets the uri of the file
     * @param uri a URI value representing a file's location. Currently, it only supports local files
     */
    public void setUri(Uri uri){
        this.uri = uri;
        if (shouldAutoGenerateName && uri != null) {
            this.fileName = generateFileNameFromUri(uri);
        }
    }

    /**
     * Gets the uri of the file
     * @return a URI value representing a file's location. Currently, it only supports local files
     */
    public Uri getUri(){
        return uri;
    }

    /**
     * Sets the byte array that represents the content of the file
     * @param data a byte array representing the data of the file
     */
    public void setFileData(byte[] data){
        this.fileData = data;
        if (shouldAutoGenerateName && data != null) {
            this.fileName = generateFileNameFromData(data);
        }
    }

    /**
     * Gets the byte array that represents the content of the file
     * @return a byte array representing the data of the file
     */
    public byte[] getFileData(){
        return fileData;
    }

    /**
     * Sets the type of the file
     * @param fileType a FileType enum value representing the type of the file
     */
    public void setType(@NonNull FileType fileType){
        this.fileType = fileType;
    }

    /**
     * Gets the type of the file
     * @return a FileType enum value representing the type of the file
     */
    public FileType getType(){
        return fileType;
    }

    /**
     * Sets whether the file should persist between sessions / ignition cycles
     * @param persistentFile a boolean value that indicates if the file is meant to persist between sessions / ignition cycles
     */
    public void setPersistent(boolean persistentFile){
        this.persistentFile = persistentFile;
    }

    /**
     * Gets whether the file should persist between sessions / ignition cycles
     * @return a boolean value that indicates if the file is meant to persist between sessions / ignition cycles
     */
    public boolean isPersistent(){
        return this.persistentFile;
    }

    /**
     * Sets the the name of the static file. Static files comes pre-shipped with the head unit
     * @param staticIcon a StaticIconName enum value representing the name of a static file that comes pre-shipped with the head unit
     */
    public void setStaticIcon(boolean staticIcon) {
        isStaticIcon = staticIcon;
    }

    /**
     * Gets the the name of the static file. Static files comes pre-shipped with the head unit
     * @return a StaticIconName enum value representing the name of a static file that comes pre-shipped with the head unit
     */
    public boolean isStaticIcon() {
        return isStaticIcon;
    }

    /**
     * Gets the overwrite property for an SdlFile by default its set to true
     * @return a boolean value that indicates if a file can be overwritten.
     */
    public boolean getOverwrite() {
        return overwrite;
    }

    /**
     * Sets the overwrite property for an SdlFile by default its set to true
     * @param overwrite a boolean value that indicates if a file can be overwritten
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Generates a file name from data by hashing the data and returning the last 16 chars
     * @param data a byte array representing the data of the file
     * @return a String value representing the name that will be used to store the file in the head unit
     */
    private String generateFileNameFromData(@NonNull byte[] data) {
        String result;
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hash = new byte[0];
        if (messageDigest != null) {
            hash = messageDigest.digest(data);
        }
        StringBuilder stringBuilder = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            stringBuilder.append(String.format("%02x", b & 0xff));
        }
        String hashString = stringBuilder.toString();
        result = hashString.substring(hashString.length() - 16);
        return result;
    }

    /**
     * Generates a file name from uri by hashing the uri string and returning the last 16 chars
     * @param uri a URI value representing a file's location
     * @return a String value representing the name that will be used to store the file in the head unit
     */
    private String generateFileNameFromUri(@NonNull Uri uri) {
        return generateFileNameFromData(uri.toString().getBytes());
    }

    /**
     * Generates a file name from resourceId by hashing the id and returning the last 16 chars
     * @param id an int value representing the android resource id of the file
     * @return a String value representing the name that will be used to store the file in the head unit
     */
    private String generateFileNameFromResourceId(int id) {
        return generateFileNameFromData("ResourceId".concat(String.valueOf(id)).getBytes());
    }

    /**
     * Used to compile hashcode for SdlFile for use to compare in overridden equals method
     * @return Custom hashcode of SdlFile variables
     */
    @Override
    public int hashCode() {
        int result = 1;
        result += ((getName() == null) ? 0 : Integer.rotateLeft(getName().hashCode(), 1));
        result += ((getUri() == null) ? 0 : Integer.rotateLeft(getUri().hashCode(), 2));
        result += ((getFileData() == null) ? 0 : Integer.rotateLeft(getFileData().hashCode(), 3));
        result += ((getType() == null) ? 0 : Integer.rotateLeft(getType().hashCode(), 4));
        result += Integer.rotateLeft(Boolean.valueOf(isStaticIcon()).hashCode(), 5);
        result += Integer.rotateLeft(Boolean.valueOf(isPersistent()).hashCode(), 6);
        result += Integer.rotateLeft(Integer.valueOf(getResourceId()).hashCode(), 7);
        return result;
    }

    /**
     * Uses our custom hashCode for SdlFile objects
     * @param o - The object to compare
     * @return boolean of whether the objects are the same or not
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        // if this is the same memory address, it's the same
        if (this == o) return true;
        // if this is not an instance of SdlFile, not the same
        if (!(o instanceof SdlFile)) return false;
        // return comparison
        return hashCode() == o.hashCode();
    }
}
