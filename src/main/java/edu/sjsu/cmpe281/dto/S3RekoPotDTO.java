package edu.sjsu.cmpe281.dto;

import java.io.Serializable;
import java.util.ArrayList;

public class S3RekoPotDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	ArrayList<S3FileDTO> fileList;

	public S3RekoPotDTO() {
	}

	public S3RekoPotDTO(ArrayList<S3FileDTO> fileList) {
		super();
		this.fileList = fileList;
	}

	public ArrayList<S3FileDTO> getFileList() {
		return fileList;
	}

	public void setFileList(ArrayList<S3FileDTO> fileList) {
		this.fileList = fileList;
	}

	@Override
	public String toString() {
		return "S3RekoPotDTO [fileList=" + fileList + "]";
	}

}
