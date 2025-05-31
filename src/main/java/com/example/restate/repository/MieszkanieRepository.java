package com.example.restate.repository;

import com.example.restate.entity.Mieszkanie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MieszkanieRepository extends JpaRepository<Mieszkanie, Integer> {

    List<Mieszkanie> findByDeveloper(String developer);

    List<Mieszkanie> findByInvestment(String investment);

    List<Mieszkanie> findByRooms(Integer rooms);

    List<Mieszkanie> findByStatus(Mieszkanie.Status status);

    @Query("SELECT m FROM Mieszkanie m WHERE m.price BETWEEN :minPrice AND :maxPrice")
    List<Mieszkanie> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT m FROM Mieszkanie m WHERE m.area BETWEEN :minArea AND :maxArea")
    List<Mieszkanie> findByAreaRange(@Param("minArea") BigDecimal minArea,
                                     @Param("maxArea") BigDecimal maxArea);

    @Query("SELECT DISTINCT m.developer FROM Mieszkanie m")
    List<String> findAllDevelopers();

    @Query("SELECT DISTINCT m.investment FROM Mieszkanie m WHERE m.developer = :developer")
    List<String> findInvestmentsByDeveloper(@Param("developer") String developer);
}