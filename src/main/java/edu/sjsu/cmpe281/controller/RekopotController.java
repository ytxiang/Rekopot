package edu.sjsu.cmpe281.controller;

import java.util.List; 

import com.amazonaws.services.cloudfront.model.Method;
import edu.sjsu.cmpe281.bean.FileUploadForm;
import edu.sjsu.cmpe281.bean.UserBean;
import edu.sjsu.cmpe281.dto.S3RekoPotDTO;
import edu.sjsu.cmpe281.dto.UserDTO;
import edu.sjsu.cmpe281.service.RekopotService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Controller
public class RekopotController {

	@Autowired
	RekopotService rekopotService;

    @RequestMapping("/")
    public String home(Model model) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null)
		    return "redirect:/signon";

	    boolean isAdmin = auth.getAuthorities().stream()
		    .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
	    S3RekoPotDTO fpDto = isAdmin ? rekopotService.getFileList() :
		    rekopotService.getFileList(auth.getName());
	    model.addAttribute("files", fpDto.getFileList());

	    return isAdmin ? "admin" : "index";
    }

	@RequestMapping(value="/register", method = RequestMethod.GET)
    public String register(Model model) {
	return "register";
	}

	@RequestMapping(value="/register", method = RequestMethod.POST)
	public String signUp(Model model, @ModelAttribute UserBean user) {
		try {
			rekopotService.signUpUser(user);
			model.addAttribute("msg", "User " + user.getUsername() + " successfully registered.");
			model.addAttribute("account", user);
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
		}
		return "register";
	}

	@RequestMapping(value = "/file/upload", method = RequestMethod.POST)
	@ResponseBody
	public String fileUpload(
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "forceful", required = false) boolean forceful) {

		if (file.isEmpty()) {
			return "Please select a file to upload!";
		}

		try {
			// Get the file and try to save it to S3
			String userName = SecurityContextHolder.getContext().getAuthentication().getName();
			rekopotService.upload(file, forceful, userName);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "";
	}

	@RequestMapping(value="/file/{id}/update", method = RequestMethod.POST)
	@ResponseBody
	public String updateFile(@PathVariable("id") String id,
			@ModelAttribute FileUploadForm form) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			return "503 Authorization not granted";

		try {
			rekopotService.upload(form.getFile(), Integer.parseInt(id),
			    auth.getName(), form.getForceful());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "";
	}

	@RequestMapping(value="/file/{id}/editnote", method = RequestMethod.POST)
	@ResponseBody
	public String extractFile(
			@PathVariable("id") String id,
			@RequestParam(value = "notes", required = true) String notes) {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			rekopotService.editNotes(Integer.parseInt(id), userName, notes);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "";
	}

	@RequestMapping(value="/file/{id}/extract", method = RequestMethod.POST)
	@ResponseBody
	public String extractFile(
			@PathVariable("id") String id,
			@RequestParam(value = "force", required = false) boolean force) {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			rekopotService.extractFile(Integer.parseInt(id), userName, force);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "";
	}

	@RequestMapping(value="/file/{id}/delete", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteFile(
			@PathVariable("id") String id) {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		rekopotService.deleteFile(Integer.parseInt(id), userName);
		return "";
	}

	@RequestMapping(value="/admin/file/delete", method = RequestMethod.POST)
	public String adminDeleteFiles(
			@RequestParam(value = "idChecked", required = true) List<String> idfiles) {
		if (idfiles != null) {
			for (String idfileStr : idfiles) {
				rekopotService.deleteFile(Integer.valueOf(idfileStr));
			}
		}
		return "redirect:/";
	}

	@RequestMapping(value="logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null){
		new SecurityContextLogoutHandler().logout(request, response, auth);
	    }
	    return "redirect:/";
	}

}
