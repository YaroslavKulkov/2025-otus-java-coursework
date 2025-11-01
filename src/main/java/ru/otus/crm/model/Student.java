package ru.otus.crm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.otus.jdbc.annotations.Column;
import ru.otus.jdbc.annotations.Entity;
import ru.otus.jdbc.annotations.Id;

@Entity(tableName = "students")
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Student {
    @Id
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "course")
    private int course;
    @Column(name = "group_title")
    private String group;
    @Column(name = "email")
    private String mail;
}
