package searchengine.services;

import searchengine.dto.index.IndexResponseDto;

/**
 * This service start and stop indexing sites
 *
 * @author Ivan_Okhlopkov
 */
public interface IndexService {
    IndexResponseDto startIndexing();

    IndexResponseDto stopIndexing();

    IndexResponseDto indexPage(String indexPage);
}
