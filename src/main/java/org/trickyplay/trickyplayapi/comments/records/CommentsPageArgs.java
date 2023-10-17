package org.trickyplay.trickyplayapi.comments.records;

import org.springframework.data.domain.Sort;

public record CommentsPageArgs(int pageNumber, int pageSize, String sortBy,
                               Sort.Direction orderDirection) {
}
