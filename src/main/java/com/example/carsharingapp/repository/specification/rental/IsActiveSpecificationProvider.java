package com.example.carsharingapp.repository.specification.rental;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

public class IsActiveSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String IS_ACTIVE_KEY = "isActive";

    @Override
    public String getKey() {
        return IS_ACTIVE_KEY;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> root.get(IS_ACTIVE_KEY)
                .in(Arrays.stream(params).toArray());
    }
}
