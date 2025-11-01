package ru.otus.ormfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import ru.otus.crm.model.Student;
import ru.otus.jdbc.mapper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @FXML
    private TextArea taInfo;

    @FXML
    private TableView<Student> tvMainTable;
    @FXML
    private Label lblInfo;

    private ObservableList<Student> studentsObservable;

    private EntityManagerImpl<Student> em;

    @FXML
    private void initialize() {

        EntityClassMetaData<Student> studentMetaData = new EntityClassMetaDataImpl<>(Student.class);
        EntitySQLMetaData entitySQLMetaData = new EntitySQLMetaDataImpl(studentMetaData);
        System.out.println(entitySQLMetaData.getSelectAllSql());

        var transactionRunner = new TransactionRunnerJdbc(MainApplication.getDataSource());
        var dbExecutor = new DbExecutorImpl();
        var dataTemplate = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaData, studentMetaData);

        em = new EntityManagerImpl<>(transactionRunner, dbExecutor, studentMetaData, entitySQLMetaData, dataTemplate);

        TableViewBuilder.bindTableView(tvMainTable, Student.class, studentMetaData);

        studentsObservable = em.createBoundList(Student.class, tvMainTable);
        tvMainTable.setItems(studentsObservable);


        lblInfo.setText("Инициализация прошла успешно");

    }

    @FXML
    void onAddBtnClick() {
        var student = new Student(null, "", "", 1, "", "");
        showDialog(student);
        em.save(student);
        taInfo.setText(studentsObservable.toString());
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
        Student student = tvMainTable.getSelectionModel().getSelectedItem();
        if (student == null) {
            return;
        }
        em.delete(student);
        studentsObservable.remove(student);
    }

    @FXML
    void onEditClick() {
        Student student = tvMainTable.getSelectionModel().getSelectedItem();

        if (student == null) {
            return;
        }
        showDialog(student);
        em.save(student);

        tvMainTable.refresh();
    }

    @FXML
    void onExitClick() {
        Platform.exit();
    }

}
