package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Modifying
    @Query(value = "delete i from search_index i join Page p on i.page_id = p.page_id where p.site_id = :site_id", nativeQuery = true)
    void deleteBySiteId(@Param("site_id") Site site_id);
}
