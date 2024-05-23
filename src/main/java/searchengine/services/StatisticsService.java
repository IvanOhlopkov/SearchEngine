package searchengine.services;

import searchengine.dto.statistics.StatisticsResponse;

/**
 * This service get statistics from indexing sites
 *
 * @author Ivan_Okhlopkov
 */
public interface StatisticsService {
    StatisticsResponse getStatistics();
}
