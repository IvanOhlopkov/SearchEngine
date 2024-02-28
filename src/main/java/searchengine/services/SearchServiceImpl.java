package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Lemma;

import java.util.Map;


@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService{

    @Autowired
    SiteService siteService;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {
        SearchResponse response = new SearchResponse();
        if (site.isEmpty()) {
            site = "site_id";
        }
        getLemmaFromQuery(query, site);

        if (query.isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }

        response.setResult(true);

        return response;
    }

    public void getLemmaFromQuery(String query, String site) {
        LemmaFinder lemmaFinder = new LemmaFinder();
        Map<String, Integer> wordsQuery = lemmaFinder.getLemma(query);

        for (Map.Entry<String, Integer> entry : wordsQuery.entrySet()) {
            String word = entry.getKey();

            Lemma lemma = siteService.getLemmaRepository().findLemma(word, siteService.getSite(site));

            int frequency = lemma.getFrequency();
            int totalPages = siteService.getPageRepository().getPageCount(siteService.getSite(site));

            if ((double) frequency / totalPages > 0.9) {
                wordsQuery.remove(word);
            }

        }

    }
}
