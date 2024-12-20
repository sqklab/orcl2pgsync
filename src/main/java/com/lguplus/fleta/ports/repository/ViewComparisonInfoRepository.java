package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.comparison.ComparisonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViewComparisonInfoRepository extends JpaRepository<ComparisonEntity, Integer> {

}
