package edu.sjsu.cmpe281.service;

import java.io.IOException;
import javax.xml.bind.ValidationException;

import org.springframework.web.multipart.MultipartFile;

import edu.sjsu.cmpe281.bean.UserBean;
import edu.sjsu.cmpe281.dto.S3FileDTO;
import edu.sjsu.cmpe281.dto.S3RekoPotDTO;
import edu.sjsu.cmpe281.dto.UserDTO;

public interface RekopotService {
	UserDTO getUser(String userName);
	public void signUpUser(UserBean userbean) throws ValidationException;
	public S3RekoPotDTO getFileList();
	public S3RekoPotDTO getFileList(String userName);
	public S3FileDTO upload(MultipartFile mfile, boolean force, String userName)
			throws IllegalStateException, IOException, ValidationException;
	public S3FileDTO upload(MultipartFile mfile, Integer fid, String userName, boolean force)
			throws IllegalStateException, IOException, ValidationException;
	public void deleteFile(Integer fileId);
	public void deleteFile(Integer fileId, String userName);
	public void extractFile(Integer fileId, String userName, boolean force)
			throws ValidationException;
	public void editNotes(Integer fileId, String userName, String notes)
			throws ValidationException;
}
