package edu.sjsu.cmpe281.dao;

import java.util.List;
import edu.sjsu.cmpe281.model.S3File;

public interface S3FileDAO {

	/**
	 * Delete a File
	 *
	 * @param file ID
	 */
	void deleteFile(Integer fid);

	/**
	 * Get File metadata from user ID and file name
	 *
	 * @param user ID
	 * @param fileName
	 * @return
	 */
	S3File getExistingFile(Integer uid, String fileName);

	/**
	 * Update file metadata in database
	 *
	 * @param fileName
	 */
	void createOrUpdate(S3File file);

	/**
	 * Retrieve all files in database
	 * @return
	 */
	List<S3File> getAllS3File();

	/**
	 * Retrieve all files for a particular user
	 * @param user ID
	 * @return
	 */
	List<S3File> getAllS3File(Integer uid);

	/**
	 * Get an S3 file object from file ID
	 * @param file ID
	 * @return file object
	 */
	public S3File getFileById(Integer fid);

	/**
	 * Get file name from file ID
	 * @param file ID
	 * @return file name
	 */
	public String getFileName(Integer fid);

	/**
	 * Get user who owns a file
	 * @param file ID
	 * @return user name
	 */
	public String getFileOwner(Integer fid);
}
