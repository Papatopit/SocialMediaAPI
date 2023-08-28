package ru.mobile.effective.SocialMediaAPI.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.mobile.effective.SocialMediaAPI.entity.User;
import ru.mobile.effective.SocialMediaAPI.sevice.FriendshipRequestService;
import ru.mobile.effective.SocialMediaAPI.sevice.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/friend-requests")
//@Api("FriendshipRequestController")
public class FriendshipRequestController {
    private final FriendshipRequestService friendshipRequestService;
    private final UserService userService;

    public FriendshipRequestController(FriendshipRequestService friendshipRequestService, UserService userService) {
        this.friendshipRequestService = friendshipRequestService;
        this.userService = userService;
    }

    @PostMapping("/send/{receiverId}")
    @ApiOperation(value = "Send a friend request", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long receiverId
    ) {
        String senderUsername = userDetails.getUsername();
        Optional<User> receiverOptional = userService.getUserById(receiverId);

        if (receiverOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User sender = userService.getUserByUsername(senderUsername);
        User receiver = receiverOptional.get();

        friendshipRequestService.sendFriendRequest(sender, receiver);
        return ResponseEntity.ok("Friendship request sent.");
    }

    @PostMapping("/accept/{requestId}")
    @ApiOperation(value = "Accept friend request", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> acceptFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable Long requestId) {

        String receiverUsername = userDetails.getUsername();
        User receiver = userService.getUserByUsername(receiverUsername);
        friendshipRequestService.acceptFriendRequest(requestId, receiver);

        return ResponseEntity.ok("Friendship request accept.");
    }

    @PostMapping("/reject/{requestId}")
    @ApiOperation(value = "Reject friend request", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> rejectFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long requestId) {

        String receiverUsername = userDetails.getUsername();
        friendshipRequestService.rejectFriendRequest(userDetails, requestId);

        return ResponseEntity.ok("Friendship request reject.");
    }

    @PostMapping("/cancel/{receiverId}")
    @ApiOperation(value = "Cancel friend request", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> cancelFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable Long receiverId) {

        String senderUsername = userDetails.getUsername();
        User sender = userService.getUserByUsername(senderUsername);
        Optional<User> receiver = userService.getUserById(receiverId);

        friendshipRequestService.cancelFriendRequest(sender, receiver);

        return ResponseEntity.ok("Friend request cancelled successfully");
    }

    @DeleteMapping("/{friendId}")
    @ApiOperation(value = "Delete friend", authorizations = @Authorization(value = "Bearer"))
    public ResponseEntity<String> removeFriend(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long friendId) {

        String currentUserUsername = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(currentUserUsername);
        User friendToRemove = userService.getUserById(friendId).orElse(null);

        if (friendToRemove == null) {
            return ResponseEntity.notFound().build();
        }

        friendshipRequestService.removeFriend(currentUser, friendToRemove);

        return ResponseEntity.ok("Friend removed successfully");
    }
}