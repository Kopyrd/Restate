package com.example.restate.service.impl;

import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.exception.ResourceNotFoundException;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.service.MieszkanieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MieszkanieServiceImpl implements MieszkanieService {

    private final MieszkanieRepository mieszkanieRepository;
    private final EntityManager entityManager;

    @Override
    public List<Mieszkanie> findAll() {
        return mieszkanieRepository.findAll();
    }

    @Override
    public Optional<Mieszkanie> findById(Integer id) {
        return mieszkanieRepository.findById(id);
    }

    @Override
    public Mieszkanie save(Mieszkanie mieszkanie) {
        return mieszkanieRepository.save(mieszkanie);
    }

    @Override
    public Mieszkanie update(Integer id, Mieszkanie mieszkanie) {
        Mieszkanie existing = mieszkanieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie not found with id: " + id));

        existing.setDeveloper(mieszkanie.getDeveloper());
        existing.setInvestment(mieszkanie.getInvestment());
        existing.setNumber(mieszkanie.getNumber());
        existing.setArea(mieszkanie.getArea());
        existing.setPrice(mieszkanie.getPrice());
        existing.setStatus(mieszkanie.getStatus());
        existing.setDescription(mieszkanie.getDescription());

        return mieszkanieRepository.save(existing);
    }

    @Override
    public void deleteById(Integer id) {
        if (!mieszkanieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mieszkanie not found with id: " + id);
        }
        mieszkanieRepository.deleteById(id);
    }

    @Override
    public List<Mieszkanie> findByDeveloper(String developer) {
        return mieszkanieRepository.findByDeveloper(developer);
    }

    @Override
    public List<Mieszkanie> findByInvestment(String investment) {
        return mieszkanieRepository.findByInvestment(investment);
    }


    @Override
    public List<Mieszkanie> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return mieszkanieRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<Mieszkanie> findByAreaRange(BigDecimal minArea, BigDecimal maxArea) {
        return mieszkanieRepository.findByAreaRange(minArea, maxArea);
    }

    @Override
    public List<Mieszkanie> searchByCriteria(MieszkanieSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Mieszkanie> query = cb.createQuery(Mieszkanie.class);
        Root<Mieszkanie> mieszkanie = query.from(Mieszkanie.class);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getDeveloper() != null && !criteria.getDeveloper().isEmpty()) {
            predicates.add(cb.equal(mieszkanie.get("developer"), criteria.getDeveloper()));
        }

        if (criteria.getInvestment() != null && !criteria.getInvestment().isEmpty()) {
            predicates.add(cb.equal(mieszkanie.get("investment"), criteria.getInvestment()));
        }

        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(mieszkanie.get("price"), criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(mieszkanie.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getMinArea() != null) {
            predicates.add(cb.greaterThanOrEqualTo(mieszkanie.get("area"), criteria.getMinArea()));
        }

        if (criteria.getMaxArea() != null) {
            predicates.add(cb.lessThanOrEqualTo(mieszkanie.get("area"), criteria.getMaxArea()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<String> getAllDevelopers() {
        return mieszkanieRepository.findAllDevelopers();
    }

    @Override
    public List<String> getInvestmentsByDeveloper(String developer) {
        return mieszkanieRepository.findInvestmentsByDeveloper(developer);
    }

    @Override
    public Mieszkanie changeStatus(Integer id, Mieszkanie.Status status) {
        Mieszkanie mieszkanie = mieszkanieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie not found with id: " + id));
        mieszkanie.setStatus(status);
        return mieszkanieRepository.save(mieszkanie);
    }
}