insert into users (id, email, password, first_name, last_name) values
(1, 'admin@gmail.com', 'test01password', 'Admin', 'Tester');
insert into cars (id, model, brand, type, inventory, daily_fee) values
(1, 'Corolla', 'Toyota', 'SEDAN', 2, 42.50);
insert into rentals (id, rental_date, return_date, actual_return_date, car_id, user_id, is_active)
values (1, '2026-10-14', '2026-10-15', null, 1, 1, true);
insert into payments (id, rental_id, status, type, session_url, session_id, total_price)
values (1, 1, 'PENDING', 'FINE', 'testurl', '1234', 44.50);