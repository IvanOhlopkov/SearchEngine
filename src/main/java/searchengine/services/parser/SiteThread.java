package searchengine.services.parser;

import lombok.NoArgsConstructor;
import searchengine.model.Site;
import searchengine.model.StatusSite;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

/**
 * This class starts threads for each site
 * and also stops and monitors status of tasks in forkjoinpool
 *
 * @author Ivan_Okhlopkov
 */
@NoArgsConstructor
public class SiteThread extends Thread {

    SiteRepository siteRepository;
    PageRepository pageRepository;
    LemmaRepository lemmaRepository;
    IndexRepository indexRepository;
    public static boolean hasStopIndexing = false;
    private String url;
    private PageServiceTask pageServiceTask;
    private ForkJoinPool forkJoinPool;

    public SiteThread(String url, SiteRepository siteRepository, PageRepository pageRepository,
                      LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    public void run() {
        hasStopIndexing = false;
        pageServiceTask = new PageServiceTask(url,siteRepository, pageRepository,
            lemmaRepository, indexRepository);
        forkJoinPool = new ForkJoinPool();

        try {
            forkJoinPool.invoke(pageServiceTask);
        } catch (CancellationException e) {
            e.getMessage();
        }
        if (pageServiceTask.isCompletedNormally() && !hasStopIndexing) {
            Site site = siteRepository.findByUrl(url);
            site.setStatus(StatusSite.INDEXED);
            siteRepository.save(site);
            pageServiceTask.isDone();
        }
    }

    public void stopParsing() {
        pageServiceTask.cancel(true);
        forkJoinPool.shutdownNow();
        hasStopIndexing = true;
        while (forkJoinPool.getRunningThreadCount() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
