create table students
(
    id bigserial not null primary key,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    course smallint not null check (course between 1 and 6),
    group_title varchar(7) not null,
    email varchar(70) unique not null
);