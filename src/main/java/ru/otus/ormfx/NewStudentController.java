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
    public TextField tfName;
    public TextField tfLastName;
    public TextField tfAge;
    public Button btOk;
    private Student student;
    @Setter
    private Stage stage;

    @FXML
    void initialize(){}

    public void setStudent(Student student) {
        this.student = student;
        tfName.setText(student.getFirstName());
        tfLastName.setText(student.getLastName());
        tfAge.setText(String.valueOf(student.getCourse()));
    }

    public void onOkButtonClick(ActionEvent actionEvent) {

        student.setFirstName(tfName.getText());
        student.setLastName(tfLastName.getText());
        student.setCourse(Integer.parseInt(tfAge.getText()));
        stage.close();

    }

    public void onCancelButtonClick(ActionEvent actionEvent) {
        stage.close();
    }
}
