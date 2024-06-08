package searchengine.services;

import searchengine.dto.statistics.StatisticsResponseDto;

/**
 * This service get statistics from indexing sites
 *
 * @author Ivan_Okhlopkov
 */
public interface StatisticsService {
    StatisticsResponseDto getStatistics();
}
