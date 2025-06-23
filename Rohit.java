import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class Rohit extends Application {

    @Override
    public void start(Stage stage) {
        // Input Fields
        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Enter Student ID");

        TextField studentNameField = new TextField();
        studentNameField.setPromptText("Enter Student Name");

        TextField studentEmailField = new TextField();
        studentEmailField.setPromptText("Enter Student Email");

        TextField courseIdField = new TextField();
        courseIdField.setPromptText("Enter Course ID");

        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Enter Course Name");

        // Buttons
        Button submitButton = new Button("➕ Submit");
        Button updateButton = new Button("📝 Update");
        Button deleteButton = new Button("🗑️ Delete");
        Button fetchButton = new Button("🔍 Fetch");
        Button clearButton = new Button("🧹 Clear");

        // Output Area
        Label statusLabel = new Label();
        TextArea dataArea = new TextArea();
        dataArea.setEditable(false);
        dataArea.setPrefHeight(250);

        // Style Buttons
        submitButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        updateButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        fetchButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
        clearButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, submitButton, updateButton, deleteButton, fetchButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Submit Logic
        submitButton.setOnAction(e -> {
            try {
                int studentId = Integer.parseInt(studentIdField.getText());
                String studentName = studentNameField.getText();
                String studentEmail = studentEmailField.getText();
                int courseId = Integer.parseInt(courseIdField.getText());
                String courseName = courseNameField.getText();

                if (studentName.isEmpty() || studentEmail.isEmpty() || courseName.isEmpty()) {
                    statusLabel.setText("Please fill all fields.");
                    return;
                }

                Connection con = connect();
                PreparedStatement pst1 = con.prepareStatement("INSERT INTO students VALUES (?, ?, ?)");
                pst1.setInt(1, studentId);
                pst1.setString(2, studentName);
                pst1.setString(3, studentEmail);
                pst1.executeUpdate();

                PreparedStatement pst2 = con.prepareStatement("INSERT INTO courses VALUES (?, ?)");
                pst2.setInt(1, courseId);
                pst2.setString(2, courseName);
                pst2.executeUpdate();

                PreparedStatement pst3 = con.prepareStatement("INSERT INTO enrollments VALUES (?, ?)");
                pst3.setInt(1, studentId);
                pst3.setInt(2, courseId);
                pst3.executeUpdate();

                statusLabel.setText("✅ Inserted successfully!");
                clearFields(studentIdField, studentNameField, studentEmailField, courseIdField, courseNameField);
                con.close();
            } catch (SQLIntegrityConstraintViolationException ex) {
                statusLabel.setText("❌ Duplicate or FK error: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                statusLabel.setText("❗ ID fields must be numbers.");
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });

        // Update Logic
        updateButton.setOnAction(e -> {
            try {
                int studentId = Integer.parseInt(studentIdField.getText());
                String studentName = studentNameField.getText();
                String studentEmail = studentEmailField.getText();
                int courseId = Integer.parseInt(courseIdField.getText());
                String courseName = courseNameField.getText();

                Connection con = connect();
                PreparedStatement pst1 = con.prepareStatement("UPDATE students SET name = ?, email = ? WHERE student_id = ?");
                pst1.setString(1, studentName);
                pst1.setString(2, studentEmail);
                pst1.setInt(3, studentId);
                pst1.executeUpdate();

                PreparedStatement pst2 = con.prepareStatement("UPDATE courses SET name = ? WHERE course_id = ?");
                pst2.setString(1, courseName);
                pst2.setInt(2, courseId);
                pst2.executeUpdate();

                statusLabel.setText("✅ Updated successfully!");
                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });

        // Delete Logic with Confirmation
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        int studentId = Integer.parseInt(studentIdField.getText());
                        int courseId = Integer.parseInt(courseIdField.getText());

                        Connection con = connect();
                        PreparedStatement pst1 = con.prepareStatement("DELETE FROM enrollments WHERE student_id = ? AND course_id = ?");
                        pst1.setInt(1, studentId);
                        pst1.setInt(2, courseId);
                        pst1.executeUpdate();

                        PreparedStatement pst2 = con.prepareStatement("DELETE FROM students WHERE student_id = ?");
                        pst2.setInt(1, studentId);
                        pst2.executeUpdate();

                        PreparedStatement pst3 = con.prepareStatement("DELETE FROM courses WHERE course_id = ?");
                        pst3.setInt(1, courseId);
                        pst3.executeUpdate();

                        statusLabel.setText("🗑️ Deleted successfully!");
                        clearFields(studentIdField, studentNameField, studentEmailField, courseIdField, courseNameField);
                        con.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusLabel.setText("❌ Error: " + ex.getMessage());
                    }
                }
            });
        });

        // Fetch Logic
        fetchButton.setOnAction(e -> {
            dataArea.clear();
            try {
                Connection con = connect();
                Statement stmt = con.createStatement();

                dataArea.appendText("📘 Students:\n");
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM students");
                while (rs1.next()) {
                    dataArea.appendText("ID: " + rs1.getInt("student_id") + ", Name: " + rs1.getString("name") + ", Email: " + rs1.getString("email") + "\n");
                }

                dataArea.appendText("\n📗 Courses:\n");
                ResultSet rs2 = stmt.executeQuery("SELECT * FROM courses");
                while (rs2.next()) {
                    dataArea.appendText("Course ID: " + rs2.getInt("course_id") + ", Name: " + rs2.getString("name") + "\n");
                }

                dataArea.appendText("\n📙 Enrollments:\n");
                ResultSet rs3 = stmt.executeQuery("SELECT * FROM enrollments");
                while (rs3.next()) {
                    dataArea.appendText("Student ID: " + rs3.getInt("student_id") + ", Course ID: " + rs3.getInt("course_id") + "\n");
                }

                statusLabel.setText("✅ Data fetched!");
                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });

        // Clear Logic
        clearButton.setOnAction(e -> {
            clearFields(studentIdField, studentNameField, studentEmailField, courseIdField, courseNameField);
            dataArea.clear();
            statusLabel.setText("🧹 Cleared.");
        });

        // Layout
        VBox layout = new VBox(12,
                new Label("👤 Student Details:"), studentIdField, studentNameField, studentEmailField,
                new Label("📚 Course Details:"), courseIdField, courseNameField,
                buttonBox, statusLabel, dataArea);

        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f1f8e9;");
        layout.setAlignment(Pos.TOP_CENTER);

        // Scene
        Scene scene = new Scene(layout, 500, 700);
        stage.setScene(scene);
        stage.setTitle("Student-Course Manager");
        stage.show();
    }

    // DB Connection
    private Connection connect() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/stdinfo", "root", "Chaithu@112223");
    }

    // Clear all input fields
    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}