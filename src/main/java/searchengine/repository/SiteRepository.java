package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

import java.util.List;


@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    @Query("select s from Site s where s.url = :url")
    Site findByUrl(@Param("url") String url);

    @Query("select s.status from Site s where s.url = :url")
    String getStatusSite(@Param("url") String url);

    @Query("select s from Site s")
    List<Site> getAllSites();

    void deleteSiteById(int id);

}
