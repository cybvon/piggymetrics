package com.piggymetrics.auth.service;

import com.piggymetrics.auth.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {

	void create(User user);

	UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
