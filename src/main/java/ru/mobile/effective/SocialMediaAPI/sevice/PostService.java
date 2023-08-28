package ru.mobile.effective.SocialMediaAPI.sevice;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.mobile.effective.SocialMediaAPI.dto.PostDto;
import ru.mobile.effective.SocialMediaAPI.dto.UserDto;
import ru.mobile.effective.SocialMediaAPI.entity.Post;
import ru.mobile.effective.SocialMediaAPI.entity.User;
import ru.mobile.effective.SocialMediaAPI.repository.PostRepository;
import ru.mobile.effective.SocialMediaAPI.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public void createPost(Post post) {
        postRepository.save(post);
    }

    public PostDto getPostById(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            PostDto postDto = new PostDto();
            postDto.setId(post.getId());
            postDto.setText(post.getText());
            postDto.setTitle(post.getTitle());

            return postDto;
        }

        return null;
    }

    public List<PostDto> getAllPostsByUserId(Long userId) {
        List<Post> posts = postRepository.findAllByUserId(userId);

        List<PostDto> postDtos = new ArrayList<>();

        for (Post post : posts) {
            PostDto postDTO = new PostDto();
            postDTO.setId(post.getId());
            postDTO.setText(post.getText());
            postDTO.setTitle(post.getTitle());

            postDtos.add(postDTO);
        }
        return postDtos;
    }

    public boolean updatePost(Long postId, Post updatedPost) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setTitle(updatedPost.getTitle());
            post.setText(updatedPost.getText());
            postRepository.save(post);
            return true;
        } else {
            return false;
        }
    }

    public boolean deletePost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            postRepository.deleteById(postId);
            return true;
        } else {
            return false;
        }
    }

    public boolean isPostBelongsToUser(Long postId, String username) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return false; // Пост не найден
        }

        User postUser = post.getUser();
        return postUser.getUsername().equals(username);
    }

    public List<Post> getPostsByUsers(Set<User> users, int page, int pageSize) {
        Set<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        return postRepository.findByUserIdIn(userIds, pageable);
    }

    private PostDto convertToPostDto(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setText(post.getText());
        postDto.setTitle(post.getTitle());

        UserDto userDto = new UserDto();
        userDto.setId(post.getUser().getId());
        userDto.setUsername(post.getUser().getUsername());
        userDto.setEmail(post.getUser().getEmail());

        postDto.setUser(userDto);

        return postDto;
    }

    public List<PostDto> getActivityFeed(Long userId, int page, int pageSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<User> subscriptions = user.getSubscribers();

        List<Post> posts = getPostsByUsers(subscriptions, page, pageSize);

        return posts.stream()
                .map(this::convertToPostDto)
                .collect(Collectors.toList());
    }
}
