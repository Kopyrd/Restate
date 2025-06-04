package com.example.restate.service;

import com.example.restate.entity.Mieszkanie;
import com.example.restate.dto.MieszkanieSearchCriteria;

import java.math.BigDecimal;
import java.util.List;
import com.example.restate.dto.UpdateMieszkanieDTO;

public interface MieszkanieService extends BaseService<Mieszkanie, Integer> {

    List<Mieszkanie> findByDeveloper(String developer);
    List<Mieszkanie> findByInvestment(String investment);
    List<Mieszkanie> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Mieszkanie> findByAreaRange(BigDecimal minArea, BigDecimal maxArea);
    List<Mieszkanie> searchByCriteria(MieszkanieSearchCriteria criteria);
    List<String> getAllDevelopers();
    List<String> getInvestmentsByDeveloper(String developer);
    Mieszkanie changeStatus(Integer id, Mieszkanie.Status newStatus);
    Mieszkanie updateFromDTO(Integer id, UpdateMieszkanieDTO dto);
}