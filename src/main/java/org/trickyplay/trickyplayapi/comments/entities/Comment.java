package org.trickyplay.trickyplayapi.comments.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String body;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", referencedColumnName = "id")
    private TPUser author;
    @OneToMany(mappedBy = "parentComment", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reply> replies;
    private String gameName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}