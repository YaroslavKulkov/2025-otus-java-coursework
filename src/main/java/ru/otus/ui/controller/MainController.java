package ru.otus.ui.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.executor.DbExecutorImpl;
import ru.otus.core.sessionmanager.TransactionRunnerJdbc;
import ru.otus.crm.model.Student;
import ru.otus.crm.repository.EntityManager;
import ru.otus.crm.repository.EntityManagerImpl;
import ru.otus.jdbc.mapper.*;
import ru.otus.ui.MainApplication;
import ru.otus.ui.manager.StageManager;
import ru.otus.crm.builder.TableViewBuilder;

import java.io.IOException;

/** Контроллер главного окна приложения. */
public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private TextArea taInfo;
    @FXML
    private TableView<Student> tvMainTable;

    private ObservableList<Student> studentsObservable;
    private EntityManager<Student> em;

    @FXML
    private void initialize() {
        EntityClassMetaData<Student> studentMetaData = new EntityClassMetaDataImpl<>(Student.class);
        EntitySQLMetaData entitySQLMetaData = new EntitySQLMetaDataImpl(studentMetaData);
        System.out.println(entitySQLMetaData.getSelectAllSql());

        var transactionRunner = new TransactionRunnerJdbc(MainApplication.getDataSource());
        var dbExecutor = new DbExecutorImpl();
        var dataTemplate = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaData, studentMetaData);

        em = new EntityManagerImpl<>(transactionRunner, dbExecutor, studentMetaData, entitySQLMetaData, dataTemplate);
        TableViewBuilder.bindTableView(tvMainTable, studentMetaData);
        studentsObservable = em.createBoundList(tvMainTable);

        tvMainTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onEditBtnClick();
            }
        });

        log.info("Initialization of component successful completed");
        taInfo.setText(String.format("Инициализация прошла успешно, загружено %d записей", studentsObservable.size()));

    }

    @FXML
    void onAddBtnClick() {
        var student = new Student(null, "", "", 1, "", "");
        showDialog(student);
        if(student.getId() == null) {
            return;
        }
        em.save(student);
        studentsObservable.add(student);
        taInfo.setText("Добавлен студент " + student.getLastName() + " в группу " + student.getGroup());
        log.info("Добавлен студент {}", student);
    }

    private void showDialog(Student student) {
        var loader = new FXMLLoader();
        loader.setLocation(
                MainApplication.class.getResource("new-student.fxml")
        );
        Parent pane;
        try {
            pane = loader.load();
        } catch (IOException e) {
            log.error("Ошибка при загрузке fxml-файла", e);
            return;
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
        log.info("Выбран для удаления {}", student);
        if (student == null) {
            return;
        }
        em.delete(student);
        studentsObservable.remove(student);
        log.info("Удален студент {}", student);
        taInfo.setText("Удален студент " + student.getLastName() + " из группы " + student.getGroup());
    }

    @FXML
    void onEditBtnClick() {
        Student student = tvMainTable.getSelectionModel().getSelectedItem();
        log.info("Выбран для редактирования {}", student);
        if (student == null) {
            return;
        }
        showDialog(student);
        em.save(student);
        log.info("Сохранен измененный студент {}", student);
        taInfo.setText("Сохранены изменения в студенте " + student.getLastName() + " в группе " + student.getGroup());
        tvMainTable.refresh();
    }

    @FXML
    void onExitClick() {
        Platform.exit();
    }

}
