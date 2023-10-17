package org.trickyplay.trickyplayapi.replies.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.general.exceptions.ReplyNotFoundException;
import org.trickyplay.trickyplayapi.replies.dtos.*;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepliesService {
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final TPUserRepository tPUserRepository;

    public GetRepliesResponse getRepliesByParentCommentId(long parentCommentId, int pageNumber, int pageSize, String sortBy, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortBy);
        Page<Reply> replyPage = replyRepository.findAllByParentCommentId(parentCommentId, pageable);
        List<ReplyRepresentation> replies = ReplyUtils.mapToReplyDTOs(replyPage.getContent());

        return GetRepliesResponse.builder()
                .replies(replies)
                .pageSize(replyPage.getSize())
                .totalElements(replyPage.getTotalElements())
                .totalPages(replyPage.getTotalPages())
                .pageSize(replyPage.getSize())
                .pageNumber(replyPage.getNumber())
                .isLast(replyPage.isLast())
                .build();
    }

    public GetRepliesResponse getRepliesByAuthorId(long authorId, int pageNumber, int pageSize, String sortBy, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortBy);
        Page<Reply> replyPage = replyRepository.findAllByAuthorId(authorId, pageable);

        List<ReplyRepresentation> replies = ReplyUtils.mapToReplyDTOs(replyPage.getContent());

        return GetRepliesResponse.builder()
                .replies(replies)
                .pageSize(replyPage.getSize())
                .totalElements(replyPage.getTotalElements())
                .totalPages(replyPage.getTotalPages())
                .pageSize(replyPage.getSize())
                .pageNumber(replyPage.getNumber())
                .isLast(replyPage.isLast())
                .build();
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
            TPUserPrincipal principalRequestingToEditResource,
            EditReplyRequest replyRequest
    ) {
        Reply replyToEdit = replyRepository.findById(replyRequest.getReplyId())
                .orElseThrow(() -> new ReplyNotFoundException(replyRequest.getReplyId()));
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
            replyRepository.deleteById(idOfTheResourceToBeDeleted);
            return DeleteReplyResponse.builder()
                    .message("Reply successfully removed").build();
        } else {
            throw new OperationNotAllowedException("You do not have permission to perform actions on this resource");
        }
    }
}

