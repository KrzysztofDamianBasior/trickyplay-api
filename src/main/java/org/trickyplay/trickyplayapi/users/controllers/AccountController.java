package org.trickyplay.trickyplayapi.users.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.trickyplay.trickyplayapi.users.dtos.DeleteAccountResponse;
import org.trickyplay.trickyplayapi.users.dtos.EditAccountRequest;
import org.trickyplay.trickyplayapi.users.dtos.TPUserRepresentation;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.services.AccountService;
import org.trickyplay.trickyplayapi.users.services.PDFGeneratorService;

@Validated // validate parameters that are passed into a method
@RestController
@RequiredArgsConstructor
@RequestMapping("account")
public class AccountController {
    private final AccountService accountService;
    private final PDFGeneratorService pdfGeneratorService;

    @GetMapping("/activity-summary")
    @PreAuthorize("isAuthenticated()")
    public void generatePDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=activity-summary_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        long principalId = ((TPUserPrincipal) principal).getId();

        this.pdfGeneratorService.export(principalId, response);
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public TPUserRepresentation getMyAccount(
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        long principalId = ((TPUserPrincipal) principal).getId();

        return accountService.getAccount(principalId);
    }

    // ref: https://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api
    // ref: https://www.rfc-editor.org/rfc/rfc7231
    // If a DELETE method is successfully applied, the origin server SHOULD send a 202 (Accepted) status code if the action will likely succeed but has not yet been enacted (the request has been accepted for processing, but the processing has not been completed), a 204 (No Content) status code if the action has been enacted and no further information is to be supplied, or a 200 (OK) status code if the action has been enacted and the response message includes a representation describing the status.
    //
    // ref: https://stackoverflow.com/questions/25970523/restful-what-should-a-delete-response-body-contain
    // 204 No Content is a popular response for DELETE and occasionally PUT as well. However, if you are implementing HATEOAS, returning a 200 OK with links to follow may be more ideal. This is because a HATEOAS REST API provides context to the client. Instead of returning 204 (No Content), the API should be helpful and suggest places to go.
    @DeleteMapping("/")
    @PreAuthorize("isAuthenticated()")
    public DeleteAccountResponse deleteAccount(
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        long principalId = ((TPUserPrincipal) principal).getId();

        return accountService.deleteAccount(principalId);
    }

    @PatchMapping()
    @PreAuthorize("isAuthenticated()")
    public TPUserRepresentation editAccount(@Valid @RequestBody EditAccountRequest editAccountRequest
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        long principalId = ((TPUserPrincipal) principal).getId();

        return accountService.editAccount(principalId, editAccountRequest);
    }

    @PatchMapping("/{id}/ban-account")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('admin:update')")
    public TPUserRepresentation banAccount(@PathVariable @Min(0) long id
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

        return accountService.banAccount(id);
    }

    @PatchMapping("/{id}/unban-account")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('admin:update')")
    public TPUserRepresentation unbanAccount(@PathVariable @Min(0) long id
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        long principalId = ((TPUserPrincipal) principal).getId();

        return accountService.unbanAccount(id);
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

        return accountService.grantAdminPermissions(id);
    }
}
