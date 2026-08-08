package com.example.carsharingapp.repository.specification.rental;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.specification.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IdSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String ID_KEY = "id";

    @Override
    public String getKey() {
        return ID_KEY;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> root.get(ID_KEY)
                .in(Arrays.stream(params).toArray());
    }
}
