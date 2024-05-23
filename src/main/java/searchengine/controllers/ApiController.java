package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexService indexService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexService indexService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexService = indexService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        return ResponseEntity.ok(indexService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(indexService.indexPage(url));
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query,
                                                 @RequestParam(required = false) String site,
                                                 @RequestParam(required = false, defaultValue = "0") int offset,
                                                 @RequestParam(required = false, defaultValue = "20") int limit){
        return searchService.search(query, site, offset, limit);
    }
}
