create table client
(
    id   bigserial not null primary key,
    name varchar(50)
);
create table manager
(
    no   bigserial not null primary key,
    label varchar(50),
    param1 varchar(50)
);
create table students
(
    id int primary key,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    course smallint not null check (course between 1 and 6),
    group_title varchar(7) not null,
    email varchar(70) unique not null
);