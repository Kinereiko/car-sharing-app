package com.example.carsharingapp.repository.specification.rental;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IsActiveSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String IS_ACTIVE_KEY = "isActive";

    @Override
    public String getKey() {
        return IS_ACTIVE_KEY;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            boolean isActive = Boolean.parseBoolean(params[0]);
            return criteriaBuilder.equal(root.get(IS_ACTIVE_KEY), isActive);
        };
    }
}
