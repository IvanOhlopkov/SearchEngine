package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.index.IndexResponseDto;
import searchengine.dto.search.SearchResponseDto;
import searchengine.dto.statistics.StatisticsResponseDto;
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

    /**
     * Эндпоинт отражает статистику индексируемых сайтов
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponseDto> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    /**
     * Запускает индексирование страницы указанных в
     * файле конфигурации
     */
    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponseDto> startIndexing() {
        return ResponseEntity.ok(indexService.startIndexing());
    }

    /**
     * Останавливает индексирование
     */
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponseDto> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }


    /**
     * Метод запускает индексироание для отдельной страницы
     */
    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponseDto> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(indexService.indexPage(url));
    }

    /**
     * Метод выполняет поиск по указанным значениям и сайтам
     */
    @GetMapping("/search")
    public SearchResponseDto search(@RequestParam String query,
                                    @RequestParam(required = false) String site,
                                    @RequestParam(required = false, defaultValue = "0") int offset,
                                    @RequestParam(required = false, defaultValue = "20") int limit){
        return searchService.search(query, site, offset, limit);
    }
}
