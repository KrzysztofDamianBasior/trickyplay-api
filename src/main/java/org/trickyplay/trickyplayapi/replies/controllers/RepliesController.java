package org.trickyplay.trickyplayapi.replies.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.trickyplay.trickyplayapi.replies.dtos.*;
import org.trickyplay.trickyplayapi.replies.services.RepliesService;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;

import java.net.URI;

@Validated
@RestController
@RequestMapping("replies")
@RequiredArgsConstructor
public class RepliesController {
    private final RepliesService repliesService;

    @GetMapping("/feed")
    @PreAuthorize("permitAll()")
    public GetRepliesResponse getRepliesByParentCommentId(
            @RequestParam(value = "parentCommentId", required = true) @Min(0) long parentCommentId,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) @Min(0) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) @Min(1) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) @Pattern(regexp = "^(id|createdAt|updatedAt)$", message = "invalid sort argument") String sortBy,
            @RequestParam(value = "order", defaultValue = "Asc", required = false) @Pattern(regexp = "^(Asc|Dsc)$", message = "invalid order direction") String orderDirection // Asc -sort descending, Dsc -sort ascending,
    ) {
//        int pageNo = pageNumber >= 0 ? pageNumber : 0;
//        Set<String> orderValues = Set.of("Asc", "Dsc");
//        Sort.Direction sortDirection = orderValues.contains(orderDirection) ? Sort.Direction.fromString(orderDirection) : Sort.Direction.ASC;
        Sort.Direction sortDirection = Sort.Direction.fromString(orderDirection);
        return repliesService.getRepliesByParentCommentId(parentCommentId, pageNumber, pageSize, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ReplyRepresentation getSingleReply(@PathVariable @Min(0) long id) {
        return repliesService.getSingleReply(id);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('user:read') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReplyRepresentation> addReply(@Valid @RequestBody AddReplyRequest addReplyRequest,
                                                        @AuthenticationPrincipal TPUserPrincipal user
    ) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        if (principal instanceof TPUserPrincipal) {
//            String username = ((TPUserPrincipal) principal).getUsername();
//            long principalId = ((TPUserPrincipal) principal).getId();
//            Role principalRole = ((TPUserPrincipal) principal).getRole();
//        } else {
//            String username = principal.toString();
//        }

        ReplyRepresentation replyDTO = repliesService.addReply(user, addReplyRequest);
        URI replyURI = URI.create("/replies/" + replyDTO.getId());
        return ResponseEntity.created(replyURI).body(replyDTO);
    }

    @PatchMapping()
    @PreAuthorize("hasAuthority('user:update') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ReplyRepresentation editReply(@Valid @RequestBody EditReplyRequest editReplyRequest,
                                         @AuthenticationPrincipal TPUserPrincipal user
    ) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        if (principal instanceof TPUserPrincipal) {
//            String username = ((TPUserPrincipal) principal).getUsername();
//            long principalId = ((TPUserPrincipal) principal).getId();
//            Role principalRole = ((TPUserPrincipal) principal).getRole();
//        } else {
//            String username = principal.toString();
//        }

        return repliesService.editReply(user, editReplyRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public DeleteReplyResponse deleteReply(@PathVariable @Min(0) long id,
                                           @AuthenticationPrincipal TPUserPrincipal user
    ) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        if (principal instanceof TPUserPrincipal) {
//            String username = ((TPUserPrincipal) principal).getUsername();
//            long principalId = ((TPUserPrincipal) principal).getId();
//            Role principalRole = ((TPUserPrincipal) principal).getRole();
//        } else {
//            String username = principal.toString();
//        }

        return repliesService.deleteReply(user, id);
    }

//    Exception handling has been moved to GlobalExceptionHandler
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
//        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
//    }
}