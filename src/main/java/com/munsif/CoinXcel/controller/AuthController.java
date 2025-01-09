package com.munsif.CoinXcel.controller;

import com.munsif.CoinXcel.Model.User;
import com.munsif.CoinXcel.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws Exception {

        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if(isEmailExist != null){
            throw new Exception("Email already exist...");
        }

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFullname(user.getFullname());
        newUser.setPassword(user.getPassword());
        User savedUser = userRepository.save(newUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);



        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);




    }
}
