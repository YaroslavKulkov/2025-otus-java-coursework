package ru.otus.ormfx;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import ru.otus.crm.model.Student;

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

    @FXML
    void initialize(){}

    public void setStudent(Student student) {
        this.student = student;
        tfName.setText(student.getFirstName());
        tfLastName.setText(student.getLastName());
        tfMail.setText(student.getMail());
        tfGroup.setText(student.getGroup());
        tfCourse.setText(String.valueOf(student.getCourse()));
    }

    public void onOkButtonClick(ActionEvent actionEvent) {

        student.setFirstName(tfName.getText());
        student.setLastName(tfLastName.getText());
        student.setMail(tfMail.getText());
        student.setGroup(tfGroup.getText());
        student.setCourse(Integer.parseInt(tfCourse.getText()));
        stage.close();

    }

    public void onCancelButtonClick(ActionEvent actionEvent) {
        stage.close();
    }
}
