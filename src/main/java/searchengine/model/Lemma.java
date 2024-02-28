package searchengine.model;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site_id;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "lemma_id")
    private List<Index> indexes;

    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Index> indexes) {
        this.indexes = indexes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Site getSite_id() {
        return site_id;
    }

    public void setSite_id(Site site) {
        this.site_id = site;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
