package searchengine.services;

import searchengine.dto.search.SearchResponse;

/**
 * This service search lemmas on sites/specified site
 *
 * @author Ivan_Okhlopkov
 */
public interface SearchService {
    SearchResponse search(String query, String site, int offset, int limit);
}
