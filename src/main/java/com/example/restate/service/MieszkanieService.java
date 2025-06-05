package com.example.restate.service;

import com.example.restate.dto.PageResponse;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.dto.MieszkanieSearchCriteria;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import com.example.restate.dto.UpdateMieszkanieDTO;

public interface MieszkanieService extends BaseService<Mieszkanie, Integer> {

    // Non-paginated methods
    List<Mieszkanie> findByDeveloper(String developer);
    List<Mieszkanie> findByInvestment(String investment);
    List<Mieszkanie> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Mieszkanie> findByAreaRange(BigDecimal minArea, BigDecimal maxArea);
    List<Mieszkanie> searchByCriteria(MieszkanieSearchCriteria criteria);

    // Paginated methods
    PageResponse<Mieszkanie> findAll(Pageable pageable);
    PageResponse<Mieszkanie> findByDeveloper(String developer, Pageable pageable);
    PageResponse<Mieszkanie> findByInvestment(String investment, Pageable pageable);
    PageResponse<Mieszkanie> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    PageResponse<Mieszkanie> findByAreaRange(BigDecimal minArea, BigDecimal maxArea, Pageable pageable);
    PageResponse<Mieszkanie> searchByCriteria(MieszkanieSearchCriteria criteria, Pageable pageable);

    // Utility methods
    List<String> getAllDevelopers();
    List<String> getInvestmentsByDeveloper(String developer);
    Mieszkanie changeStatus(Integer id, Mieszkanie.Status newStatus);
    Mieszkanie updateFromDTO(Integer id, UpdateMieszkanieDTO dto);
}
