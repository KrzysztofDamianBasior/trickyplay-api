package org.trickyplay.trickyplayapi.users.controllers;

import io.micrometer.core.annotation.Timed;

import jakarta.servlet.http.HttpServletResponse;
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
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.records.UsersPageArgs;
import org.trickyplay.trickyplayapi.users.services.PDFGeneratorService;
import org.trickyplay.trickyplayapi.users.services.UsersService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Validated
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;
    private final CommentsService commentsService;
    private final RepliesService repliesService;
    private final PDFGeneratorService pdfGeneratorService;

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
        UsersPageArgs usersPageArgs = new UsersPageArgs(pageNumber, pageSize, sortBy, sortDirection);
        return usersService.getUsers(usersPageArgs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public TPUserRepresentation getUser(@PathVariable @Min(0) long id) {
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

    @GetMapping("/{id}/activity-summary")
    @Timed("generate-pdf-timer")
    public void generatePDF(@PathVariable @Min(0) long id, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=activity-summary_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        this.pdfGeneratorService.export(id, response);
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('admin:update')")
    public TPUserRepresentation ban(@PathVariable @Min(0) long id
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
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

        return usersService.banUser(id);
    }

    @PatchMapping("/{id}/unban")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('admin:update')")
    public TPUserRepresentation unban(@PathVariable @Min(0) long id
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        long principalId = ((TPUserPrincipal) principal).getId();

        return usersService.unbanUser(id);
    }

    @PatchMapping("/{id}/grant-admin-permissions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('admin:update')")
    public TPUserRepresentation grantAdminPermissions(@PathVariable @Min(0) long id
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        long principalId = ((TPUserPrincipal) principal).getId();

        return usersService.grantAdminPermissions(id);
    }
}

