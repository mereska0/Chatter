package app.Gui;

import app.Server.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.PrintWriter;

public class Chat extends Application {
    private BorderPane root;
    private TextArea chatHistoryArea;
    private TextArea inputArea;
    private PrintWriter serverOut;
    private static Chat instance;

    @Override
    public void start(Stage stage) {
        instance = this;
        Client.setChatInstance(this);
        serverOut = Client.getServerWriter();
        root = new BorderPane();
        root.getStyleClass().add("root-pane");
        VBox centerPanel = createChatPanel();
        root.setCenter(centerPanel);
        HBox bottomPanel = createInputPanel();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Chat - " + StartPage.getName());
        stage.setScene(scene);
        stage.setResizable(true);

        stage.setOnCloseRequest(event -> {
            if (serverOut != null) {
                serverOut.println("/exit");
                serverOut.flush();
            }
        });

        stage.show();
    }

    private HBox createInputPanel() {
        Font font = Font.loadFont(
                getClass().getResourceAsStream("/fonts/SixtyFour.ttf"),
                14
        );

        HBox inputPanel = new HBox(0);
        inputPanel.setPadding(new Insets(5, 10, 5, 10));
        inputPanel.setAlignment(Pos.CENTER_LEFT);
        inputPanel.getStyleClass().add("input-panel");

        TextField prefix = new TextField(">");
        prefix.getStyleClass().add("prefix");
        prefix.setEditable(false);
        prefix.setPrefColumnCount(2);
        prefix.setPrefHeight(35);
        prefix.setFont(font);
        prefix.setStyle(
                "-fx-background-color: #262729;" +
                        "-fx-text-fill: #11fc00;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-border-width: 0;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 5px 2px 5px 5px;"
        );
        inputArea = new TextArea();
        inputArea.setFont(font);
        inputArea.setPromptText("Введите сообщение...");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(1);
        inputArea.setPrefHeight(35);
        inputArea.getStyleClass().add("input-area");
        inputArea.setStyle(
                "-fx-background-color: #262729;" +
                        "-fx-text-fill: #11fc00;" +
                        "-fx-control-inner-background: #262729;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 5px;" +
                        "-fx-border-width: 0;"
        );

        HBox combinedField = new HBox(0);
        combinedField.getStyleClass().add("combined-field");
        combinedField.getChildren().addAll(prefix, inputArea);

        HBox.setHgrow(inputArea, Priority.ALWAYS);
        HBox.setHgrow(combinedField, Priority.ALWAYS);
        inputArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                String message = inputArea.getText().trim();
                if (!message.isEmpty()) {
                    appendMessage(StartPage.getName(), message);
                    if (serverOut != null) {
                        serverOut.println(message);
                        serverOut.flush();
                    }
                    inputArea.clear();
                }
            }
        });

        inputPanel.getChildren().addAll(combinedField);
        return inputPanel;
    }

    private VBox createChatPanel() {
        Font font = Font.loadFont(
                getClass().getResourceAsStream("/fonts/SixtyFour.ttf"),
                12
        );

        VBox chatPanel = new VBox(10);
        chatPanel.setPadding(new Insets(20));
        chatPanel.setAlignment(Pos.TOP_LEFT);
        chatPanel.getStyleClass().add("form-panel");

        Label chatLabel = new Label("Chat: #general");
        chatLabel.setFont(font);
        chatLabel.getStyleClass().add("input-label");

        chatHistoryArea = new TextArea();
        chatHistoryArea.setFont(font);
        chatHistoryArea.setPromptText("Chat has started");
        chatHistoryArea.setWrapText(true);
        chatHistoryArea.setEditable(false);
        chatHistoryArea.setPrefRowCount(25);
        chatHistoryArea.getStyleClass().add("text-area");

        chatHistoryArea.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                chatHistoryArea.setScrollTop(Double.MAX_VALUE);
            });
        });

        VBox.setVgrow(chatHistoryArea, Priority.ALWAYS);

        chatPanel.getChildren().addAll(chatLabel, chatHistoryArea);
        return chatPanel;
    }

    public void appendMessage(String name, String message) {
        if (chatHistoryArea != null) {
            Platform.runLater(() -> {
                chatHistoryArea.appendText(name + "> " + message + "\n");
            });
        }
    }

    public void close() {
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        });
    }
}