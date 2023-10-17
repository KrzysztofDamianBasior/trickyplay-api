package org.trickyplay.trickyplayapi.comments.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.comments.controllers.CommentsController;
import org.trickyplay.trickyplayapi.comments.dtos.*;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.records.CommentPageArgs;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.general.exceptions.CommentNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentsService {
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final TPUserRepository tPUserRepository;

    public GetCommentsResponse getCommentsByGameName(CommentPageArgs commentPageArgs) {
        Pageable pageable = PageRequest.of(
                commentPageArgs.pageNumber(),
                commentPageArgs.pageSize(),
                commentPageArgs.orderDirection(),
                commentPageArgs.sortBy()
        );
        Page<Comment> commentPage = commentRepository.findAllByGameName(commentPageArgs.gameName(), pageable);
//        if (!commentPage.hasContent()) {
//            log.info("no content in comment page, game: {}, page: {}", gameName, pageNumber);
//        }

        List<CommentRepresentation> comments = CommentUtils.mapToCommentDTOs(commentPage.getContent());

        GetCommentsResponse commentsResponse = GetCommentsResponse.builder()
                .comments(comments)
                .pageSize(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .pageSize(commentPage.getSize())
                .pageNumber(commentPage.getNumber())
                .isLast(commentPage.isLast())
                .build();
        commentsResponse.add(linkTo(methodOn(CommentsController.class)
                .getCommentsByGameName(
                        commentPageArgs.gameName(),
                        commentPageArgs.pageNumber(),
                        commentPageArgs.pageSize(),
                        commentPageArgs.sortBy(),
                        commentPageArgs.orderDirection().name())
        )
                .withSelfRel());
        return commentsResponse;
    }

    public GetCommentsResponse getCommentsByAuthorId(long authorId, int pageNumber, int pageSize, String sortBy, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortBy);
        Page<Comment> commentPage = commentRepository.findAllByAuthorId(authorId, pageable);
        List<CommentRepresentation> comments = CommentUtils.mapToCommentDTOs(commentPage.getContent());

        return GetCommentsResponse.builder()
                .comments(comments)
                .pageSize(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .pageSize(commentPage.getSize())
                .pageNumber(commentPage.getNumber())
                .isLast(commentPage.isLast())
                .build();
    }

    public CommentRepresentation getSingleComment(long id) {
        return commentRepository.findById(id)
                .map(CommentUtils::mapToCommentDTO)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    public List<Comment> getCommentsWithReplies(int pageNumber, int pageSize, String sortBy, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortBy);
        Page<Comment> commentPage = commentRepository.findAll(pageable);

        List<Comment> comments = commentPage.getContent();
        List<Long> ids = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Reply> replies = replyRepository.findAllByParentCommentIdIn(ids);
        comments.forEach(comment -> comment.setReplies(
                        replies.stream()
                                .filter(reply -> reply.getParentComment().getId().equals(comment.getId()))
                                .collect(Collectors.toList())
                )
        );
        return comments;
    }

    public CommentRepresentation addComment(
            TPUserPrincipal principalRequestingToAddResource,
            AddCommentRequest addCommentRequest
    ) {
        Comment comment = Comment.builder()
                .body(addCommentRequest.getBody())
                .author(tPUserRepository.getReferenceById(principalRequestingToAddResource.getId()))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .gameName(addCommentRequest.getGameName())
                .build();
        Comment savedComment = commentRepository.save(comment);
        return CommentUtils.mapToCommentDTO(savedComment);
    }

    @Transactional
    public CommentRepresentation editComment(
            TPUserPrincipal principalRequestingToEditResource,
            long idOfTheResourceToBeEdited,
            EditCommentRequest commentRequest
    ) {
        Comment commentToEdit = commentRepository.findById(idOfTheResourceToBeEdited)
                .orElseThrow(() -> new CommentNotFoundException(idOfTheResourceToBeEdited));
        TPUser commentAuthor = commentToEdit.getAuthor();
        System.out.println(commentAuthor.getId());
        System.out.println(principalRequestingToEditResource.getId());
        System.out.println(commentAuthor.getId().equals(principalRequestingToEditResource.getId()));
        if (commentAuthor.getId().equals(principalRequestingToEditResource.getId())) {
            commentToEdit.setBody(commentRequest.getNewCommentBody());
            commentToEdit.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            return CommentUtils.mapToCommentDTO(commentToEdit);
        } else {
            throw new OperationNotAllowedException("You do not have permission to perform actions on this resource");
        }
    }

    public DeleteCommentResponse deleteComment(
            TPUserPrincipal principalRequestingToDeleteResource,
            long idOfTheResourceToBeDeleted
    ) {
        Comment commentToDelete = commentRepository.findById(idOfTheResourceToBeDeleted)
                .orElseThrow(() -> new CommentNotFoundException(idOfTheResourceToBeDeleted));
        TPUser commentAuthor = commentToDelete.getAuthor();
        if (commentAuthor.getId().equals(principalRequestingToDeleteResource.getId()) || principalRequestingToDeleteResource.getRole() == Role.ADMIN) {
            commentRepository.deleteById(idOfTheResourceToBeDeleted);
            return DeleteCommentResponse.builder()
                    .message("Comment successfully removed")
                    .build();
        } else {
            throw new OperationNotAllowedException("You do not have permission to perform actions on this resource");
        }
    }
}
