package ru.mobile.effective.SocialMediaAPI.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.mobile.effective.SocialMediaAPI.dto.PostDto;
import ru.mobile.effective.SocialMediaAPI.entity.Post;
import ru.mobile.effective.SocialMediaAPI.entity.User;
import ru.mobile.effective.SocialMediaAPI.sevice.PostService;
import ru.mobile.effective.SocialMediaAPI.sevice.UserService;

@RestController
@RequestMapping("/api/posts")
@Api("PostController")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping
    @ApiOperation(value = "Create post", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> createPost(@RequestBody Post post, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to create a post.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.getUserByUsername(userDetails.getUsername());
        post.setUser(user);
        postService.createPost(post);
        return ResponseEntity.ok("Post created successfully.");
    }

    @GetMapping("/{postId}")
    @ApiOperation(value = "Get post by ID", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<Object> getPostById(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to view the post.");
        }

        PostDto postDto = postService.getPostById(postId);
        if (postDto != null) {
            return ResponseEntity.ok(postDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{postId}")
    @ApiOperation(value = "Update post by ID", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> updatePost(@PathVariable Long postId, @RequestBody Post updatedPost, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to update the post.");
        }

        // Получить имя пользователя из аутентификации
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Проверить, принадлежит ли пост пользователю
        boolean isPostBelongsToUser = postService.isPostBelongsToUser(postId, username);
        if (!isPostBelongsToUser) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this post.");
        }

        boolean success = postService.updatePost(postId, updatedPost);
        if (success) {
            return ResponseEntity.ok("Post updated successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{postId}")
    @ApiOperation(value = "Delete post by ID", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> deletePost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to delete the post.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Проверить, принадлежит ли пост пользователю
        boolean isPostBelongsToUser = postService.isPostBelongsToUser(postId, username);
        if (!isPostBelongsToUser) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this post.");
        }

        boolean success = postService.deletePost(postId);
        if (success) {
            return ResponseEntity.ok("Post deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}