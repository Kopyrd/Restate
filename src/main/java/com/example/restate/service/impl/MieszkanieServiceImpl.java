package com.example.restate.service.impl;

import com.example.restate.entity.Mieszkanie;
import com.example.restate.exception.ResourceNotFoundException;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.service.MieszkanieService;
import com.example.restate.dto.MieszkanieSearchCriteria;
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
@Transactional
@RequiredArgsConstructor
public class MieszkanieServiceImpl implements MieszkanieService {

    private final MieszkanieRepository mieszkanieRepository;
    private final EntityManager entityManager;

    @Override
    public Mieszkanie save(Mieszkanie entity) {
        return mieszkanieRepository.save(entity);
    }

    @Override
    public Optional<Mieszkanie> findById(Integer id) {
        return mieszkanieRepository.findById(id);
    }

    @Override
    public List<Mieszkanie> findAll() {
        return mieszkanieRepository.findAll();
    }

    @Override
    public Mieszkanie update(Integer id, Mieszkanie entity) {
        Mieszkanie existing = mieszkanieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie not found with id: " + id));

        // Update fields
        existing.setDeveloper(entity.getDeveloper());
        existing.setInvestment(entity.getInvestment());
        existing.setNumber(entity.getNumber());
        existing.setArea(entity.getArea());
        existing.setPrice(entity.getPrice());
        existing.setRooms(entity.getRooms());
        existing.setLat(entity.getLat());
        existing.setLng(entity.getLng());
        existing.setDescription(entity.getDescription());


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
    public List<Mieszkanie> findByRooms(Integer rooms) {
        return mieszkanieRepository.findByRooms(rooms);
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
        // Implementacja wzorca
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Mieszkanie> query = cb.createQuery(Mieszkanie.class);
        Root<Mieszkanie> root = query.from(Mieszkanie.class);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getDeveloper() != null) {
            predicates.add(cb.equal(root.get("developer"), criteria.getDeveloper()));
        }

        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getMinArea() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("area"), criteria.getMinArea()));
        }

        if (criteria.getMaxArea() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("area"), criteria.getMaxArea()));
        }

        if (criteria.getRooms() != null) {
            predicates.add(cb.equal(root.get("rooms"), criteria.getRooms()));
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
    public Mieszkanie changeStatus(Integer id, Mieszkanie.Status newStatus) {
        Mieszkanie mieszkanie = mieszkanieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mieszkanie not found with id: " + id));

        mieszkanie.setStatus(newStatus);
        return mieszkanieRepository.save(mieszkanie);
    }
}