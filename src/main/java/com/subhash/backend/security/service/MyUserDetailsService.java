package com.subhash.backend.security.service;

import com.subhash.backend.model.Userr;
import com.subhash.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Userr user=userRepository.findByUserName(username).orElseThrow(()-> new RuntimeException("user not found with username:"+username));
        if(user == null){
            throw new RuntimeException("user not found");
        }
        return new MyUserDetails(user);
    }
}
