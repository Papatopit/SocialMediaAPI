package ru.mobile.effective.SocialMediaAPI.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mobile.effective.SocialMediaAPI.entity.Post;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserId(Long userId);
    List<Post> findByUserIdIn(Collection<Long> userIds, Pageable pageable);
}
