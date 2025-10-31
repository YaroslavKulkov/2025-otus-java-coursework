package ru.otus.ormfx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.executor.DbExecutorImpl;
import ru.otus.core.sessionmanager.TransactionRunnerJdbc;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Manager;
import ru.otus.crm.model.Student;
import ru.otus.crm.service.DbServiceClientImpl;
import ru.otus.crm.service.DbServiceManagerImpl;
import ru.otus.jdbc.mapper.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @FXML
    private TextArea taInfo;

    @FXML
    private TableView<?> tvMainTable;
    @FXML
    private Label lblInfo;

    private final List<Student> students = new ArrayList<>();

    private void initStudents() {
        students.add(new Student(1L, "Миша", "Зеленов", 1, "ИВТ-100", "misha@otus.ru"));
        students.add(new Student(2L, "Маша", "Краснова", 2, "ИВТ-201", "masha@otus.ru"));
        students.add(new Student(3L, "Семен", "Сиренев", 3, "ИС-201","semen@otus.ru"));

    }

    @FXML
    private void initialize() {
        initStudents();

        var dataSource = MainApplication.getDataSource();
        var transactionRunner = new TransactionRunnerJdbc(dataSource); // оборачивает запрос в транзакцию
        var dbExecutor = new DbExecutorImpl(); // непосредственно выполняет запрос

        EntityClassMetaData<Student> entityClassMetaDataStudent = new EntityClassMetaDataImpl<>(Student.class);
        EntitySQLMetaData entitySQLMetaDataStudent = new EntitySQLMetaDataImpl(entityClassMetaDataStudent);
        var dataTemplateStudent = new DataTemplateJdbc<Student>(
                dbExecutor,
                entitySQLMetaDataStudent,
                entityClassMetaDataStudent);
        System.out.println(entitySQLMetaDataStudent.getSelectAllSql());
        System.out.println(entitySQLMetaDataStudent.getInsertSql());
        System.out.println(entitySQLMetaDataStudent.getSelectByIdSql());
        System.out.println(entitySQLMetaDataStudent.getUpdateSql());

        lblInfo.setText("Инициализация прошла успешно");
        var columns = tvMainTable.getColumns();
        var column = columns.get(0);
        System.out.println(column.getId());
        tvMainTable = new TableView<Student>();
    }



    @FXML
    void onAddBtnClick() {
            var student = new Student(0, "", "", 1, "", "");
            showDialog(student);
            //TODO проверить на пустого студента перед добавлением
            students.add(student);
            taInfo.setText(students.toString());
    }

    private void showDialog(Student student) {
        var loader = new FXMLLoader();
        loader.setLocation(
                MainApplication.class.getResource("new-student.fxml")
        );
        Parent pane = null;
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var addStage = new Stage();
        addStage.setTitle("Информация о студенте");
        addStage.initModality(Modality.WINDOW_MODAL);
        addStage.initOwner(StageManager.getPrimaryStage());
        var scene = new Scene(pane);
        addStage.setScene(scene);
        NewStudentController controller = loader.getController();
        controller.setStage(addStage);
        controller.setStudent(student);
        addStage.showAndWait();

    }

    @FXML
    void onDeleteBtnClick() {
        //Student student = studentsTable.getSelectionModel().getSelectedItem();
        var student = students.get(0);
        //TODO проверить на удаление пустого студента
        students.remove(student);
        lblInfo.setText("Студент " + student.getLastName() + " удален.");
//        if (students.isEmpty()){
//            btDelete.setDisable(true);
//        }
        taInfo.setText(students.toString());
    }

    @FXML
    void onEditClick() {
        //Student student = studentsTable.getSelectionModel().getSelectedItem();
        var student = students.get(0);
        if(student == null){
            return;
        }
        showDialog(student);

        //studentsTable.refresh();
        taInfo.setText(students.toString());
    }

    @FXML
    void onExitClick() {
        Platform.exit();
    }

}
