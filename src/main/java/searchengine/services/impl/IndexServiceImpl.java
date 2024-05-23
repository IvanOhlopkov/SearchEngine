package searchengine.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.PresetSite;
import searchengine.config.PresetSitesList;
import searchengine.dto.index.IndexResponse;
import searchengine.model.Site;
import searchengine.model.StatusSite;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexService;
import searchengine.services.parser.SiteThread;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class IndexServiceImpl implements IndexService {

    private final PresetSitesList sites;
    private final List<SiteThread> threads;

    @Getter
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    IndexRepository indexRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Transactional
    @Override
    public IndexResponse startIndexing() {
        List<PresetSite> siteList = sites.getSites();
        IndexResponse response = new IndexResponse();
        if (getSiteRepository() != null) {
            stopThreadsAndWait();
        }

        for (PresetSite presetSite : siteList) {
            if (siteRepository.findByUrl(presetSite.getUrl()) != null) {
                deleteFromRepositories(presetSite);
            }
        }

        for (PresetSite presetSite : siteList) {
            saveNewSite(presetSite);

            SiteThread thread = new SiteThread(presetSite.getUrl(), siteRepository,
                pageRepository, lemmaRepository, indexRepository);
            threads.add(thread);
            thread.start();
            response.setResult(true);
        }
        return response;
    }

    @Override
    public IndexResponse stopIndexing() {
        IndexResponse response = new IndexResponse();
        stopThreadsAndWait();

        for (Site site : siteRepository.findAll()) {
            if (site.getStatus() == StatusSite.INDEXING) {
                site.setStatus(StatusSite.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                siteRepository.save(site);
            }
        }
        response.setResult(true);
        return response;
    }

    @Transactional
    public IndexResponse indexPage(String indexPage) {
        IndexResponse response = new IndexResponse();
        for (PresetSite presetSite : sites.getSites()) {
            if (presetSite.getUrl().contains(indexPage)) {
                response.setResult(true);

                deleteFromRepositories(presetSite);
                saveNewSite(presetSite);

                Thread thread = new SiteThread(indexPage, siteRepository,
                    pageRepository, lemmaRepository, indexRepository);
                thread.start();
                
                return response;
            }
        }
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов,\n" +
                "указанных в конфигурационном файле");
            return response;

    }

    private void saveNewSite(PresetSite presetSite) {
        Site site = new Site();
        site.setName(presetSite.getName());
        site.setUrl(presetSite.getUrl());
        site.setStatus(StatusSite.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        getSiteRepository().save(site);

    }

    private void deleteFromRepositories(PresetSite presetSite) {
        Site site = siteRepository.findByUrl(presetSite.getUrl());
        indexRepository.deleteAllBySiteId(site);
        pageRepository.deleteAllBySiteId(siteRepository.findByUrl(presetSite.getUrl()));
        siteRepository.deleteSiteById(site.getId());
    }

    private void stopThreadsAndWait(){
        if (threads.isEmpty()){
            return;
        }
        for (SiteThread siteThread : threads) {
            if (siteThread.isAlive()) {
                siteThread.stopParsing();
            }
            try {
                siteThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
