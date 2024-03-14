package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.*;


@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {

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

    public SortedSet<Map.Entry<String, Integer>> getLemmaFromQuery(String query, String site) {
        LemmaFinder lemmaFinder = new LemmaFinder();
        Map<String, Integer> wordsQuery = lemmaFinder.getLemma(query);
        SortedSet<Map.Entry<String, Integer>> newQueryMap = new TreeSet<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : wordsQuery.entrySet()) {
            String word = entry.getKey();

            Lemma lemma = siteService.getLemmaRepository().findLemma(word, siteService.getSite(site));

            int frequency = lemma.getFrequency();
            int totalPages = siteService.getPageRepository().getPageCount(siteService.getSite(site));

            if ((double) frequency / totalPages > 0.9) {
                wordsQuery.remove(word);
                continue;
                //если список пустой останется надо что-то придумать
            }

            wordsQuery.replace(word, entry.getValue(), frequency);

        }
        newQueryMap.addAll(wordsQuery.entrySet());

        List<Index> indexList = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        builder.append("page_id");

        for (Map.Entry<String, Integer> entry : newQueryMap) {
            String word = entry.getKey();
            Lemma lemma = siteService.getLemmaRepository().findLemma(word, siteService.getSite(site));

            indexList.addAll(siteService.getIndexRepository().getIndex(lemma, String.valueOf(builder)));

            if (builder.toString().contains("page_id")) {
                builder.delete(0, builder.length());
            }

            for (Index index : indexList) {
                builder.append(index.getPage_id().getId());
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            System.out.println(builder);
        }

        return newQueryMap;
    }


}
