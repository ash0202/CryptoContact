package com.scm.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.scm.ContactRepo;
import com.scm.Message;
import com.scm.SCMRepository;
import com.scm.entity.Contact;
import com.scm.entity.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private SCMRepository sr;
	@Autowired
	private ContactRepo cr;
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	//method for adding common data response
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {

		String name = principal.getName();
		User user = sr.getUserByUserName(name);
		m.addAttribute("user", user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal)
	{		
		m.addAttribute("title","User Dashboard-Smart Contact Manager");
		m.addAttribute("confirm", null);
		return "/user/user_dashboard";
	}
	
	//add handeler
	@RequestMapping("/addcontact")
	public String openAddContact(Model m) {
		m.addAttribute("title", "Add Contact -Smart Contact Manager");
		m.addAttribute("contact", new Contact());
		return "/user/addcontact";
	}
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileimg")MultipartFile file,Principal principal,Model model,
			HttpSession session) {
		
		try {
			String name=principal.getName();
			User user = sr.getUserByUserName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			System.out.println(contact.getCid());
			//processing and uploading file
			if(file.isEmpty()) {
				model.addAttribute("file", "empty");
				return "/user/addcontact";
			}
			else {
				contact.setImgURL(contact.getCid()+"_"+user.getId()+file.getOriginalFilename());
				
				File file2 = new ClassPathResource("static/image").getFile();
				//byte[] b=file.getBytes();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+contact.getCid()+"_"+user.getId()+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);				
			}
			
			sr.save(user);
			//cr.save(contact);
			session.setAttribute("message", new Message("Contact added successfully", "success"));
		} catch (Exception e) {
			
			session.setAttribute("message", new Message("Something went wrong!!Try again", "danger"));
			e.printStackTrace();
			
		}
		model.addAttribute("file", "");
		return "/user/addcontact";
		
		//System.out.println(contact);
//		
//			try {
//			String name=principal.getName();
//			User user = sr.getUserByUserName(name);
//			contact.setUser(user);
//			user.getContacts().add(contact);
//			cr.save(contact);
//			sr.save(user);
//			return "/user/addcontact";
//			}
//			catch(Exception e) {
//				model.addAttribute("exist", "exist");
//				return "/user/addcontact";
//			}
//		String no=contact.getPhone();
//		String name = principal.getName();
//		User user=sr.getUserByUserName(name);
//		
//		 Optional<String> phone = cr.findByPhone(no,user.getId());
//			if(phone.isEmpty()) {
//				contact.setUser(user);
//				user.getContacts().add(contact);
//				cr.save(contact);
//				sr.save(user);
//				return "/user/addcontact";
//			}
//			else {
//				model.addAttribute("exist", "exist");
//				return "/user/addcontact";
//			}
		}		
	//pagination -- per page 8 contacts current page-0
	@RequestMapping("/showcontacts/{page}")
	public String showContacts(Model m,Principal principal,@PathVariable("page") int page) {
		m.addAttribute("title", "View Your Contact -Smart Contact Manager");
				
				//contact retrive
				String name = principal.getName();
				User user = sr.getUserByUserName(name);
				//List<Contact> contacts = user.getContacts();
				
				int size=user.getContacts().size();
				if(size==0)
				{
					m.addAttribute("count", new Message("Sorry..U don't have any contacts to show..Add some contacts","danger"));
					return "/user/showContacts";
				}
				
				Pageable pageable = PageRequest.of(page, 8);
				Page<Contact> list = cr.findByUser(user.getId(),pageable);
				m.addAttribute("con", list);
				m.addAttribute("currentpage", page);
				m.addAttribute("total", list.getTotalPages());
				m.addAttribute("count", null);
		return "/user/showContacts";
	}
	//showing specific contact details
	@RequestMapping("/contact/{cid}")
	public String contactDetails(@PathVariable("cid")int id,Model m,Principal principal) {
		
		Optional<Contact> findById = cr.findById(id);
		Contact contact = findById.get();
		String name = principal.getName();
		User user = sr.getUserByUserName(name);
		if(user.getId()==contact.getUser().getId())
			m.addAttribute("contact", contact);
		return "/user/contact_details";
	}
	@RequestMapping("/delete/{id}")
	public String delete(@PathVariable("id") int id,Principal principal,HttpSession session) {
		
		String name = principal.getName();
		User user = sr.getUserByUserName(name);
		Optional<Contact> byId = cr.findById(id);
		Contact contact = byId.get();
		if(user.getId()==contact.getUser().getId())       
		{
			cr.deleteById(id);
			session.setAttribute("message", new Message("Contact deleted successfully", "success"));
			return "redirect:/user/showcontacts/0";
		}
		return "/user/showContacts";
	}
	// open update form
	@PostMapping("/updateContact/{cid}")
	public String updateForm(@PathVariable("cid")int id,Model m) {
		m.addAttribute("title", "Update Contact - Smart Contact Manager ");
		Optional<Contact> byId = cr.findById(id);
		Contact contact = byId.get();
		m.addAttribute("contact", contact);
		return "/user/updateform";
	}
	//processing of update in backend
	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Contact contact,@RequestParam("profileimg") MultipartFile file,Model m,
			HttpSession session,Principal principal,@RequestParam("id") int cid) {
		
		Optional<Contact> byId = cr.findById(cid);
		Contact contact2 = byId.get();
		String imgURL = contact2.getImgURL();
		User user = sr.getUserByUserName(principal.getName());
		try {
			
			if(!file.isEmpty()) {
				
				//file delete
				File del = new ClassPathResource("static/image").getFile();
				File delete= new File(del, contact2.getImgURL());
				delete.delete();
				//file rewrite
				File file2 = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+contact.getCid()+"_"+user.getId()+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);	
				contact.setImgURL(imgURL);
				contact.setImgURL(contact.getCid()+"_"+user.getId()+file.getOriginalFilename());
				
			}
			else {
				contact.setImgURL(imgURL);
			}
			contact.setCid(cid);
			//User user = sr.getUserByUserName(principal.getName());
			contact.setUser(user);
			//contact.getUser().setId(user.getId());
			cr.save(contact);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		session.setAttribute("message", new Message("Contact updated successfully", "success"));
		return "redirect:/user/contact/"+contact.getCid();
	}
	@RequestMapping("/profile")
	public String userProfile(Model model,Principal principal) {
		
		User user = sr.getUserByUserName(principal.getName());
		
		model.addAttribute("title", "Your Profile- Smart Contact Manager ");
		
		model.addAttribute("user", user);
		return "/user/profile";
	}
	//open settings controller
	@RequestMapping("/setting")
	public String openSetting(Model m) 
	{
		
		m.addAttribute("title", "Setting - Smart Contact Manager");
		
		return "/user/setting";
	}
	//change password handeler
	@PostMapping("/changePassword")
	public String changePassword(Principal principal,@RequestParam("oldPassword")String old,@RequestParam("newPassword")String newpass,
	@RequestParam("confirm")String confirm,Model m,HttpSession session) {
		
		String name = principal.getName();
		User user = sr.getUserByUserName(name);
		String act=user.getPassword();
	
		if(encoder.matches(old, act)) {
			//change password
			if(newpass.equals(confirm)) {
			user.setPassword(encoder.encode(newpass));
			sr.save(user);
			m.addAttribute("confirm", new Message("Password changed successfully!!", "alert-success"));
			session.setAttribute("equal", null);
			}
			else{
				session.setAttribute("equal", new Message("Entered password and confirm password didn't match ", "alert-danger"));
				return "redirect:/user/setting";
			}
		}
		else{
			m.addAttribute("confirm", new Message("Old password didn't match","alert-danger"));
			return "/user/setting";
		}
		
		return "/user/user_dashboard";
	}
	//payment integration
	@RequestMapping("/donation")
	public String donate(Model m) {
		m.addAttribute("title", "Donate Us-Smart Contact Manager");
		return "/user/donation";
	}
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws RazorpayException 
	{
		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());
		
		RazorpayClient client = new RazorpayClient("rzp_test_tdXePszMuggSb2", "EmQfbahxLnxepBFLvD0DDXUH");
		JSONObject obj=new JSONObject();
		obj.put("amount", amt*100);
		obj.put("currency","INR");
		obj.put("receipt","TXN_12533");
		System.out.println(obj);
		//creating order
		
		Order order=client.Orders.create(obj);
		System.out.println(order);
		
		//saving of data into database
		
		
		
		return order.toString();
	}
}
