package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Query("select p from Page p where p.path = :path")
    Page findPageByPath(@Param("path") String path);

    @Query("delete from Page where site_id = :site_id")
    @Modifying
    void deleteAllBySiteId(@Param("site_id") int site_id);

}
