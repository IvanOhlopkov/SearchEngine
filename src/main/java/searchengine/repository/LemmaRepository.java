package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    @Query("select l from Lemma l where l.lemma = :word and l.siteId = :site_id")
    Lemma findLemma(@Param("word") String word, @Param("site_id") Site site_id);

    @Query("select count(*) from Lemma l where l.siteId = :site_id")
    Integer getCountLemma(@Param("site_id") Site site_id);
}
