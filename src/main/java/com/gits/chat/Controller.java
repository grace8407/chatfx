package com.gits.chat;

import com.gits.ChatService.grpc.Chat;
import com.gits.ChatService.grpc.ChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import sun.net.www.MessageHeader;

import java.awt.*;
import java.net.URL;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class Controller implements Initializable {

    @FXML
    TextFlow chatFlow;

    @FXML
    TextField chatText;

    @FXML
    ScrollPane chatScrollPane;

    @FXML
    TextField userName;

    StreamObserver<Chat.ChatMessage> streamObserver;

    ChatServiceGrpc.ChatServiceStub chatService;

    public void postMessage() {
        ObservableList chatMessages = chatFlow.getChildren();
        Text text = new Text(chatText.getText() + "\n");
        text.setFill(Color.BLUE);

        chatMessages.add(text);

        if(chatFlow.getBoundsInLocal().getHeight() > chatScrollPane.getViewportBounds().getHeight()) {
            chatScrollPane.setVvalue(chatScrollPane.getViewportBounds().getHeight());
        }

        Chat.ChatMessage chatMessage = Chat.ChatMessage.newBuilder().setUser(userName.getText()).setMessage(text.getText()).build();
        streamObserver.onNext(chatMessage);

        chatText.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext(true).build();
        chatService = ChatServiceGrpc.newStub(channel);
        System.out.println("Intializing...");

        streamObserver = chatService.sendMessage(new StreamObserver<Chat.ChatMessage>() {
            @Override
            public void onNext(Chat.ChatMessage value) {
                ObservableList chatMessages = chatFlow.getChildren();
                Text text = new Text(value.getUser() + ": " + value.getMessage());

                text.setFill(Color.RED);

                Platform.runLater(() -> {
                    chatMessages.add(text);
                });
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                System.out.println("Disconnected");
            }

            @Override
            public void onCompleted() {
                System.out.println("Disconnected");
            }
        });
    }
}
