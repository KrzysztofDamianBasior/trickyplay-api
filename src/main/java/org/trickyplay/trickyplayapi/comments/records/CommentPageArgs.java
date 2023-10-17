package org.trickyplay.trickyplayapi.comments.records;

import org.springframework.data.domain.Sort;

public record CommentPageArgs(String gameName, int pageNumber, int pageSize, String sortBy,
                              Sort.Direction orderDirection) {
}
