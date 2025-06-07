package com.example.restate.repository;

import com.example.restate.entity.Mieszkanie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MieszkanieRepository extends JpaRepository<Mieszkanie, Integer> {

    // Non-paginated methods
    List<Mieszkanie> findByDeveloper(String developer);
    List<Mieszkanie> findByInvestment(String investment);
    List<Mieszkanie> findByStatus(Mieszkanie.Status status);

    @Query("SELECT m FROM Mieszkanie m WHERE m.price BETWEEN :minPrice AND :maxPrice")
    List<Mieszkanie> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice);


    // Paginated methods
    Page<Mieszkanie> findByDeveloper(String developer, Pageable pageable);
    Page<Mieszkanie> findByInvestment(String investment, Pageable pageable);
    Page<Mieszkanie> findByStatus(Mieszkanie.Status status, Pageable pageable);


    @Query(value = "SELECT m FROM Mieszkanie m WHERE m.price BETWEEN :minPrice AND :maxPrice",
           countQuery = "SELECT COUNT(m) FROM Mieszkanie m WHERE m.price BETWEEN :minPrice AND :maxPrice")
    Page<Mieszkanie> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, 
                                        @Param("maxPrice") BigDecimal maxPrice, 
                                        Pageable pageable);



    @Query("SELECT DISTINCT m.developer FROM Mieszkanie m")
    List<String> findAllDevelopers();

}
