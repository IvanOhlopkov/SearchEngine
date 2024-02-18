package searchengine.services;

import org.jsoup.nodes.Document;
import searchengine.model.Page;

public interface PageService {
    void parseElement(Document document);

}
