package org.trickyplay.trickyplayapi.users.controllers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.trickyplay.trickyplayapi.comments.dtos.GetCommentsResponse;
import org.trickyplay.trickyplayapi.comments.records.CommentsPageArgs;
import org.trickyplay.trickyplayapi.comments.services.CommentsService;
import org.trickyplay.trickyplayapi.replies.dtos.GetRepliesResponse;
import org.trickyplay.trickyplayapi.replies.records.RepliesPageArgs;
import org.trickyplay.trickyplayapi.replies.services.RepliesService;
import org.trickyplay.trickyplayapi.users.dtos.GetUsersResponse;
import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;
import org.trickyplay.trickyplayapi.users.services.UsersService;

@Validated
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;
    private final CommentsService commentsService;
    private final RepliesService repliesService;

    @GetMapping("/feed")
    @PreAuthorize("permitAll()")
    public GetUsersResponse getUsers(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) @Min(0) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) @Min(1) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) @Pattern(regexp = "^(id|createdAt|updatedAt)$", message = "invalid sort argument") String sortBy,
            @RequestParam(value = "orderDirection", defaultValue = "Asc", required = false) @Pattern(regexp = "^(Asc|Dsc)$", message = "invalid order direction") String orderDirection // Asc -sort descending, Dsc -sort ascending,
    ) {
//        int pageNo = pageNumber >= 0 ? pageNumber : 0;
//        Set<String> orderValues = Set.of("Asc", "Dsc");
//        Sort.Direction sortDirection = orderValues.contains(orderDirection) ? Sort.Direction.fromString(orderDirection) : Sort.Direction.ASC;
        Sort.Direction sortDirection = Sort.Direction.fromString(orderDirection);
        return usersService.getUsers(pageNumber, pageSize, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public TPUserPublicInfoDTO getUser(@PathVariable @Min(0) long id) {
        return usersService.getUser(id);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("permitAll()")
    public GetCommentsResponse getUserComments(
            @PathVariable @Min(0) long id,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) @Min(0) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) @Min(1) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) @Pattern(regexp = "^(id|createdAt|updatedAt)$", message = "invalid sort argument") String sortBy,
            @RequestParam(value = "orderDirection", defaultValue = "Asc", required = false) @Pattern(regexp = "^(Asc|Dsc)$", message = "invalid order direction") String orderDirection // Asc -sort descending, Dsc -sort ascending,
    ) {
//        int pageNo = pageNumber >= 0 ? pageNumber : 0;
//        Set<String> orderValues = Set.of("Asc", "Dsc");
//        Sort.Direction sortDirection = orderValues.contains(orderDirection) ? Sort.Direction.fromString(orderDirection) : Sort.Direction.ASC;
        Sort.Direction sortDirection = Sort.Direction.fromString(orderDirection);
        CommentsPageArgs commentsPageArgs = new CommentsPageArgs(pageNumber, pageSize, sortBy, sortDirection);
        return commentsService.getCommentsByAuthorId(id, commentsPageArgs);
    }

    @GetMapping("/{id}/replies")
    @PreAuthorize("permitAll()")
    public GetRepliesResponse getUserReplies(
            @PathVariable @Min(0) long id,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) @Min(0) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) @Min(1) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) @Pattern(regexp = "^(id|createdAt|updatedAt)$", message = "invalid sort argument") String sortBy,
            @RequestParam(value = "orderDirection", defaultValue = "Asc", required = false) @Pattern(regexp = "^(Asc|Dsc)$", message = "invalid order direction") String orderDirection // Asc -sort descending, Dsc -sort ascending,
    ) {
//        int pageNo = pageNumber >= 0 ? pageNumber : 0;
//        Set<String> orderValues = Set.of("Asc", "Dsc");
//        Sort.Direction sortDirection = orderValues.contains(orderDirection) ? Sort.Direction.fromString(orderDirection) : Sort.Direction.ASC;
        Sort.Direction sortDirection = Sort.Direction.fromString(orderDirection);
        RepliesPageArgs repliesPageArgs = new RepliesPageArgs(pageNumber, pageSize, sortBy, sortDirection);
        return repliesService.getRepliesByAuthorId(id, repliesPageArgs);
    }
}

