package com.example.carsharingapp.service.rental;

import com.example.carsharingapp.dto.rental.*;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.specification.rental.RentalSpecificationBuilder;
import com.example.carsharingapp.service.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;
    private final NotificationService notificationService;

    @Override
    public RentalDto save(RentalRequestDto requestDto, Authentication authentication) {
        Car car = carRepository.findById(requestDto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find car with id: "
                + requestDto.getCarId()));
        if (car.getInventory() == 0) {
            throw new RuntimeException("There's not available car of this model: "
                    + car.getModel());
        }
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        User user = getUserFromAuthentication(authentication);
        Rental rental = rentalRepository.save(createRental(requestDto, car, user));
        RentalDto responseDto = rentalMapper.toDto(rental);
        notificationService.sendMessage(responseDto);
        return responseDto;
    }

    @Override
    public List<RentalDto> search(RentalSearchParameters params) {
        Specification<Rental> rentalSpecification = rentalSpecificationBuilder.build(params);
        return rentalRepository.findAll(rentalSpecification)
                .stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public List<RentalInfoForUserDto> findAll(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return rentalRepository.findAllByUserId(user.getId())
                .stream()
                .map(rentalMapper::toUserInfoDto)
                .toList();
    }

    @Override
    public RentalDto findById(Long id) {
        return rentalMapper.toDto(rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can't find rental with id: " + id)));
    }

    @Override
    public void setReturn(RentalReturnDto returnDto) {
        Rental rental = rentalRepository.findById(returnDto.getId())
                .orElseThrow(() -> new RuntimeException("Can't find rental with id: "
                        + returnDto.getId()));
        if (returnDto.getActualReturnDate() == null) {
            rental.setActualReturnDate(LocalDate.now());
        } else {
            rental.setActualReturnDate(returnDto.getActualReturnDate());
        }
        rental.setActive(false);
        rentalRepository.save(rental);
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    private Rental createRental(RentalRequestDto requestDto, Car car, User user) {
        Rental rental = new Rental();
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(requestDto.getRentedDays()));
        rental.setCar(car);
        rental.setUser(user);
        return  rental;
    }
}
