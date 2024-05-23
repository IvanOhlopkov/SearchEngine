package searchengine.services;

import searchengine.dto.index.IndexResponse;

/**
 * This service start and stop indexing sites
 *
 * @author Ivan_Okhlopkov
 */
public interface IndexService {
    IndexResponse startIndexing();

    IndexResponse stopIndexing();

    IndexResponse indexPage(String indexPage);
}
