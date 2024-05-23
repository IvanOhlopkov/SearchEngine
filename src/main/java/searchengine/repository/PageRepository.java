package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query("select p from Page p where p.path = :path and p.siteId = :site_id")
    Page findPageByPath(@Param("path") String path, @Param("site_id") Site site_id);

    @Modifying
    @Query("delete from Page p where p.siteId = :site_id")
    void deleteAllBySiteId(@Param("site_id") Site site_id);

    @Query("select count(*) from Page p where p.siteId = :site_id")
    Integer getPageCount(@Param("site_id") Site site_id);

}
