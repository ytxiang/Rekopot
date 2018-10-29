package edu.sjsu.cmpe281.bean;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadForm {

    private boolean forceful;

    private MultipartFile file;

    public boolean getForceful() {
	return forceful;
    }

    public void setForceful(boolean forceful) {
	this.forceful = forceful;
    }

    public MultipartFile getFile() {
	return file;
    }

    public void setFile(MultipartFile file) {
	this.file = file;
    }

}
