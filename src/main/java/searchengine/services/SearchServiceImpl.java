package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;

@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService{

    @Autowired
    SiteService siteService;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {
        SearchResponse response = new SearchResponse();




        if (query.isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }

        response.setResult(true);


        return response;
    }
}
