package searchengine.model;

import javax.persistence.*;
import javax.persistence.Index;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page",
        indexes = @Index (columnList = "path", name = "idx_path", unique = true))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site_id;

    @Column(name = "path", columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "page_id")
    private List<searchengine.model.Index> indexes;

    public List<searchengine.model.Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<searchengine.model.Index> indexes) {
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

    public void setSite_id(Site site_id) {
        this.site_id = site_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
