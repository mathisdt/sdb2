package org.zephyrsoft.sdb2.model;

import java.io.File;
import java.util.UUID;

public class ImageSong extends Song {
    private final File file;
    private int rotateRight = 0;

    public ImageSong(final File file) {
        super(UUID.randomUUID().toString());
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public int getRotateRight() {
        return rotateRight;
    }

    public void setRotateRight(final int rotateRight) {
        this.rotateRight = rotateRight;
    }
    @Override
    public String getTitle() {
        return file != null ? file.getName() : "empty";
    }

    @Override
    public String getLyrics() {
        return "";
    }

}
