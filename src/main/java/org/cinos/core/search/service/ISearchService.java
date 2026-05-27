package org.cinos.core.search.service;

import org.cinos.core.search.dto.SearchResultDTO;

import java.util.List;

public interface ISearchService {
    List<SearchResultDTO> search(String query);
    List<SearchResultDTO> searchUsers(String query);
}
