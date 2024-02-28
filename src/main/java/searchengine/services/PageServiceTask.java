package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageServiceTask extends RecursiveAction {

    private final String url;
    private volatile HashSet<PageServiceTask> taskList = new HashSet<>();
    private volatile SiteService siteService;

    public PageServiceTask(String url, SiteService siteService) {
        this.url = url;
        this.siteService = siteService;
    }

    @Override
    protected void compute() {
        if (siteService.isStoppedIndexing()) {
            return;
        }
        Document document = getConnect(url);
        parseElement(document);
    }

    public void parseElement(Document document) {
        if (document == null) {
            return;
        }
        Elements elements = document.select("a[href]");
        List<PageServiceTask> taskList = new ArrayList<>();

        String regex = "^/[a-z0-9-/]+[^#]";
        Pattern pattern = Pattern.compile(regex);

        for (Element element : elements) {
            String link = element.attr("href");
            Matcher matcher = pattern.matcher(link);

            if (!matcher.find() || link.contains("/#") || siteService.isStoppedIndexing()) {
                continue;
            }

            synchronized (siteService.getPageRepository()) {
                if (findPage(link)) {
                    continue;
                }
                savePage(link, document);
                findAndSaveLemma(link, document);
            }

            link = url + link;

            PageServiceTask task = new PageServiceTask(link, siteService);
            task.fork();
            taskList.add(task);
        }

        for (PageServiceTask task : taskList) {
            task.join();
        }
    }

    public boolean findPage(String link) {
        return siteService.getPageRepository().findPageByPath(link) != null;
    }

    public void savePage(String link, Document document) {
        Page page = new Page();

        Connection connection = document.connection();
        Connection.Response response = connection.response();

        page.setSite_id(siteService.getSite(url));
        page.setContent(document.toString());
        page.setPath(link);
        page.setCode(response.statusCode());

        siteService.getPageRepository().save(page);
    }

    private Document getConnect(String url) {
        Document document = null;
        try {
            Thread.sleep(500);
            document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com")
                    .followRedirects(true)
                    .get();
        } catch (InterruptedException | IOException e) {
            if (e.getMessage().contains("Status=403")) {
                return null;
            }
        }
        return document;
    }

    public void stopIndexing() {
        synchronized (taskList) {
            for (PageServiceTask task : taskList) {
                task.cancel(true);
            }
        }
    }

    public void findAndSaveLemma(String link, Document document) {

        LemmaFinder lemmaFinder = new LemmaFinder();
        Page page = siteService.getPageRepository().findPageByPath(link);

        Map<String, Integer> lemmaMap = lemmaFinder.getLemma(document.toString());

        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {

            String word = entry.getKey();
            Integer value = entry.getValue();

            Lemma lemma = siteService.getLemmaRepository().findLemma(word, siteService.getSite(url));
            if (lemma != null) {
                lemma.setFrequency(lemma.getFrequency() + 1);
            } else {
                lemma = new Lemma();
                lemma.setSite_id(siteService.getSite(url));
                lemma.setLemma(word);
                lemma.setFrequency(1);
            }

            siteService.getLemmaRepository().save(lemma);

            Index index = new Index();
            index.setPage_id(page);
            index.setLemma_id(siteService.getLemmaRepository()
                    .findLemma(word, siteService.getSite(url)));
            index.setRate(value);

            siteService.getIndexRepository().save(index);
        }
    }

}
