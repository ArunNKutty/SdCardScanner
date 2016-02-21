package com.sdcardscanner.models;

/**
 * Created by akutty on 2/21/16.
 */
public class FileExtension extends CustomFile {

    @Override
    public String toString() {
        return "File Extension : " + getFileExtension() + " :: " + "Frequency : " + getFrequency();
    }
}
