package gui;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class Gui {
    public static void display() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chatter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLayout(new BorderLayout());
            TextInput textInput = new TextInput();
            frame.add(new JScrollPane(TextInput.textArea));
            TextInput.setOnSendListener(e -> {
                PrintWriter out = TextInput.getOut();
                String message = e.getActionCommand();
                String username = TextInput.getUserName();
                System.out.println("CLIENT [" + username + "]: " + message);
                if (out != null) {
                    out.println("[" + username + "]: " + message);
                }
            });

            frame.setVisible(true);
        });
    }
}
