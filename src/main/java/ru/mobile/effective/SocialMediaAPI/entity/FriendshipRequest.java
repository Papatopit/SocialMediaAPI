package ru.mobile.effective.SocialMediaAPI.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "friendship_requests")
public class FriendshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status; // Enum - PENDING, ACCEPTED, REJECTED

    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}