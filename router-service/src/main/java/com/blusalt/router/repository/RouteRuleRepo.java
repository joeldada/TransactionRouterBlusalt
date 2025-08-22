package com.blusalt.router.repository;

import com.blusalt.router.model.RouteRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRuleRepo extends JpaRepository<RouteRule, Long> {

    List<RouteRule> findByEnabledTrueOrderByPriorityAsc();

}
