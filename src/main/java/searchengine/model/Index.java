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
    private Page page_id;

    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private Lemma lemma_id;

    @Column(nullable = false)
    private float rate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page getPage_id() {
        return page_id;
    }

    public void setPage_id(Page page_id) {
        this.page_id = page_id;
    }

    public Lemma getLemma_id() {
        return lemma_id;
    }

    public void setLemma_id(Lemma lemma_id) {
        this.lemma_id = lemma_id;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
