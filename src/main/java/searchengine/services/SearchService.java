package searchengine.services;

import searchengine.dto.search.SearchResponseDto;

/**
 * This service search lemmas on sites/specified site
 *
 * @author Ivan_Okhlopkov
 */
public interface SearchService {
    SearchResponseDto search(String query, String site, int offset, int limit);
}
