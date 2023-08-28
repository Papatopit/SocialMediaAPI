package ru.mobile.effective.SocialMediaAPI.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.mobile.effective.SocialMediaAPI.config.security.jwt.JwtUtil;
import ru.mobile.effective.SocialMediaAPI.dto.PostDto;
import ru.mobile.effective.SocialMediaAPI.entity.User;
import ru.mobile.effective.SocialMediaAPI.sevice.PostService;
import ru.mobile.effective.SocialMediaAPI.sevice.UserService;

import java.util.List;

@RestController
@RequestMapping("/api")
//@Api("UserController")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PostService postService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    @ApiOperation("Register a new user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    @ApiOperation("Authenticate and generate JWT token")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(token);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/users/{userId}/posts")
    @ApiOperation("Get all posts by user ID")
    public ResponseEntity<List<PostDto>> getAllPosts(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<PostDto> postDTOs = postService.getAllPostsByUserId(userId);
        return ResponseEntity.ok(postDTOs);
    }

    @GetMapping("/{userId}/feed")
    @ApiOperation("Get activity feed for a user")
    public ResponseEntity<List<PostDto>> getActivityFeed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<PostDto> activityFeed = postService.getActivityFeed(userId, page, pageSize);
        return ResponseEntity.ok(activityFeed);
    }
}