package ru.mobile.effective.SocialMediaAPI.sevice;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.mobile.effective.SocialMediaAPI.entity.FriendshipRequest;
import ru.mobile.effective.SocialMediaAPI.entity.User;
import ru.mobile.effective.SocialMediaAPI.repository.FriendshipRequestRepository;
import ru.mobile.effective.SocialMediaAPI.repository.UserRepository;

import java.util.Optional;

@Service
public class FriendshipRequestService {
    private final FriendshipRequestRepository friendshipRequestRepository;
    private final UserRepository userRepository;

    public FriendshipRequestService(FriendshipRequestRepository friendshipRequestRepository, UserRepository userRepository) {
        this.friendshipRequestRepository = friendshipRequestRepository;
        this.userRepository = userRepository;
    }

    public void sendFriendRequest(User senderUsername, User receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername.getUsername());
        User receiver = userRepository.findByUsername(receiverUsername.getUsername());

        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender or receiver does not exist");
        }

        FriendshipRequest request = new FriendshipRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendshipRequest.RequestStatus.PENDING);

        sender.getSubscribers().add(receiver); // sender is now subscribing to receiver
        userRepository.save(sender);

        friendshipRequestRepository.save(request);
    }

    public void acceptFriendRequest(Long requestId, User receiver) {
        FriendshipRequest request = friendshipRequestRepository.findById(requestId).orElse(null);

        if (request == null || request.getStatus() != FriendshipRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Invalid request");
        }

        User sender = request.getSender();

        receiver.getFriends().add(sender); // receiver is now friends with sender
        sender.getFriends().add(receiver); // sender is also friends with receiver

        userRepository.save(receiver);
        userRepository.save(sender);

        request.setStatus(FriendshipRequest.RequestStatus.ACCEPTED);
        friendshipRequestRepository.save(request);
    }

    public void rejectFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long requestId) {

        String receiverUsername = userDetails.getUsername();
        FriendshipRequest request = friendshipRequestRepository.findById(requestId).orElse(null);

        if (request == null || request.getStatus() != FriendshipRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Invalid request");
        }

        User receiver = userRepository.findByUsername(receiverUsername);

        if (!request.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("Unauthorized to reject this request");
        }

        request.setStatus(FriendshipRequest.RequestStatus.REJECTED);
        friendshipRequestRepository.save(request);
    }

    public void cancelFriendRequest(User sender, Optional<User> receiver) {
        FriendshipRequest request = friendshipRequestRepository.findBySenderAndReceiver(sender, receiver);

        if (request == null || request.getStatus() != FriendshipRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Invalid friend request");
        }

        friendshipRequestRepository.delete(request);

        sender.getSubscribers().remove(receiver);

        userRepository.save(sender);
    }
    public void removeFriend(User currentUser, User friendToRemove) {

        currentUser.getFriends().remove(friendToRemove);
        friendToRemove.getFriends().remove(currentUser);

        currentUser.getSubscribers().remove(friendToRemove);

        userRepository.save(currentUser);
        userRepository.save(friendToRemove);
    }
}