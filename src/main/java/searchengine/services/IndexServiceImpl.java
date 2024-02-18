package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.PresetSite;
import searchengine.config.PresetSitesList;
import searchengine.dto.index.IndexResponse;

import java.util.List;

@RequiredArgsConstructor
@Service
public class IndexServiceImpl implements IndexService {

    private final PresetSitesList sites;
    private final List<SiteThread> threads;

    @Autowired
    SiteService siteService;

    @Override
    public IndexResponse startIndexing() {
        List<PresetSite> siteList = sites.getSites();
        IndexResponse response = new IndexResponse();

        for (PresetSite presetSite : siteList) {
            if (siteService.findSite(presetSite)) {
                siteService.deleteDataSite(presetSite);
            }

            siteService.saveNewSite(presetSite);

            Thread thread = new SiteThread(presetSite.getUrl(), siteService);
            thread.start();
            response.setResult(true);
        }
        return response;
    }

    @Override
    public IndexResponse stopIndexing() {
        IndexResponse response = new IndexResponse();

        for (SiteThread siteThread : threads) {
            siteThread.stopParsing();
            response.setResult(true);
        }

        return response;
    }

    public IndexResponse indexPage(String indexPage) {
        IndexResponse response = new IndexResponse();
        for (PresetSite presetSite : sites.getSites()) {
            if (presetSite.getUrl().contains(indexPage)) {
                response.setResult(true);
                Thread thread = new SiteThread(indexPage, siteService);
                thread.start();
            } else {
                response.setResult(false);
                response.setError("Данная страница находится за пределами сайтов,\n" +
                        "указанных в конфигурационном файле");
            }

        }
        return response;
    }
}
