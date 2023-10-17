package org.trickyplay.trickyplayapi.replies.records;

import org.springframework.data.domain.Sort;

public record RepliesPageArgs(int pageNumber, int pageSize, String sortBy,
                              Sort.Direction orderDirection) {
}
