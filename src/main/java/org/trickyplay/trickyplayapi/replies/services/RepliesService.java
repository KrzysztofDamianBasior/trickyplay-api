package org.trickyplay.trickyplayapi.replies.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.general.exceptions.ReplyNotFoundException;
import org.trickyplay.trickyplayapi.replies.controllers.RepliesController;
import org.trickyplay.trickyplayapi.replies.dtos.*;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.records.RepliesPageArgs;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.controllers.UsersController;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepliesService {
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final TPUserRepository tPUserRepository;

    public GetRepliesResponse getRepliesByParentCommentId(long parentCommentId, RepliesPageArgs repliesPageArgs) {
        Pageable pageable = PageRequest.of(
                repliesPageArgs.pageNumber(),
                repliesPageArgs.pageSize(),
                repliesPageArgs.orderDirection(),
                repliesPageArgs.sortBy()
        );
        Page<Reply> replyPage = replyRepository.findAllByParentCommentId(parentCommentId, pageable);
        List<ReplyRepresentation> replies = ReplyUtils.mapToReplyDTOs(replyPage.getContent());

        GetRepliesResponse getRepliesResponse = GetRepliesResponse.builder()
                .replies(replies)
                .pageSize(replyPage.getSize())
                .totalElements(replyPage.getTotalElements())
                .totalPages(replyPage.getTotalPages())
                .pageSize(replyPage.getSize())
                .pageNumber(replyPage.getNumber())
                .isLast(replyPage.isLast())
                .build();

        getRepliesResponse.add(linkTo(methodOn(RepliesController.class)
                .getRepliesByParentCommentId(
                        parentCommentId,
                        repliesPageArgs.pageNumber(),
                        repliesPageArgs.pageSize(),
                        repliesPageArgs.sortBy(),
                        repliesPageArgs.orderDirection().name()
                )).withSelfRel());
        return getRepliesResponse;
    }

    public GetRepliesResponse getRepliesByAuthorId(long authorId, RepliesPageArgs repliesPageArgs) {
        Pageable pageable = PageRequest.of(
                repliesPageArgs.pageNumber(),
                repliesPageArgs.pageSize(),
                repliesPageArgs.orderDirection(),
                repliesPageArgs.sortBy()
        );
        Page<Reply> replyPage = replyRepository.findAllByAuthorId(authorId, pageable);
        List<ReplyRepresentation> replies = ReplyUtils.mapToReplyDTOs(replyPage.getContent());

        GetRepliesResponse getRepliesResponse = GetRepliesResponse.builder()
                .replies(replies)
                .pageSize(replyPage.getSize())
                .totalElements(replyPage.getTotalElements())
                .totalPages(replyPage.getTotalPages())
                .pageSize(replyPage.getSize())
                .pageNumber(replyPage.getNumber())
                .isLast(replyPage.isLast())
                .build();

        getRepliesResponse.add(linkTo(methodOn(UsersController.class)
                .getUserReplies(
                        authorId,
                        repliesPageArgs.pageNumber(),
                        repliesPageArgs.pageSize(),
                        repliesPageArgs.sortBy(),
                        repliesPageArgs.orderDirection().name()
                )).withSelfRel());
        return getRepliesResponse;

    }

    public ReplyRepresentation getSingleReply(long id) {
        return replyRepository.findById(id).map(ReplyUtils::mapToReplyDTO)
                .orElseThrow(() -> new ReplyNotFoundException(id));
    }

    public ReplyRepresentation addReply(
            TPUserPrincipal principalRequestingToAddResource,
            AddReplyRequest addReplyRequest) {
        Reply reply = Reply.builder()
                .body(addReplyRequest.getBody())
                .author(tPUserRepository.getReferenceById(principalRequestingToAddResource.getId()))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .parentComment(commentRepository.getReferenceById(addReplyRequest.getParentCommentId()))
                .build();
        Reply savedReply = replyRepository.save(reply);
        return ReplyUtils.mapToReplyDTO(savedReply);
    }

    @Transactional
    public ReplyRepresentation editReply(
            long id,
            TPUserPrincipal principalRequestingToEditResource,
            EditReplyRequest replyRequest
    ) {
        Reply replyToEdit = replyRepository.findById(id)
                .orElseThrow(() -> new ReplyNotFoundException(id));
        TPUser replyAuthor = replyToEdit.getAuthor();
        if (replyAuthor.getId().equals(principalRequestingToEditResource.getId())) {
            replyToEdit.setBody(replyRequest.getNewReplyBody());
            replyToEdit.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            return ReplyUtils.mapToReplyDTO(replyToEdit);
        } else {
            throw new OperationNotAllowedException("You do not have permission to perform actions on this resource");
        }
    }

    public DeleteReplyResponse deleteReply(
            TPUserPrincipal principalRequestingToDeleteResource,
            long idOfTheResourceToBeDeleted
    ) {
        Reply replyToDelete = replyRepository.findById(idOfTheResourceToBeDeleted)
                .orElseThrow(() -> new ReplyNotFoundException(idOfTheResourceToBeDeleted));
        TPUser replyAuthor = replyToDelete.getAuthor();
        if (replyAuthor.getId().equals(principalRequestingToDeleteResource.getId()) || principalRequestingToDeleteResource.getRole() == Role.ADMIN) {
            DeleteReplyResponse deleteReplyResponse = DeleteReplyResponse.builder()
                    .message("Reply successfully removed").build();
            deleteReplyResponse.add(linkTo(methodOn(UsersController.class)
                    .getUser(replyToDelete.getAuthor().getId()))
                    .withRel("author"));
            deleteReplyResponse.add(linkTo(methodOn(RepliesController.class)
                    .getRepliesByParentCommentId(replyToDelete.getParentComment().getId(), 0, 10, "id", "Asc"))
                    .withRel("collection"));
            replyRepository.deleteById(idOfTheResourceToBeDeleted);
            return deleteReplyResponse;
        } else {
            throw new OperationNotAllowedException("You do not have permission to perform actions on this resource");
        }
    }
}

