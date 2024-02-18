package searchengine.services;

import searchengine.dto.index.IndexResponse;
import searchengine.model.Site;

public interface IndexService {
    IndexResponse startIndexing();

    IndexResponse stopIndexing();

    IndexResponse indexPage(String indexPage);
}
