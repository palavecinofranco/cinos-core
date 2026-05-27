package org.cinos.core.search.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.core.search.dto.SearchResultDTO;
import org.cinos.core.search.service.ISearchService;
import org.cinos.core.users.entity.AccountEntity;
import org.cinos.core.users.service.impl.AccountService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {

    private final AccountService accountService;

    @Override
    public List<SearchResultDTO> search(String query) {
        List<SearchResultDTO> results = new ArrayList<>();

        // Buscar usuarios
        List<AccountEntity> users = accountService.findByUsernameContainingIgnoreCase(query);
        users.forEach(user -> results.add(new SearchResultDTO(user.getId(), user.getUser().getUsername(), user.getAvatarImg(), "user")));

        return results;
    }

    @Override
    public List<SearchResultDTO> searchUsers(String query) {
        List<SearchResultDTO> results = new ArrayList<>();

        // Buscar solo usuarios
        List<AccountEntity> users = accountService.findByUsernameContainingIgnoreCase(query);
        users.forEach(user -> results.add(new SearchResultDTO(user.getId(), user.getUser().getUsername(), user.getAvatarImg(), "user")));

        return results;
    }
}
