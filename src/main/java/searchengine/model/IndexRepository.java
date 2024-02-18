package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Query(value = "delete from Index i join Page p on i.page_id = p.page_id where p.site_id = :site_id", nativeQuery = true)
    Index findBySiteId(@Param("site_id") int site_id);
}
