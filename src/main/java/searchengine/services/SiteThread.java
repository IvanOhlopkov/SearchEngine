package searchengine.services;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

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
        if (pageServiceTask.isCancelled()) {
            forkJoinPool.shutdown();

        } else if (pageServiceTask.isDone()) {
            siteService.setIndexed(siteService.getSiteRepository().findByUrl(url));
        }
    }

    public void stopParsing() {
        pageServiceTask.cancelTask();
        pageServiceTask.cancel(true);
        siteService.setFailedAfterCancel(siteService.getSiteRepository().findByUrl(url));
    }

}
