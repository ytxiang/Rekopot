package edu.sjsu.cmpe281.dao;

import java.util.List;
import edu.sjsu.cmpe281.model.User;

public interface UserDAO {

	/**
	 * Fetch all users
	 * @return
	 */
	public List<User> getAllUser();

	/**
	 * Fetch user based on credentials
	 * @param userName
	 * @param password
	 * @return
	 */
	public User getUser(String userName, String password);

	/**
	 * Fetch user by username
	 * @param userName
	 * @return
	 */
	public User getUserByUserName(String userName);

	/**
	 * method to create user object
	 * @param user
	 */
	public void createUser(User user);
}
