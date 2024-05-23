package searchengine.model;

import javax.persistence.*;

@Entity
@Table(name = "search_index")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private Page pageId;

    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private Lemma lemmaId;

    @Column(nullable = false)
    private float rate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page getPageId() {
        return pageId;
    }

    public void setPageId(Page page_id) {
        this.pageId = page_id;
    }

    public Lemma getLemmaId() {
        return lemmaId;
    }

    public void setLemmaId(Lemma lemma_id) {
        this.lemmaId = lemma_id;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
