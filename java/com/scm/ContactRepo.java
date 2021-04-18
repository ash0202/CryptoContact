package com.scm;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.scm.entity.Contact;
import com.scm.entity.User;

public interface ContactRepo extends JpaRepository<Contact, Integer> {

	
	//@Query(value = "select phone from contact where user_id=:id and phone:=no",nativeQuery = true)
//	@Query("select c.phone from Contact c where c.user.id=:id and phone:=no")
//	Optional<String> findByPhone(@Param("no")String no,@Param("id")int uid);
	@Query("from Contact as c where c.user.id=:id")
	public Page<Contact> findByUser(@Param("id")int id,Pageable pageable);
	//searching of contact
	public List<Contact> findByNameContainingAndUser(String  keyword,User user);
	
}
