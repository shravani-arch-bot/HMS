import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.*;

public class App extends Application {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    private TextArea outputArea;
    private Label outputTitle;

    @Override
    public void start(Stage stage) {

        Label title = new Label("🏥 CityCare Super Specialty Hospital");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Hospital Dashboard | JavaFX + Spring Boot + MySQL");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #dbeafe;");

        VBox header = new VBox(6, title, subtitle);
        header.setStyle("""
            -fx-background-color: linear-gradient(to right, #075985, #0f766e);
            -fx-padding: 28;
            -fx-background-radius: 0 0 30 30;
        """);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(patientTab(), wardTab(), doctorTab(), operationTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 14px;");

        outputTitle = new Label("Live Output");
        outputTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #075985;");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(false);
        outputArea.setPrefHeight(240);
        outputArea.setStyle("""
            -fx-control-inner-background: #f8fafc;
            -fx-text-fill: #0f172a;
            -fx-font-family: Consolas;
            -fx-font-size: 14px;
            -fx-border-color: #cbd5e1;
            -fx-border-radius: 12;
            -fx-background-radius: 12;
        """);

        VBox outputCard = new VBox(14, outputTitle, outputArea);
        outputCard.setStyle(cardStyle());

        VBox page = new VBox(20, header, tabPane, outputCard);
        page.setStyle("-fx-background-color: #eef6ff; -fx-padding: 0 24 24 24;");

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #eef6ff;");

        Scene scene = new Scene(scrollPane, 1100, 800);
        stage.setTitle("Hospital Management Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private Tab patientTab() {
        TextField patientId = input("Patient ID");
        TextField name = input("Patient Name");
        TextField age = input("Age");
        TextField gender = input("Gender");
        TextField disease = input("Disease / Problem");
        TextField phone = input("Phone Number");

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("HEART", "ICU", "ORTHO", "NEURO", "PEDIATRIC", "GENERAL", "MATERNITY", "EMERGENCY", "BASIC");
        type.setPromptText("Select Patient Type");
        type.setStyle(fieldStyle());
        type.setPrefHeight(44);

        Button admitBtn = primaryButton("➕ Admit / Checkup Patient");
        Button allPatientsBtn = secondaryButton("👥 Show All Patients");

        TextField dischargeId = input("DB Patient ID to Discharge");
        Button dischargeBtn = dangerButton("🚪 Discharge & Free Bed");

        GridPane grid = grid();
        grid.add(patientId, 0, 0);
        grid.add(name, 1, 0);
        grid.add(age, 0, 1);
        grid.add(gender, 1, 1);
        grid.add(disease, 0, 2);
        grid.add(phone, 1, 2);
        grid.add(type, 0, 3);
        grid.add(admitBtn, 1, 3);
        grid.add(allPatientsBtn, 0, 4);
        grid.add(dischargeId, 0, 5);
        grid.add(dischargeBtn, 1, 5);

        admitBtn.setOnAction(e -> {
            if (patientId.getText().isEmpty() || name.getText().isEmpty() || age.getText().isEmpty()
                    || gender.getText().isEmpty() || disease.getText().isEmpty()
                    || phone.getText().isEmpty() || type.getValue() == null) {
                showMessage("Please fill all patient details.");
                return;
            }

            String json = """
            {
                "patientId": "%s",
                "name": "%s",
                "age": %s,
                "gender": "%s",
                "disease": "%s",
                "phone": "%s",
                "type": "%s"
            }
            """.formatted(patientId.getText(), name.getText(), age.getText(), gender.getText(),
                    disease.getText(), phone.getText(), type.getValue());

            post("/api/hospital/admit", json, "Patient Saved");
        });

        allPatientsBtn.setOnAction(e -> get("/api/hospital/patients", "Patients"));

        dischargeBtn.setOnAction(e -> {
            if (dischargeId.getText().isEmpty()) {
                showMessage("Enter DB Patient ID to discharge.");
                return;
            }
            delete("/api/hospital/discharge/" + dischargeId.getText(), "Discharge Status");
            dischargeId.clear();
        });

        return tab("Patients", card("Patient Admission & Discharge", grid));
    }

    private Tab wardTab() {
        Button wardBtn = primaryButton("🛏 Show Ward Bed Availability");
        Button statusBtn = secondaryButton("📊 Show Complete Hospital Status");

        VBox box = new VBox(16, wardBtn, statusBtn);
        wardBtn.setOnAction(e -> get("/api/hospital/wards", "Ward Bed Availability"));
        statusBtn.setOnAction(e -> get("/api/hospital/status", "Hospital Status"));

        return tab("Wards", card("Ward & Bed Management", box));
    }

    private Tab doctorTab() {
        Button allDoctors = primaryButton("👨‍⚕️ Show All Doctors");
        Button freeDoctors = secondaryButton("✅ Show Free Doctors");

        VBox box = new VBox(16, allDoctors, freeDoctors);
        allDoctors.setOnAction(e -> get("/api/doctors", "Doctors"));
        freeDoctors.setOnAction(e -> get("/api/doctors/free", "Free Doctors"));

        return tab("Doctors", card("Doctor Availability", box));
    }

    private Tab operationTab() {
        TextField patientId = input("DB Patient ID");
        TextField doctorId = input("Doctor ID");
        TextField operationType = input("Operation Type");
        TextField operationDate = input("Date YYYY-MM-DD");
        TextField operationTime = input("Time HH:MM:SS");

        Button scheduleBtn = primaryButton("🗓 Schedule Operation");
        Button showOperations = secondaryButton("📋 Show Operations");

        TextField completeOperationId = input("Operation ID to Complete");
        Button completeOperationBtn = dangerButton("✅ Complete / Free Doctor");

        GridPane grid = grid();
        grid.add(patientId, 0, 0);
        grid.add(doctorId, 1, 0);
        grid.add(operationType, 0, 1);
        grid.add(operationDate, 1, 1);
        grid.add(operationTime, 0, 2);
        grid.add(scheduleBtn, 1, 2);
        grid.add(showOperations, 0, 3);
        grid.add(completeOperationId, 0, 4);
        grid.add(completeOperationBtn, 1, 4);

        scheduleBtn.setOnAction(e -> {
            if (patientId.getText().isEmpty() || doctorId.getText().isEmpty()
                    || operationType.getText().isEmpty() || operationDate.getText().isEmpty()
                    || operationTime.getText().isEmpty()) {
                showMessage("Please fill all operation details.");
                return;
            }

            String json = """
            {
                "patientId": %s,
                "doctorId": %s,
                "operationType": "%s",
                "operationDate": "%s",
                "operationTime": "%s"
            }
            """.formatted(patientId.getText(), doctorId.getText(), operationType.getText(),
                    operationDate.getText(), operationTime.getText());

            post("/api/operations", json, "Operation Status");
        });

        showOperations.setOnAction(e -> get("/api/operations", "Operations"));

        completeOperationBtn.setOnAction(e -> {
            if (completeOperationId.getText().isEmpty()) {
                showMessage("Enter Operation ID.");
                return;
            }
            put("/api/operations/complete/" + completeOperationId.getText(), "Operation Completed");
            completeOperationId.clear();
        });

        return tab("Operations", card("Operation Scheduling", grid));
    }

    private void showResponse(String title, String body) {
        outputTitle.setText(title);

        if (body.trim().startsWith("[")) {
            outputArea.setText(toTable(body));
        } else if (body.trim().startsWith("{") && body.contains("\"message\"")) {
            outputArea.setText(extractValue(body, "message") + "\n\n" + toTable(body));
        } else {
            outputArea.setText(body);
        }
    }

    private String toTable(String json) {
        List<String> objects = extractObjects(json);

        if (objects.isEmpty()) {
            return json;
        }

        Set<String> keys = new LinkedHashSet<>();

        for (String obj : objects) {
            Matcher m = Pattern.compile("\"(\\w+)\"\\s*:").matcher(obj);
            while (m.find()) keys.add(m.group(1));
        }

        StringBuilder sb = new StringBuilder();
        List<String> keyList = new ArrayList<>(keys);

        for (String key : keyList) {
            sb.append(String.format("%-18s", key));
        }

        sb.append("\n");
        sb.append("-".repeat(keyList.size() * 18)).append("\n");

        for (String obj : objects) {
            for (String key : keyList) {
                String value = extractValue(obj, key);
                if (value.length() > 16) value = value.substring(0, 15) + ".";
                sb.append(String.format("%-18s", value));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private List<String> extractObjects(String json) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("\\{[^{}]*\\}").matcher(json);
        while (m.find()) list.add(m.group());
        return list;
    }

    private String extractValue(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(\"([^\"]*)\"|null|\\d+)");
        Matcher m = p.matcher(json);

        if (m.find()) {
            String val = m.group(1);
            if (val == null || val.equals("null")) return "-";
            return val.replace("\"", "");
        }

        return "-";
    }

    private void get(String endpoint, String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            showResponse(title, response.body());

        } catch (Exception e) {
            showMessage("Backend connection failed. Run Spring Boot first.");
        }
    }

    private void post(String endpoint, String json, String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            showResponse(title, response.body());

        } catch (Exception e) {
            showMessage("Error sending data to backend.");
        }
    }

    private void put(String endpoint, String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            showResponse(title, response.body());

        } catch (Exception e) {
            showMessage("Error updating operation.");
        }
    }

    private void delete(String endpoint, String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            showResponse(title, response.body());

        } catch (Exception e) {
            showMessage("Error deleting/discharging patient.");
        }
    }

    private void showMessage(String message) {
        outputTitle.setText("Message");
        outputArea.setText(message);
    }

    private GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        return grid;
    }

    private Tab tab(String name, javafx.scene.Node content) {
        Tab tab = new Tab(name);
        tab.setContent(content);
        return tab;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(44);
        field.setStyle(fieldStyle());
        return field;
    }

    private String fieldStyle() {
        return """
            -fx-background-color: white;
            -fx-border-color: #cbd5e1;
            -fx-border-radius: 14;
            -fx-background-radius: 14;
            -fx-padding: 11;
            -fx-font-size: 14px;
        """;
    }

    private Button primaryButton(String text) {
        return styledButton(text, "linear-gradient(to right, #0284c7, #0f766e)");
    }

    private Button secondaryButton(String text) {
        return styledButton(text, "linear-gradient(to right, #6366f1, #2563eb)");
    }

    private Button dangerButton(String text) {
        return styledButton(text, "linear-gradient(to right, #ef4444, #b91c1c)");
    }

    private Button styledButton(String text, String bg) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(44);
        btn.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 14;
            -fx-cursor: hand;
        """.formatted(bg));
        return btn;
    }

    private VBox card(String title, javafx.scene.Node content) {
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #075985;");

        VBox box = new VBox(20, label, content);
        box.setStyle(cardStyle());
        return box;
    }

    private String cardStyle() {
        return """
            -fx-background-color: white;
            -fx-padding: 28;
            -fx-background-radius: 26;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 20, 0, 0, 6);
        """;
    }

    public static void main(String[] args) {
        launch();
    }
}