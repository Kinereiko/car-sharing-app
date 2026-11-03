package com.example.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CarInfoForUserDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.dto.rental.RentalInfoForUserDto;
import com.example.carsharingapp.dto.rental.RentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalReturnDto;
import com.example.carsharingapp.dto.rental.RentalSearchParameters;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.specification.rental.RentalSpecificationBuilder;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.service.rental.RentalServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 100L;
    private static final int INITIAL_INVENTORY = 2;
    private static final int RENTED_DAYS = 2;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalSpecificationBuilder rentalSpecificationBuilder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("""
            Verify the correct save rental from requestDto when requestDto exists
            """)
    public void save_ValidCreateRentalRequestDto() {
        RentalRequestDto requestDto = createTestRentalRequestDto();
        Car car = createTestCar();
        User user = createTestUser();
        Rental rental = createTestRental();
        RentalDto rentalDto = createTestRentalDto(rental);

        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto actual = rentalService.save(requestDto, authentication);

        assertEquals(rentalDto, actual);
        verify(carRepository).save(car);
        verify(rentalRepository).save(any(Rental.class));
        verify(notificationService).sendNewRental(rentalDto);
    }

    @Test
    @DisplayName("""
            Verify save decreases car inventory and builds rental with correct fields
            """)
    public void save_ValidCreateRentalRequestDto_DecreasesInventoryAndFillsRental() {
        RentalRequestDto requestDto = createTestRentalRequestDto();
        Car car = createTestCar();
        User user = createTestUser();
        Rental rental = createTestRental();
        RentalDto rentalDto = createTestRentalDto(rental);

        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        rentalService.save(requestDto, authentication);

        assertEquals(INITIAL_INVENTORY - 1, car.getInventory());

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(rentalCaptor.capture());
        Rental savedRental = rentalCaptor.getValue();
        assertSame(car, savedRental.getCar());
        assertSame(user, savedRental.getUser());
        assertEquals(LocalDate.now(), savedRental.getRentalDate());
        assertEquals(LocalDate.now().plusDays(RENTED_DAYS), savedRental.getReturnDate());
        assertNull(savedRental.getActualReturnDate());
        assertTrue(savedRental.isActive());
    }

    @Test
    @DisplayName("""
            Verify save throws EntityNotFoundException when car with given id doesn't exist
            """)
    public void save_NonExistingCarId_ThrowsEntityNotFoundException() {
        RentalRequestDto requestDto = createTestRentalRequestDto();
        requestDto.setCarId(INVALID_ID);

        when(carRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.save(requestDto, authentication));

        assertEquals("Can't find car with id: " + INVALID_ID, exception.getMessage());
        verify(carRepository, never()).save(any(Car.class));
        verifyNoInteractions(rentalRepository, rentalMapper, notificationService);
    }

    @Test
    @DisplayName("""
            Verify save throws RuntimeException when there is no available car of this model
            """)
    public void save_CarWithZeroInventory_ThrowsRuntimeException() {
        RentalRequestDto requestDto = createTestRentalRequestDto();
        Car car = createTestCar();
        car.setInventory(0);

        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rentalService.save(requestDto, authentication));

        assertEquals("There's not available car of this model: " + car.getModel(),
                exception.getMessage());
        assertEquals(0, car.getInventory());
        verify(carRepository, never()).save(any(Car.class));
        verifyNoInteractions(rentalRepository, rentalMapper, notificationService);
    }

    @Test
    @DisplayName("""
            Verify search returns list of rental dtos matching search parameters
            """)
    @SuppressWarnings("unchecked")
    public void search_ValidSearchParameters_ReturnsListOfRentalDtos() {
        RentalSearchParameters params = new RentalSearchParameters(
                new String[]{"1"}, new String[]{"true"});
        Specification<Rental> specification = mock(Specification.class);
        Rental rental = createTestRental();
        RentalDto rentalDto = createTestRentalDto(rental);

        when(rentalSpecificationBuilder.build(params)).thenReturn(specification);
        when(rentalRepository.findAll(specification)).thenReturn(List.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        List<RentalDto> actual = rentalService.search(params);

        assertEquals(List.of(rentalDto), actual);
        verify(rentalSpecificationBuilder).build(params);
        verify(rentalRepository).findAll(specification);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
            Verify search returns empty list when no rentals match search parameters
            """)
    @SuppressWarnings("unchecked")
    public void search_NoMatchingRentals_ReturnsEmptyList() {
        RentalSearchParameters params = new RentalSearchParameters(
                new String[]{String.valueOf(INVALID_ID)}, null);
        Specification<Rental> specification = mock(Specification.class);

        when(rentalSpecificationBuilder.build(params)).thenReturn(specification);
        when(rentalRepository.findAll(specification)).thenReturn(List.of());

        List<RentalDto> actual = rentalService.search(params);

        assertTrue(actual.isEmpty());
        verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("""
            Verify findAll returns rentals of the authenticated user
            """)
    public void findAll_AuthenticatedUser_ReturnsUserRentals() {
        User user = createTestUser();
        Rental rental = createTestRental();
        RentalInfoForUserDto infoDto = createTestRentalInfoForUserDto(rental);

        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.findAllByUserId(user.getId())).thenReturn(List.of(rental));
        when(rentalMapper.toUserInfoDto(rental)).thenReturn(infoDto);

        List<RentalInfoForUserDto> actual = rentalService.findAll(authentication);

        assertEquals(List.of(infoDto), actual);
        verify(rentalRepository).findAllByUserId(user.getId());
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
            Verify findAll returns empty list when the authenticated user has no rentals
            """)
    public void findAll_UserWithoutRentals_ReturnsEmptyList() {
        User user = createTestUser();

        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.findAllByUserId(user.getId())).thenReturn(List.of());

        List<RentalInfoForUserDto> actual = rentalService.findAll(authentication);

        assertTrue(actual.isEmpty());
        verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("""
            Verify findById returns rental dto when rental with given id exists
            """)
    public void findById_ExistingId_ReturnsRentalDto() {
        Rental rental = createTestRental();
        RentalDto rentalDto = createTestRentalDto(rental);

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto actual = rentalService.findById(VALID_ID);

        assertEquals(rentalDto, actual);
        verify(rentalRepository).findById(VALID_ID);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
            Verify findById throws RuntimeException when rental with given id doesn't exist
            """)
    public void findById_NonExistingId_ThrowsRuntimeException() {
        when(rentalRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rentalService.findById(INVALID_ID));

        assertEquals("Can't find rental with id: " + INVALID_ID, exception.getMessage());
        verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("""
            Verify setReturn uses given actual return date, closes rental and returns car
            """)
    public void setReturn_WithActualReturnDate_SetsGivenDateAndIncreasesInventory() {
        Rental rental = createTestRental();
        Car car = rental.getCar();
        LocalDate actualReturnDate = LocalDate.now().plusDays(1);
        RentalReturnDto returnDto = createTestRentalReturnDto(VALID_ID, actualReturnDate);

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));

        rentalService.setReturn(returnDto);

        assertEquals(actualReturnDate, rental.getActualReturnDate());
        assertFalse(rental.isActive());
        assertEquals(INITIAL_INVENTORY + 1, car.getInventory());
        verify(rentalRepository).save(rental);
        verify(carRepository).save(car);
    }

    @Test
    @DisplayName("""
            Verify setReturn uses today's date when actual return date isn't provided
            """)
    public void setReturn_WithoutActualReturnDate_SetsTodayDate() {
        Rental rental = createTestRental();
        Car car = rental.getCar();
        RentalReturnDto returnDto = createTestRentalReturnDto(VALID_ID, null);

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));

        rentalService.setReturn(returnDto);

        assertEquals(LocalDate.now(), rental.getActualReturnDate());
        assertFalse(rental.isActive());
        assertEquals(INITIAL_INVENTORY + 1, car.getInventory());
        verify(rentalRepository).save(rental);
        verify(carRepository).save(car);
    }

    @Test
    @DisplayName("""
            Verify setReturn throws RuntimeException when rental with given id doesn't exist
            """)
    public void setReturn_NonExistingId_ThrowsRuntimeException() {
        RentalReturnDto returnDto = createTestRentalReturnDto(INVALID_ID, null);

        when(rentalRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rentalService.setReturn(returnDto));

        assertEquals("Can't find rental with id: " + INVALID_ID, exception.getMessage());
        verify(rentalRepository, never()).save(any(Rental.class));
        verifyNoInteractions(carRepository);
    }

    private RentalRequestDto createTestRentalRequestDto() {
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(VALID_ID);
        requestDto.setRentedDays(RENTED_DAYS);
        return requestDto;
    }

    private RentalReturnDto createTestRentalReturnDto(Long id, LocalDate actualReturnDate) {
        RentalReturnDto returnDto = new RentalReturnDto();
        returnDto.setId(id);
        returnDto.setActualReturnDate(actualReturnDate);
        return returnDto;
    }

    private Rental createTestRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setCar(createTestCar());
        rental.setUser(createTestUser());
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(RENTED_DAYS));
        rental.setActualReturnDate(null);
        rental.setActive(true);
        return rental;
    }

    private Car createTestCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setModel("Mazda 3");
        car.setBrand("Mazda");
        car.setType(Car.TypeName.SEDAN);
        car.setInventory(INITIAL_INVENTORY);
        car.setDailyFee(BigDecimal.valueOf(48.50));
        return car;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setFirstName("Test");
        user.setLastName("Tester");
        user.setEmail("test@gmail.com");
        user.setPassword("test1test");
        user.setRoles(new HashSet<>(Set.of(createTestRole())));
        return user;
    }

    private Role createTestRole() {
        Role role = new Role();
        role.setId(VALID_ID);
        role.setRole(Role.RoleName.MANAGER);
        return role;
    }

    private RentalDto createTestRentalDto(Rental rental) {
        RentalDto rentalDto = new RentalDto();
        rentalDto.setId(rental.getId());
        rentalDto.setCarDto(createTestCarDto(rental.getCar()));
        rentalDto.setUserId(rental.getUser().getId());
        rentalDto.setRentalDate(rental.getRentalDate());
        rentalDto.setReturnDate(rental.getReturnDate());
        rentalDto.setActualReturnDate(rental.getActualReturnDate());
        return rentalDto;
    }

    private RentalInfoForUserDto createTestRentalInfoForUserDto(Rental rental) {
        RentalInfoForUserDto infoDto = new RentalInfoForUserDto();
        infoDto.setCarDto(createTestCarInfoForUserDto(rental.getCar()));
        infoDto.setRentalDate(rental.getRentalDate());
        infoDto.setReturnDate(rental.getReturnDate());
        infoDto.setActualReturnDate(rental.getActualReturnDate());
        return infoDto;
    }

    private CarDto createTestCarDto(Car car) {
        CarDto carDto = new CarDto();
        carDto.setId(car.getId());
        carDto.setModel(car.getModel());
        carDto.setBrand(car.getBrand());
        carDto.setType(car.getType());
        carDto.setInventory(car.getInventory());
        carDto.setDailyFee(car.getDailyFee());
        return carDto;
    }

    private CarInfoForUserDto createTestCarInfoForUserDto(Car car) {
        CarInfoForUserDto carDto = new CarInfoForUserDto();
        carDto.setModel(car.getModel());
        carDto.setBrand(car.getBrand());
        carDto.setType(car.getType());
        carDto.setDailyFee(car.getDailyFee());
        return carDto;
    }
}
