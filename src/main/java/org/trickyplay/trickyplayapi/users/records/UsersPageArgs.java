package org.trickyplay.trickyplayapi.users.records;

import org.springframework.data.domain.Sort;

public record UsersPageArgs(int pageNumber, int pageSize, String sortBy,
                            Sort.Direction orderDirection) {
}
