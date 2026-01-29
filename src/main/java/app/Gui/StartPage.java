package app.Gui;

import app.Server.Server;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class StartPage extends Application {
private static String username = "";
private static String chatId = "";
    TextField nameField = new TextField();
    TextField IdField = new TextField();
public static String getName(){
    return username;
}
public static void setName(String name){
    username = name;
}
public static boolean isNicknameSet(){
    return !username.isEmpty();
}

    @Override
    public void start(Stage primaryStage) {
        // Создаем основной контейнер
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // === ВЕРХНЯЯ ПАНЕЛЬ - ЗАГОЛОВОК ===
        Label titleLabel = new Label("CHATTER");
        titleLabel.getStyleClass().add("title-label");
        Font font = Font.loadFont(
                getClass().getResourceAsStream("/fonts/SixtyFour.ttf"),
                24
        );
        titleLabel.setFont(font);

        HBox topPanel = new HBox(titleLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(20));
        topPanel.getStyleClass().add("top-panel");
        root.setTop(topPanel);

        VBox centerPanel = createFormPanel();
        root.setCenter(centerPanel);

        HBox bottomPanel = createButtonPanel();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Chatter");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private VBox createFormPanel() {
        Font font = Font.loadFont(
                getClass().getResourceAsStream("/fonts/SixtyFour.ttf"),
                10
        );
        VBox formPanel = new VBox(15);
        formPanel.setPadding(new Insets(30));
        formPanel.setAlignment(Pos.TOP_CENTER);
        formPanel.getStyleClass().add("form-panel");

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(15);
        inputGrid.setVgap(15);
        inputGrid.setPadding(new Insets(20));
        inputGrid.getStyleClass().add("input-grid");

        Label nameLabel = new Label("NAME:");
        nameLabel.setFont(font);
        nameLabel.getStyleClass().add("input-label");

        nameField.setFont(font);
        nameField.setPromptText("type your name");
        nameField.getStyleClass().add("text-field");
        nameField.setPrefWidth(250);

        Label IdLabel = new Label("Chat name/id:");
        IdLabel.setFont(font);
        IdLabel.getStyleClass().add("input-label");
        IdField.setFont(font);
        IdField.setPromptText("type chat name..");
        IdField.getStyleClass().add("text-field");

        inputGrid.add(nameLabel, 0, 0);
        inputGrid.add(nameField, 1, 0);
        inputGrid.add(IdLabel, 0, 2);
        inputGrid.add(IdField, 1, 2);

        formPanel.getChildren().add(inputGrid);

        return formPanel;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(25));
        buttonPanel.getStyleClass().add("button-panel");

        Button saveButton = new Button("Start chatting");
        saveButton.getStyleClass().add("save-button");
        saveButton.setPrefSize(300, 40);

        saveButton.setOnAction(e -> {
            try {
                username = nameField.getText();
                chatId = IdField.getText();
                Stage chatStage = new Stage();
                Chat chat = new Chat(chatId);
                chat.start(chatStage);
                ((Stage) saveButton.getScene().getWindow()).close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        buttonPanel.getChildren().add(saveButton);

        return buttonPanel;
    }
}