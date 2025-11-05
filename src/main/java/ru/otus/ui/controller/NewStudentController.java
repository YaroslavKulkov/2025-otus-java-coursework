package ru.otus.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import ru.otus.crm.model.Student;

/** Контроллер для окна создания нового студента. */
public class NewStudentController {
    @FXML
    private TextField tfCourse;

    @FXML
    private TextField tfGroup;

    @FXML
    private TextField tfLastName;

    @FXML
    private TextField tfMail;

    @FXML
    private TextField tfName;


    private Student student;
    @Setter
    private Stage stage;

    public void setStudent(Student student) {
        this.student = student;
        tfName.setText(student.getFirstName());
        tfLastName.setText(student.getLastName());
        tfMail.setText(student.getMail());
        tfGroup.setText(student.getGroup());
        tfCourse.setText(String.valueOf(student.getCourse()));
    }

    public void onOkButtonClick() {
        student.setFirstName(tfName.getText());
        student.setLastName(tfLastName.getText());
        student.setMail(tfMail.getText());
        student.setGroup(tfGroup.getText());
        student.setCourse(Integer.parseInt(tfCourse.getText()));
        stage.close();

    }

    public void onCancelButtonClick() {
        stage.close();
    }
}
