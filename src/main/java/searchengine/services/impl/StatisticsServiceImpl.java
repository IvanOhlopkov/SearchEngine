package searchengine.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.PresetSite;
import searchengine.config.PresetSitesList;
import searchengine.dto.statistics.DetailedStatisticsItemDto;
import searchengine.dto.statistics.StatisticsDataDto;
import searchengine.dto.statistics.StatisticsResponseDto;
import searchengine.dto.statistics.TotalStatisticsDto;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Getter
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final Random random = new Random();
    private final PresetSitesList sites;

    @Override
    public StatisticsResponseDto getStatistics() {
        TotalStatisticsDto total = new TotalStatisticsDto();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        List<DetailedStatisticsItemDto> detailed = new ArrayList<>();
        List<PresetSite> sitesList = sites.getSites();

        for(int i = 0; i < sitesList.size(); i++) {
            PresetSite presetSite = sitesList.get(i);
            DetailedStatisticsItemDto item = new DetailedStatisticsItemDto();
            item.setName(presetSite.getName());
            item.setUrl(presetSite.getUrl());
            Site site = siteRepository.findByUrl(presetSite.getUrl());
            if(site == null) {
                break;
            }
            int pages = getPageRepository().getPageCount(site);
            int lemmas = getLemmaRepository().getCountLemma(site);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(site.getStatus().name());
            item.setError(site.getLastError() == null ? " " : site.getLastError());
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponseDto response = new StatisticsResponseDto();
        StatisticsDataDto data = new StatisticsDataDto();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
