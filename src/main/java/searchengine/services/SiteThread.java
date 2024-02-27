package searchengine.services;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@RequiredArgsConstructor
public class SiteThread extends Thread {

    private final String url;
    private final SiteService siteService;

    private PageServiceTask pageServiceTask;
    private ForkJoinPool forkJoinPool;

    @Override
    public void run() {
        pageServiceTask = new PageServiceTask(url, siteService);
        forkJoinPool = new ForkJoinPool();

        try {
            forkJoinPool.invoke(pageServiceTask);
        } catch (CancellationException e) {
            e.getMessage();
        }
        pageServiceTask.join();

        if (pageServiceTask.isCancelled()) {
            forkJoinPool.shutdownNow();
        }
        siteService.setIndexed(siteService.getSite(url));
    }

    public void stopParsing() {
        pageServiceTask.cancel(true);

        siteService.setFailedAfterCancel(siteService.getSiteRepository().findByUrl(url));
    }

}
