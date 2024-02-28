package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.PresetSite;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
@Transactional
public class SiteService {


    @Autowired
    PageRepository pageRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Autowired
    IndexRepository indexRepository;

    private boolean isCancelTask;

    public PageRepository getPageRepository() {
        return pageRepository;
    }

    public SiteRepository getSiteRepository() {
        return siteRepository;
    }

    public LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    public IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public void saveNewSite(PresetSite presetSite) {
        Site site = new Site();
        site.setName(presetSite.getName());
        site.setUrl(presetSite.getUrl());
        site.setStatus(StatusSite.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        getSiteRepository().save(site);

    }

    public void setIndexed(Site site) {
        site.setStatus(StatusSite.INDEXED);
        siteRepository.save(site);
    }

    public void setFailedAfterCancel(Site site) {
        site.setStatus(StatusSite.FAILED);
        site.setLastError("Индексация остановлена пользователем");
    }

    public boolean findSite(PresetSite presetSite) {
        return siteRepository.findByUrl(presetSite.getUrl()) != null;
    }

    public void deleteDataSite(PresetSite presetSite) {
        indexRepository.deleteBySiteId(siteRepository.findByUrl(presetSite.getUrl()));
        pageRepository.deleteAllBySiteId(siteRepository.findByUrl(presetSite.getUrl()));
        siteRepository.delete(siteRepository.findByUrl(presetSite.getUrl()));
    }

    public Site getSite(String url) {
        String regex = "^https://[a-z.]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        String editURL = "";
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            editURL = url.substring(start, end);
        }
        return siteRepository.findByUrl(editURL);
    }

    public Integer getPageCount(Site site) {
        return pageRepository.getPageCount(site);
    }

    public Integer getLemmaCount(Site site) {
        return lemmaRepository.getCountLemma(site);
    }

    public String getStatusSite(String url) {
        return siteRepository.getStatusSite(url);
    }

//    public String getLastErrorSite(String url) {
//        return siteRepository.getLastErrorSite(url);
//    }
    public void hasStopIndexing(Boolean isCancel) {
        isCancelTask = isCancel;
    }

    public boolean isStoppedIndexing(){
        return isCancelTask;
    }

}
