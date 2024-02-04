package org.trickyplay.trickyplayapi.comments.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.trickyplay.trickyplayapi.comments.dtos.*;
import org.trickyplay.trickyplayapi.comments.records.CommentsPageArgs;
import org.trickyplay.trickyplayapi.comments.services.CommentsService;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;

import java.net.URI;

@Validated // validate parameters that are passed into a method
@RestController
@RequestMapping("comments")
@RequiredArgsConstructor
public class CommentsController {
    private final CommentsService commentsService;

    /**
     * @param gameName       name of the game from which comments are fetched
     * @param pageNumber     number of the page returned
     * @param pageSize       number of entries in each page
     * @param sortBy         column to sort on
     * @param orderDirection sort order. Can be ASC or DESC
     * @return Page object with comments after filtering and sorting
     */
    @GetMapping("/feed")
    @PreAuthorize("permitAll()")
    public GetCommentsResponse getCommentsByGameName(
            @RequestParam(value = "gameName", required = true) @Pattern(regexp = "^(Snake|TicTacToe|Minesweeper)$", message = "invalid sort argument") String gameName,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) @Min(0) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) @Min(1) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) @Pattern(regexp = "^(id|createdAt|updatedAt)$", message = "invalid sort argument") String sortBy,
            @RequestParam(value = "orderDirection", defaultValue = "Asc", required = false) @Pattern(regexp = "^(Asc|Dsc)$", message = "invalid order direction") String orderDirection // Asc -sort descending, Dsc -sort ascending,
    ) {
//        int pageNo = pageNumber >= 0 ? pageNumber : 0;
//        Set<String> orderValues = Set.of("Asc", "Dsc");
//        Sort.Direction sortDirection = orderValues.contains(orderDirection) ? Sort.Direction.fromString(orderDirection) : Sort.Direction.ASC;

//        Spring hateoas provides a ready-made paging solution, but it will not be used because this API returns a more custom solution
//        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(data.size(), 1, data.size(), 1);
//        PagedModel<CommentRepresentation> pagedComments = new PagedModel<>(commentRepresentationCollection, metadata);

        Sort.Direction sortDirection = Sort.Direction.fromString(orderDirection);
        CommentsPageArgs commentsPageArgs = new CommentsPageArgs(pageNumber, pageSize, sortBy, sortDirection);
        return commentsService.getCommentsByGameName(gameName, commentsPageArgs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public CommentRepresentation getSingleComment(@PathVariable @Min(0) long id) {
        return commentsService.getSingleComment(id);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('user:read') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<CommentRepresentation> addComment(@Valid @RequestBody AddCommentRequest addCommentRequest
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        TPUserPrincipal principal = (TPUserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        CommentRepresentation commentRepresentation = commentsService.addComment(principal, addCommentRequest);
        URI commentURI = URI.create("/comments/" + commentRepresentation.getId());
        return ResponseEntity.created(commentURI).body(commentRepresentation);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public CommentRepresentation editComment(
            @PathVariable @Min(0) long id,
            @Valid @RequestBody EditCommentRequest editCommentRequest
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        TPUserPrincipal principal = (TPUserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return commentsService.editComment(principal, id, editCommentRequest);
    }

    // ref: https://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api
    // ref: https://www.rfc-editor.org/rfc/rfc7231
    // If a DELETE method is successfully applied, the origin server SHOULD send a 202 (Accepted) status code if the action will likely succeed but has not yet been enacted (the request has been accepted for processing, but the processing has not been completed), a 204 (No Content) status code if the action has been enacted and no further information is to be supplied, or a 200 (OK) status code if the action has been enacted and the response message includes a representation describing the status.
    //
    // ref: https://stackoverflow.com/questions/25970523/restful-what-should-a-delete-response-body-contain
    // 204 No Content is a popular response for DELETE and occasionally PUT as well. However, if you are implementing HATEOAS, returning a 200 OK with links to follow may be more ideal. This is because a HATEOAS REST API provides context to the client. Instead of returning 204 (No Content), the API should be helpful and suggest places to go.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public DeleteCommentResponse deleteComment(@PathVariable @Min(0) long id
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        TPUserPrincipal principal = (TPUserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return commentsService.deleteComment(principal, id);
    }

//    Exception handling has been moved to GlobalExceptionHandler
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
//        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
//    }
}
