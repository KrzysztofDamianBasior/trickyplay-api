/*
 * The code below was written based on
 * https://github.com/LibrePDF/OpenPDF/blob/master/pdf-toolbox/src/test/java/com/lowagie/examples/objects/tables/pdfptable/Tables.java
 * --> Copyright 2001-2005 by G. Martinelli and Bruno Lowagie <--
 * Which is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDFtutorial/
 */
package org.trickyplay.trickyplayapi.users.services;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

// ref: https://stackoverflow.com/questions/1080932/pdfptable-vs-table-vs-simpletable
// com.lowagie.text.pdf.PdfPTable vs com.lowagie.text.Table
// com.lowagie.text.Table is the original table class; it dates from the early iText days. It uses class com.lowagie.text.pdf.PdfTable internally to render a table to PDF. There’s also the newer SimpleTable class, which tries to form a link between PdfPTable and Table. It’s able to translate itself to a PdfPTable if you add it to a document that writes PDF or to a Table if you’re producing HTML or RTF. The major disadvantage of the Table class is that it’s no longer supported. Different people have fixed most of the known issues, but today not a single person understands if and how all the Table-methods work. If you decide to use this class, you’re more or less on your own, and you’ll encounter lots of quirky layout issues based on historical design decisions.
// PdfPTable should be your first choice; but depending on the requirements defined for your project, there can be good reasons to opt for Table or SimpleTable.

@Slf4j
@Service
@RequiredArgsConstructor
public class PDFGeneratorService {
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final TPUserRepository tPUserRepository;

    public void export(long userId, HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(
                0,
                200,
                Sort.Direction.ASC,
                "id"
        );

        TPUser user = tPUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Page<Comment> commentPage = commentRepository.findAllByAuthorId(userId, pageable);
        Page<Reply> replyPage = replyRepository.findAllByAuthorId(userId, pageable);

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA);
        fontSubtitle.setSize(12);
        Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fontTableCell = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font fontWarning = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.RED);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        float width = document.getPageSize().getWidth();
        float height = document.getPageSize().getHeight();
        document.open();

        Paragraph paragraph = new Paragraph("TrickyPlay", fontTitle);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);

        Paragraph paragraph2 = new Paragraph("Activity summary", fontSubtitle);
        paragraph2.setAlignment(Paragraph.ALIGN_CENTER);
        paragraph2.setSpacingAfter(15);
        paragraph2.setSpacingBefore(5);
        document.add(paragraph2);

        PdfPTable table = null;

        Paragraph paragraph3 = new Paragraph("User details", fontSubtitle);
        paragraph3.setAlignment(Paragraph.ALIGN_LEFT);
        paragraph3.setSpacingAfter(5);
        paragraph3.setSpacingBefore(10);
        document.add(paragraph3);
        float[] userDetailsColumnDefinitionSize = {20F, 30F, 20F, 20F, 10F};
        table = new PdfPTable(userDetailsColumnDefinitionSize);
        table.getDefaultCell().setBorder(1);
        table.setHorizontalAlignment(0);
        table.setTotalWidth(width - 72);
        table.setLockedWidth(true);
        table.addCell(new PdfPCell(new Phrase("id", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("username", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("created at", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("last updated at", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("role", fontTableHeader)));
        table.addCell(new Phrase(user.getId().toString(), fontTableCell));
        table.addCell(new Phrase(user.getName(), fontTableCell));
        table.addCell(new Phrase(user.getCreatedAt().toString(), fontTableCell));
        table.addCell(new Phrase(user.getUpdatedAt().toString(), fontTableCell));
        table.addCell(new Phrase(user.getRole().name(), fontTableCell));
        table.setSpacingAfter(20);
        document.add(table);

        if (!commentPage.isLast()) {
            Paragraph commentsWarningParagraph = new Paragraph("The user has posted more than 50 comments. To reduce the size of the PDF document, we have included only the first 50, if you want to receive the document with all comments, please contact the administration", fontWarning);
            commentsWarningParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            commentsWarningParagraph.setSpacingAfter(5);
            commentsWarningParagraph.setSpacingBefore(5);
            document.add(commentsWarningParagraph);
        }

        Paragraph paragraph4 = new Paragraph("Created comments", fontSubtitle);
        paragraph4.setAlignment(Paragraph.ALIGN_LEFT);
        paragraph4.setSpacingAfter(5);
        paragraph4.setSpacingBefore(10);
        document.add(paragraph4);
        float[] createdCommentsColumnDefinitionSize = {8F, 40F, 12F, 20F, 20F};
        table = new PdfPTable(createdCommentsColumnDefinitionSize);
        table.getDefaultCell().setBorder(1);
        table.setHorizontalAlignment(0);
        table.setTotalWidth(width - 72);
        table.setLockedWidth(true);
        table.addCell(new PdfPCell(new Phrase("id", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("body", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("gameName", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("created at", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("last updated at", fontTableHeader)));

        for (Comment comment : commentPage.getContent()) {
            table.addCell(new Phrase(comment.getId().toString(), fontTableCell));
            table.addCell(new Phrase(comment.getBody(), fontTableCell));
            table.addCell(new Phrase(comment.getGameName(), fontTableCell));
            table.addCell(new Phrase(comment.getCreatedAt().toString(), fontTableCell));
            table.addCell(new Phrase(comment.getUpdatedAt().toString(), fontTableCell));
        }
        table.setSpacingAfter(20);
        document.add(table);

        if (!replyPage.isLast()) {
            Paragraph repliesWarningParagraph = new Paragraph("The user has posted more than 50 replies. To reduce the size of the PDF document, we have included only the first 50, if you want to receive the document with all replies, please contact the administration", fontWarning);
            repliesWarningParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            repliesWarningParagraph.setSpacingAfter(5);
            repliesWarningParagraph.setSpacingBefore(5);
            document.add(repliesWarningParagraph);
        }

        Paragraph paragraph5 = new Paragraph("Created replies", fontSubtitle);
        paragraph5.setAlignment(Paragraph.ALIGN_LEFT);
        paragraph5.setSpacingAfter(5);
        paragraph5.setSpacingBefore(10);
        document.add(paragraph5);
        float[] createdRepliesColumnDefinitionSize = {10F, 40F, 10F, 20F, 20F};
        table = new PdfPTable(createdRepliesColumnDefinitionSize);
        table.getDefaultCell().setBorder(1);
        table.setHorizontalAlignment(0);
        table.setTotalWidth(width - 72);
        table.setLockedWidth(true);
        table.addCell(new PdfPCell(new Phrase("id", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("body", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("parent comment id", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("created at", fontTableHeader)));
        table.addCell(new PdfPCell(new Phrase("last updated at", fontTableHeader)));
        for (Reply reply : replyPage.getContent()) {
            table.addCell(new Phrase(reply.getId().toString(), fontTableCell));
            table.addCell(new Phrase(reply.getBody(), fontTableCell));
            table.addCell(new Phrase(reply.getParentComment().getId().toString(), fontTableCell));
            table.addCell(new Phrase(reply.getCreatedAt().toString(), fontTableCell));
            table.addCell(new Phrase(reply.getUpdatedAt().toString(), fontTableCell));
        }
        table.setSpacingAfter(20);
        document.add(table);

        document.close();
    }

    // Playground
    public static void main(String[] args) {
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA);
        fontSubtitle.setSize(12);
        Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fontTableCell = FontFactory.getFont(FontFactory.HELVETICA, 8);

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("tables.pdf"));
            float width = document.getPageSize().getWidth();
            float height = document.getPageSize().getHeight();
            document.open();

            Paragraph paragraph = new Paragraph("TrickyPlay", fontTitle);
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(paragraph);

            Paragraph paragraph2 = new Paragraph("Activity summary", fontSubtitle);
            paragraph2.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph2.setSpacingAfter(15);
            paragraph2.setSpacingBefore(5);
            document.add(paragraph2);

            PdfPTable table = null;

            Paragraph paragraph3 = new Paragraph("User details", fontSubtitle);
            paragraph3.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph3.setSpacingAfter(5);
            paragraph3.setSpacingBefore(10);
            document.add(paragraph3);
            float[] userDetailsColumnDefinitionSize = {20F, 30F, 20F, 20F, 10F};
            table = new PdfPTable(userDetailsColumnDefinitionSize);
            table.getDefaultCell().setBorder(1);
            table.setHorizontalAlignment(0);
            table.setTotalWidth(width - 72);
            table.setLockedWidth(true);
            table.addCell(new PdfPCell(new Phrase("id", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("username", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("created at", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("last updated at", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("role", fontTableHeader)));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("asdf", fontTableCell));
            table.addCell(new Phrase("10.10.10.10.10.10.10.10", fontTableCell));
            table.addCell(new Phrase("10.10.10.10.10.10.10.10", fontTableCell));
            table.addCell(new Phrase("User", fontTableCell));
            table.setSpacingAfter(20);
            document.add(table);

            Paragraph paragraph4 = new Paragraph("Created comments", fontSubtitle);
            paragraph4.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph4.setSpacingAfter(5);
            paragraph4.setSpacingBefore(10);
            document.add(paragraph4);
            float[] createdCommentsColumnDefinitionSize = {8F, 40F, 12F, 20F, 20F};
            table = new PdfPTable(createdCommentsColumnDefinitionSize);
            table.getDefaultCell().setBorder(1);
            table.setHorizontalAlignment(0);
            table.setTotalWidth(width - 72);
            table.setLockedWidth(true);
            table.addCell(new PdfPCell(new Phrase("id", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("body", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("gameName", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("created at", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("last updated at", fontTableHeader)));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("asdf", fontTableCell));
            table.addCell(new Phrase("Minesweeper", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("asdf", fontTableCell));
            table.addCell(new Phrase("Minesweeper", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.setSpacingAfter(20);
            document.add(table);

            Paragraph paragraph5 = new Paragraph("Created replies", fontSubtitle);
            paragraph5.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph5.setSpacingAfter(5);
            paragraph5.setSpacingBefore(10);
            document.add(paragraph5);
            float[] createdRepliesColumnDefinitionSize = {10F, 40F, 10F, 20F, 20F};
            table = new PdfPTable(createdRepliesColumnDefinitionSize);
            table.getDefaultCell().setBorder(1);
            table.setHorizontalAlignment(0);
            table.setTotalWidth(width - 72);
            table.setLockedWidth(true);
            table.addCell(new PdfPCell(new Phrase("id", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("body", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("parent comment id", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("created at", fontTableHeader)));
            table.addCell(new PdfPCell(new Phrase("last updated at", fontTableHeader)));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("asdf", fontTableCell));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("asdf", fontTableCell));
            table.addCell(new Phrase("1", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.addCell(new Phrase("2023-10-19T11:34:43Z", fontTableCell));
            table.setSpacingAfter(20);
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        document.close();
    }
}