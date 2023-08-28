package ru.mobile.effective.SocialMediaAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mobile.effective.SocialMediaAPI.entity.FriendshipRequest;
import ru.mobile.effective.SocialMediaAPI.entity.User;

import java.util.Optional;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {
    FriendshipRequest findBySenderAndReceiver(User sender, Optional<User> receiver);
}