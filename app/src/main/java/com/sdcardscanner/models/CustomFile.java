package com.sdcardscanner.models;

import org.parceler.Parcel;

/**
 * Created by akutty on 2/19/16.
 */
@Parcel
public class CustomFile implements Comparable<CustomFile> {

    private String fileName;
    private float fileSize;
    private String filePath;


    private String fileExtension;
    private int frequency;

    public CustomFile() {
        /*Required Empty Bean Constuctor*/
    }

    public CustomFile(String fileName, String path, float fileSize, String fileExtension) {
        this.fileName = fileName;
        this.filePath = path;
        this.fileSize = fileSize;
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }


    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public float getFileSize() {
        return fileSize;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }


    @Override
    public String toString() {
        return "File Name : " + this.getFileName() + " :: " + "File Size : " + this.getFileSize() + " kb";
    }

    @Override
    public int compareTo(CustomFile customFile) {
        return (this.fileSize > customFile.fileSize ? -1 : (this.fileSize == customFile.fileSize ? 0 : 1));
    }


}
