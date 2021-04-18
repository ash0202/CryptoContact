package com.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.scm.SCMRepository;
import com.scm.entity.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private SCMRepository sr;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//fetching user		
		User user = sr.getUserByUserName(username);
		
		if(user==null)
		{
			throw new UsernameNotFoundException("Could not found user");
		}
		
		CustomUserDetails details=new CustomUserDetails(user);
		
		return details;
				
	}

	
}
