package searchengine.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.LemmaFinder;
import searchengine.services.SearchService;

import java.util.*;

import static searchengine.util.Variables.CHAR_LENGTH;

@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {

    @Getter
    @Autowired
    SiteRepository siteRepository;

    @Getter
    @Autowired
    LemmaRepository lemmaRepository;

    @Getter
    @Autowired
    IndexRepository indexRepository;

    @Getter
    @Autowired
    PageRepository pageRepository;

    List<Index> indexList;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {
        SearchResponse response = new SearchResponse();
        List<SearchData> dataList;

        List<Lemma> lemmaList = findLemmasFromQuery(query, site);

        if (lemmaList.isEmpty()) {
            response.setResult(false);
            response.setError("По заданному запросу ничего не найдено");
            return response;
        } else {
            dataList = getSearchData(lemmaList, query);
        }

        if (query.isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }

        response.setResult(true);
        response.setCount(dataList.size());
        response.setData(dataList);

        return response;
    }

    private List<SearchData> getSearchData(List<Lemma> lemmaList, String query) {

        HashMap<Page, Double> sortedRelativePages = getRelativePages(lemmaList);

        List<SearchData> dataList = new ArrayList<>();

        if (sortedRelativePages.isEmpty()) {
            return dataList;
        }

        for (Map.Entry<Page, Double> entry : sortedRelativePages.entrySet()) {
            SearchData searchData = new SearchData();
            Page page = entry.getKey();

            searchData.setSite(page.getSiteId().getUrl());
            searchData.setSiteName(page.getSiteId().getName());
            searchData.setUri(entry.getKey().getPath());

            searchData.setSnippet(findSnippet(entry.getKey(), query));

            Document document = Jsoup.parse(entry.getKey().getContent());
            searchData.setTitle(document.title());
            searchData.setRelevance(entry.getValue());

            dataList.add(searchData);
        }

        return dataList;
    }

    private HashMap<Page, Double> getRelativePages(List<Lemma> lemmaList) {

        HashMap<Page, Double> relevantMap = getAbsoluteRatePage(lemmaList);

        Double absoluteMaxRelevant = absoluteMaxRelevant(relevantMap);

        HashMap<Page, Double> relativeMap = new HashMap<>();

        for (Map.Entry<Page, Double> entry : relevantMap.entrySet()) {
            double relevant = entry.getValue();
            relativeMap.put(entry.getKey(), relevant / absoluteMaxRelevant);
        }

        HashMap<Page, Double> sortedRelativeMap = new HashMap<>();

        relativeMap.entrySet().stream()
            .sorted(Map.Entry.<Page, Double>comparingByValue().reversed())
            .forEach(entry -> sortedRelativeMap.put(entry.getKey(), entry.getValue()));

        return sortedRelativeMap;
    }

    private HashMap<Page, Double> getAbsoluteRatePage(List<Lemma> lemmaList) {
        List<Index> indexList = searchIndex(lemmaList);

        HashMap<Page, Double> relevantMap = new HashMap<>();

        for (Index index : indexList) {
            double rank = index.getRate();
            if (relevantMap.containsKey(index.getPageId())) {
                relevantMap.put(index.getPageId(), relevantMap.get(index.getPageId()) + rank);
            } else {
                relevantMap.put(index.getPageId(), rank);
            }
        }

        this.indexList = indexList;

        return relevantMap;
    }

    private String findSnippet(Page page, String query) {

        String pageContent = page.getContent();
        Document document = Jsoup.parse(pageContent);
        String ownText = document.body().text();

        String[] words = query.split(" ");
        String findText = "";

        for (String word : words) {
            int index = ownText.indexOf(word);

            if (index == -1) {
                findText = ownText.substring(0, CHAR_LENGTH);
            } else {
                int left = ownText.substring(0, index).length();
                int right = ownText.substring(index).length();

                String textBefore = ownText.substring(index - Math.min(left, CHAR_LENGTH), index);
                String textAfter = ownText.substring(index + word.length(),
                    index + word.length() + Math.min(right, CHAR_LENGTH));
                findText = textBefore + "<b>" + word + "</b>" + textAfter;
            }
        }

        return findText;
    }


    private <K, V extends Comparable<V>> V absoluteMaxRelevant(Map<K, V> map) {
        Map.Entry<K, V> maxEntry = Collections.max(map.entrySet(),
            Map.Entry.comparingByValue());
        return maxEntry.getValue();
    }

    public List<Lemma> findLemmasFromQuery(String query, String site) {
        LemmaFinder lemmaFinder = new LemmaFinder();
        Map<String, Integer> wordsQuery = lemmaFinder.getLemma(query);
        List<Lemma> lemmaList = new ArrayList<>();

        List<Site> siteList = new ArrayList<>();

        if (site != null) {
            siteList.add(siteRepository.findByUrl(site));
        } else {
            siteList = getSiteRepository().getAllSites();
        }

        for (Site siteItem : siteList) {
            lemmaList.addAll(getLemmaMap(wordsQuery, siteItem));
        }

        return lemmaList;
    }

    private List<Lemma> getLemmaMap(Map<String, Integer> wordsQuery, Site site) {
        List<Lemma> lemmaList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : wordsQuery.entrySet()) {
            String word = entry.getKey();

            Lemma lemma = getLemmaRepository().findLemma(word, site);

            if (lemma == null) {
                continue;
            }

            lemmaList.add(lemma);
        }
        return lemmaList;
    }

    private List<Index> searchIndex(List<Lemma> lemmaList) {

        List<Index> indexList = new ArrayList<>();
        String pages = "";

        for (Lemma lemma : lemmaList) {

            List<Index> indexListWithOneLemma = new ArrayList<>();

            if (getIndexRepository().getAllIndex(lemma).isEmpty()) {
                continue;
            }

            if (indexList.isEmpty()) {
                indexListWithOneLemma.addAll(getIndexRepository().getAllIndex(lemma));
            } else {
                indexListWithOneLemma.addAll(getIndexRepository().getIndexFilterPage(lemma, pages));
            }

            for (Index index : indexListWithOneLemma) {
                pages = pages + index.getPageId().getId() + ",";
            }

            pages = pages.replaceAll(",$", "");
            indexList.addAll(indexListWithOneLemma);

        }

        return indexList;
    }


}
