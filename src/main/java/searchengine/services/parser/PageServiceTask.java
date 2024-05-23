package searchengine.services.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.LemmaFinder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recursion task for site parsing
 *
 * @author Ivan_Okhlopkov
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PageServiceTask extends RecursiveAction {

    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    private String url;
    private volatile HashSet<PageServiceTask> taskList = new HashSet<>();

    public PageServiceTask(String url, SiteRepository siteRepository,
                           PageRepository pageRepository, LemmaRepository lemmaRepository,
                           IndexRepository indexRepository) {
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    protected void compute() {
        if (SiteThread.hasStopIndexing) {
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

        String regex = "^/[a-z0-9-/]+[^#/]";
        Pattern pattern = Pattern.compile(regex);

        for (Element element : elements) {
            String link = element.attr("href");
            Matcher matcher = pattern.matcher(link);

            if (!matcher.find() || link.contains("/#") || SiteThread.hasStopIndexing) {
                continue;
            }

            synchronized (getPageRepository()) {
                if (findPage(link)) {
                    continue;
                }

                savePage(link, document);
                findAndSaveLemma(link, document);
            }

            link = url + link;

            PageServiceTask task = new PageServiceTask(link, siteRepository,
                pageRepository, lemmaRepository, indexRepository);
            task.fork();
            taskList.add(task);
        }

        for (PageServiceTask task : taskList) {
            task.join();
        }
    }

    public boolean findPage(String link) {
        return pageRepository.findPageByPath(link, getSite(url)) != null;
    }

    public void savePage(String link, Document document) {
        Page page = new Page();

        Connection connection = document.connection();
        Connection.Response response = connection.response();

        page.setSiteId(getSite(url));
        page.setContent(document.toString());
        page.setPath(link);
        page.setCode(response.statusCode());

        getPageRepository().save(page);
    }

    private Document getConnect(String url) {
        Document document = null;
        try {
            Thread.sleep(500);
            document = Jsoup.connect(url)
                .userAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
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
        Page page = getPageRepository().findPageByPath(link, getSite(url));

        Map<String, Integer> lemmaMap = lemmaFinder.getLemma(document.toString());

        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {

            String word = entry.getKey();
            Integer value = entry.getValue();

            Lemma lemma = getLemmaRepository().findLemma(word, getSite(url));
            if (lemma != null) {
                lemma.setFrequency(lemma.getFrequency() + 1);
            } else {
                lemma = new Lemma();
                lemma.setSiteId(getSite(url));
                lemma.setLemma(word);
                lemma.setFrequency(1);
            }

            getLemmaRepository().save(lemma);

            Index index = new Index();
            index.setPageId(page);
            index.setLemmaId(getLemmaRepository()
                .findLemma(word, getSite(url)));
            index.setRate(value);

            getIndexRepository().save(index);
        }
    }

    public Site getSite(String url) {
        String regex = "^https://[a-z0-9.]+";
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
}
