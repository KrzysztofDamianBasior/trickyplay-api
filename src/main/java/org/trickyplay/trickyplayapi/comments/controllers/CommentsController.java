package org.trickyplay.trickyplayapi.comments.controllers;

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

import org.trickyplay.trickyplayapi.comments.dtos.*;
import org.trickyplay.trickyplayapi.comments.services.CommentsService;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;

import java.net.URI;

@Validated // validate parameters that are passed into a method
@RestController
@RequestMapping("comments")
@RequiredArgsConstructor
public class CommentsController {
    private final CommentsService commentsService;

    @GetMapping("/feed")
    @PreAuthorize("permitAll()")
    public GetCommentsResponse getCommentsByGameName(
            @RequestParam(value = "gameName", required = true) String gameName,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) @Min(0) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) @Min(1) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) @Pattern(regexp = "^(id|createdAt|updatedAt)$", message = "invalid sort argument") String sortBy,
            @RequestParam(value = "orderDirection", defaultValue = "Asc", required = false) @Pattern(regexp = "^(Asc|Dsc)$", message = "invalid order direction") String orderDirection // Asc -sort descending, Dsc -sort ascending,
    ) {
//        int pageNo = pageNumber >= 0 ? pageNumber : 0;
//        Set<String> orderValues = Set.of("Asc", "Dsc");
//        Sort.Direction sortDirection = orderValues.contains(orderDirection) ? Sort.Direction.fromString(orderDirection) : Sort.Direction.ASC;
        Sort.Direction sortDirection = Sort.Direction.fromString(orderDirection);
        return commentsService.getCommentsByGameName(gameName, pageNumber, pageSize, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public CommentDTO getSingleComment(@PathVariable @Min(0) long id) {
        return commentsService.getSingleComment(id);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('user:read') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<CommentDTO> addComment(@Valid @RequestBody AddCommentRequest addCommentRequest,
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
        CommentDTO commentDTO = commentsService.addComment(user, addCommentRequest);
        URI commentURI = URI.create("/comments/" + commentDTO.getId());
        return ResponseEntity.created(commentURI).body(commentDTO);
    }


    @PatchMapping()
    @PreAuthorize("hasAuthority('user:update') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public CommentDTO editComment(@Valid @RequestBody EditCommentRequest editCommentRequest,
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

        return commentsService.editComment(user, editCommentRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public DeleteCommentResponse deleteComment(@PathVariable @Min(0) long id,
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

        return commentsService.deleteComment(user, id);
    }

//    Exception handling has been moved to GlobalExceptionHandler
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
//        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
//    }
}
