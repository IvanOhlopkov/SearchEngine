package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Modifying
    @Query("delete from Index i where i.pageId in (select p.id from Page p where p.siteId = :site)")
    void deleteAllBySiteId(@Param("site") Site site);

    @Query(value = "select * from search_index i where i.lemma_id = :lemma_id " +
        "and i.page_id in (page_id)", nativeQuery = true)
    List<Index> getAllIndex(@Param("lemma_id") Lemma lemma_id);

    @Query(value = "select * from search_index i where i.lemma_id = :lemma_id " +
        "and i.page_id = :page_id", nativeQuery = true)
    List<Index> getIndexFilterPage(@Param("lemma_id") Lemma lemma_id, @Param("page_id") String page_id);

    @Query(value = "select * from search_engine.search_index i " +
        "where i.lemma_id = :lemma_id order by i.page_id asc limit 1", nativeQuery = true)
    Index getFirstIndexByLemma(@Param("lemma_id") Lemma lemma_id);
}
