package edu.sjsu.cmpe281.service;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.ValidationException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.StringUtils;

import edu.sjsu.cmpe281.model.S3File;
import edu.sjsu.cmpe281.model.User;
import edu.sjsu.cmpe281.bean.UserBean;
import edu.sjsu.cmpe281.dao.S3FileDAO;
import edu.sjsu.cmpe281.dao.UserDAO;
import edu.sjsu.cmpe281.dto.S3FileDTO;
import edu.sjsu.cmpe281.dto.S3RekoPotDTO;
import edu.sjsu.cmpe281.dto.UserDTO;
import edu.sjsu.cmpe281.service.LicensePlateParser;

@Service
@SuppressWarnings("deprecation")
public class RekopotServiceImpl implements RekopotService {

	@Autowired
	UserDAO userDao;

	@Autowired
	S3FileDAO s3FileDao;

	@Value("${rekopot.s3.region}")
	private String S3_REGION;

	@Value("${rekopot.s3.bucket}")
	private String S3_BUCKET;

	@Value("${rekopot.s3.namespace}")
	private String S3_NAMESPACE;

	@Value("${rekopot.s3.baseurl}")
	private String S3_BASEURL;

	@Value("${rekopot.s3.access.id}")
	private String S3_ACCESS;

	@Value("${rekopot.s3.access.secret}")
	private String S3_SECRET;

        @Value("${rekopot.aws.region}")
        private String REKO_REGION;

	private AmazonS3Client s3Client;

	private AWSCredentialsProvider credProvider;

	private void initS3Client() {
		// fetch access keys from property file
		AWSCredentials cred = new BasicAWSCredentials(S3_ACCESS, S3_SECRET);

		// instantiate S3 client
		s3Client = new AmazonS3Client(cred);

		// Set S3 region
		s3Client.setRegion(Region.getRegion(Regions.fromName(S3_REGION)));

		// Enable Transfer Acceleration Mode
		//s3Client.setS3ClientOptions(S3ClientOptions.builder().setAccelerateModeEnabled(true).build());

		// Instantiate a static credential provider
		credProvider = new AWSStaticCredentialsProvider(cred);
	}

	@PostConstruct
	private void postInit() {
		initS3Client();
	}

	@Override
	public UserDTO getUser(String userName) {
		User user = userDao.getUserByUserName(userName);

		if (user == null) {
			return null;
		}
		UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(),
				user.getFullName(), user.getNick());
		return userDTO;
	}

	@Override
	public void signUpUser(UserBean userbean) throws ValidationException {

		if (userbean.getUsername() == null || userbean.getUsername().equals("")
				|| userbean.getPassword() == null || userbean.getPassword().equals("")
				|| userbean.getConfirm() == null || userbean.getConfirm().equals("")
				|| userbean.getFullname() == null || userbean.getFullname().equals(""))
			throw new ValidationException("Must provide all required information for registration");

		if (userDao.getUserByUserName(userbean.getUsername()) != null)
			throw new ValidationException("User " + userbean.getUsername() +
					" already exists!");

		if (!userbean.getPassword().equals(userbean.getConfirm()))
			throw new ValidationException("Unmatched passwords, please re-enter");

		User user = new User();
		user.setUsername(userbean.getUsername());
		user.setPassword(userbean.getPassword());
		user.setFullName(userbean.getFullname());
		user.setNick(userbean.getNick());
		userDao.createUser(user);
	}

	@Override
	public S3FileDTO upload(MultipartFile mfile, boolean force, String userName)
			throws IllegalStateException, IOException, ValidationException {

		String fileName = mfile.getOriginalFilename();

		return upload(mfile, fileName, userName, force);
	}

	@Override
	public S3FileDTO upload(MultipartFile mfile, Integer fid, String userName, boolean force)
			throws IllegalStateException, IOException, ValidationException {

		String fileName = s3FileDao.getFileName(fid);

		return upload(mfile, fileName, userName, force);
	}

	private S3FileDTO upload(MultipartFile mfile, String fileName, String userName, boolean force)
			throws IllegalStateException, IOException, ValidationException {

		final int maxFileSize = 10485760;
		boolean updateNotes = true;
		String notes = "";

		if (mfile.getSize() > maxFileSize)
			throw new ValidationException("File size should not be over 10MB.");

		/* Assemble to a single file */
		File file = File.createTempFile(fileName, "");
		mfile.transferTo(file);

		LicensePlateParser licensePlateParser = new LicensePlateParser(credProvider,
			Regions.fromName(REKO_REGION));
		if (licensePlateParser.isLicensePlate(file) != 1.0f) {
			if (!force) {
				throw new ValidationException(
				    "Sorry, we're unable to detect license plate within " +
				    "the image that was just added. You may disregard " +
				    "this error if you think it's a false alarm by " +
				    "checking the ''Save Unknown'' box to proceed. " +
				    "You may also click on the 'Extract' button later " +
				    "to re-run the detection service on the image file, " +
				    "with the caution that the result might be skewed.");
			}
			updateNotes = false;
		}

		String key = new String();
		if (!S3_NAMESPACE.trim().equals("")) {
			 key = S3_NAMESPACE.endsWith("/") ? S3_NAMESPACE : S3_NAMESPACE + "/";
		}
		key += userName + "/" + fileName;

		/* Upload to S3 */
		@SuppressWarnings("unused")
		PutObjectResult poResult = s3Client.putObject(S3_BUCKET, key, file);

		if (updateNotes) {

			String plateNumber = licensePlateParser.detectLicensePlateNumber(S3_BUCKET, key);
			if (!plateNumber.trim().equals("")) {
				notes = new String(plateNumber);
			} else {
				notes = "<empty license plate number>";
			}
		}

		/* Create DB entry if new file or else update */
		S3FileDTO s3FileDTO = createOrUpdateS3File(fileName, key, userName,
				mfile.getSize(), notes);
		return s3FileDTO;
	}

	private S3FileDTO createOrUpdateS3File(String fileName, String path, String userName,
			Long size, String notes) {

		User user = userDao.getUserByUserName(userName);

		S3File userFile = s3FileDao.getExistingFile(user.getId(), fileName);

		if (null == userFile) {
			userFile = new S3File();
			userFile.setFileName(fileName);
			userFile.setCreatedTime(new Timestamp(System.currentTimeMillis()));
			userFile.setModifiedTime(new Timestamp(System.currentTimeMillis()));
			userFile.setNotes(notes);
			userFile.setSize(FileUtils.byteCountToDisplaySize(size));
			userFile.setPath(path);
			userFile.setUser(user);
		} else {
			userFile.setModifiedTime(new Timestamp(System.currentTimeMillis()));
			userFile.setSize(FileUtils.byteCountToDisplaySize(size));
			if (!StringUtils.isNullOrEmpty(notes)) {
				userFile.setNotes(notes);
			}
		}

		s3FileDao.createOrUpdate(userFile);
		S3File returnUserFile = s3FileDao.getExistingFile(user.getId(), fileName);
		S3FileDTO s3FileDTO = new S3FileDTO(returnUserFile.getId(),
				returnUserFile.getFileName(), returnUserFile.getNotes(), returnUserFile.getSize(),
				S3_BASEURL + "/" + returnUserFile.getPath(),
				returnUserFile.getCreatedTime(), returnUserFile.getModifiedTime(),
				returnUserFile.getUser().getFullName());

		return s3FileDTO;
	}

	private S3RekoPotDTO getAllFiles(List<S3File> s3Files) {
		S3RekoPotDTO filesDTO = new S3RekoPotDTO();
		ArrayList<S3FileDTO> fileList = new ArrayList<S3FileDTO>();

		if (s3Files!= null && !CollectionUtils.isEmpty(s3Files)) {
			for (S3File f : s3Files) {
				S3FileDTO s3FileDTO = new S3FileDTO(f.getId(), f.getFileName(),
						f.getNotes(), f.getSize(), S3_BASEURL + "/" + f.getPath(),
						f.getCreatedTime(), f.getModifiedTime(),
						f.getUser().getFullName());
				fileList.add(s3FileDTO);
			}
		}

		filesDTO.setFileList(fileList);
		return filesDTO;
	}

	@Override
	public S3RekoPotDTO getFileList() {
		List<S3File> s3Files = s3FileDao.getAllS3File();

		return getAllFiles(s3Files);
	}

	@Override
	public S3RekoPotDTO getFileList(String userName) {
		User user = userDao.getUserByUserName(userName);

		List<S3File> s3Files = s3FileDao.getAllS3File(user.getId());
		return getAllFiles(s3Files);
	}

	@Override
	public void deleteFile(Integer fileId) {

		// delete file from S3
		s3Client.deleteObject(S3_BUCKET, s3FileDao.getFileById(fileId).getPath());

		// delete entry from RDS table
		s3FileDao.deleteFile(fileId);
	}

	@Override
	public void deleteFile(Integer fileId, String userName) {
		String owner = s3FileDao.getFileOwner(fileId);

		if (userName.equals(owner))
			deleteFile(fileId);
	}

	private S3File getS3FileIfMatched(Integer fileId, String userName)
			throws ValidationException {

		S3File file = s3FileDao.getFileById(fileId);
		if (null == file) {
			throw new ValidationException("Image file not found.");
		}

		User user = userDao.getUserByUserName(userName);
		if (null == user) {
			throw new ValidationException("Account does not exist or already been deleted.");
		}

		S3File userFile = s3FileDao.getExistingFile(user.getId(), file.getFileName());
		if (null == userFile) {
			throw new ValidationException("Image does not belong to the current logged-in user.");
		}

		return userFile;
	}

	@Override
	public void editNotes(Integer fileId, String userName, String notes)
			throws ValidationException {

		S3File userFile = getS3FileIfMatched(fileId, userName);
		
		userFile.setNotes(notes);
		s3FileDao.createOrUpdate(userFile);
	}

	@Override
	public void extractFile(Integer fileId, String userName, boolean force) throws ValidationException {

		LicensePlateParser licensePlateParser = new LicensePlateParser(credProvider, Regions.fromName(REKO_REGION));
		S3File userFile = getS3FileIfMatched(fileId, userName);

		if (!force && licensePlateParser.isLicensePlate(S3_BUCKET, userFile.getPath()) == 0.0f) {
			throw new ValidationException("Tried hard but still cannot detect license plate in the image, sorry..\n" +
						      "Maybe an unsupported or undetectable license plate out of the States?");
		}

		String plateNumber = licensePlateParser.detectLicensePlateNumber(S3_BUCKET, userFile.getPath());
		if (!StringUtils.isNullOrEmpty(plateNumber)) {
			userFile.setNotes(plateNumber);
		} else {
			throw new ValidationException("Empty Plate Number");
		}
		s3FileDao.createOrUpdate(userFile);
	}

}
