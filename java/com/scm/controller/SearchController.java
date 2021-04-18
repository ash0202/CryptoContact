package com.scm.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.scm.ContactRepo;
import com.scm.SCMRepository;
import com.scm.entity.Contact;
import com.scm.entity.User;

@RestController
public class SearchController {

	@Autowired
	private SCMRepository sr;
	@Autowired
	private ContactRepo cr;
	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<List<Contact>> search(Principal principal,@PathVariable("query") String query){
		System.out.println(query);
		User user = sr.getUserByUserName(principal.getName());
		List<Contact> list = cr.findByNameContainingAndUser(query, user);
		return ResponseEntity.ok(list);
	}
}
