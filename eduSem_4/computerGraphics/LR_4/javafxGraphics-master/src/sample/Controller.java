package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Builder;
import sample.graphical.GraphicalObject;
import sample.graphical.entity.*;
import sample.graphical.formations.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.Line;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import java.lang.StrictMath.*;

import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;
import static sample.graphical.entity.CanvasOperations.*;
import static sample.graphical.entity.GraphicalProcessing.graphProcessing;
import static sample.graphical.formations.TimeRecording.getTimeOfWorkingFunctionDrawingLine;
import static sample.graphical.formations.TimeRecording.onShowHistogramStats;

public class Controller implements Initializable {

    private final double DRAW_RADIUS = 10.0D;

    Map<String, ObservableList<String>> objectsFields;
    Map<String, GraphicalObject> objectNamesToClassReference;

    List<GraphicalObject> objectList;
    List<GraphicalObject> copyObjectList;


    final public int NEEDED_NUMBER_OF_POINTS_FOR_COUNTING = 2;
    final public int OVER_LINE = 1;
    final public int ON_LINE = 0;
    final public int UNDER_LINE  = -1;

    public double ZOMM_COFF = 1.0;
    public double START_CANVAS_X = 0;
    public double START_CANVAS_Y = 0;

    @FXML
    private ChoiceBox algCircleType = new ChoiceBox(), colorCircleType = new ChoiceBox();

    @FXML
    private ChoiceBox algOvalType = new ChoiceBox(), colorOvalType = new ChoiceBox();

    public Controller() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        objectsFields = new HashMap<>();
        objectsFields.put("Point", GraphicalPoint.parametersToObservableList());

        objectNamesToClassReference = new HashMap<>();
        objectNamesToClassReference.put("Point", GraphicalPoint.builder().build());

        objectList = new ArrayList<>();
        copyObjectList = new ArrayList<>();

        algCircleType.getItems().addAll("Каноническое уравнение",
                "Параметрическое уравнение", "Алгоритм Брезенхема", "Алгоритм средней точки",
                "Построение при помощи библиотечной функции");

        colorCircleType.getItems().addAll("Черный", "Красный", "Зеленый", "Серый", "Желтый", "Синий");

        algOvalType.getItems().addAll("Каноническое уравнение",
                "Параметрическое уравнение", "Алгоритм Брезенхема", "Алгоритм средней точки",
                "Построение при помощи библиотечной функции");

        colorOvalType.getItems().addAll("Черный", "Красный", "Зеленый", "Серый", "Желтый", "Синий");
    }

    @FXML
    private Label parameterErrorField;

    @FXML
    private ListView<String> objectsPlacedList, objectsToPlaceList;

    @FXML
    private Canvas graphTable;

    @FXML
    private ScrollPane scrollPanel;

    @FXML Button graphCircleButton, graphOvalButton, spectrCircleButton, spectrOvalButton;

    // Кнопка очистки канваса от всех данных и возврат обратно
    @FXML
    private Button clearAllButton, comeBack, timeCircleButton, timeOvalButton;

    @FXML
    private TextField circleCenterX, circleCenterY, circleRadious,
            ovalCenterX, ovalCenterY, ovalLeftRadious, ovalTopRadious,
            numberOfSpectrCircle, radiousDeltaSpectrCircle, radiousDeltaSpectrOval, numberOfSpectrOval;

    @FXML
    public void onComeBack() {
        comeBack.setOnAction(actionEvent -> {
            getCopyFunction();
            objectsPlacedList.setItems(
                    FXCollections.observableList(objectList.stream().map(Object::toString).collect(Collectors.toList())));
            redrawElements(graphTable, objectList);
        });
    }

    // Копия создания дублежа
    @FXML
    public void createCopyFunction() {
        // Очистка массива дубляжей
        copyObjectList.clear();

        // Добавление всех элементов
        for (int tempNumOfObject = 0; tempNumOfObject < objectList.size(); tempNumOfObject++)
            copyObjectList.add(objectList.get(tempNumOfObject));
    }

    // Получение дублирующий копии в оригинал
    @FXML
    public void getCopyFunction() {
        // Очистка массива дубляжей
        objectList.clear();

        // Добавление всех элементов
        for (int tempNumOfObject = 0; tempNumOfObject < copyObjectList.size(); tempNumOfObject++)
            objectList.add(copyObjectList.get(tempNumOfObject));
    }

    @FXML
    public void onClearAllButton() {
        clearAllButton.setOnAction(event -> {
            createCopyFunction();

            objectList.clear();

            redrawElements(graphTable, objectList);
        });
    }

    @FXML
    public void onCreateCanvas()
    {
        scrollPanel.widthProperty().addListener(event -> {
            graphTable.setWidth(scrollPanel.getWidth());
            redrawElements(graphTable, objectList);
        });

        scrollPanel.heightProperty().addListener(event -> {
            graphTable.setHeight(scrollPanel.getHeight());
            redrawElements(graphTable, objectList);
        });
    }

    // Перерисовка элементов экрана
    private void redrawElements(Canvas graphTable, List<GraphicalObject> objectList)
    {
        graphTable.getGraphicsContext2D().clearRect(0, 0, graphTable.getWidth(), graphTable.getHeight());
        objectList.forEach(graphicalObject -> graphicalObject.draw(graphTable.getGraphicsContext2D()));
    }

    @FXML
    public void onGraph() {

        // Нажатие операции над Окружностью
        graphCircleButton.setOnAction(actionEvent -> {
            if (algCircleType.getValue() != null && colorCircleType.getValue() != null)
            {
                try {
                    graphProcessing(objectList, Integer.parseInt(circleCenterX.getText()), Integer.parseInt(circleCenterY.getText()),
                            Integer.parseInt(circleRadious.getText()), Integer.parseInt(circleRadious.getText()),
                            CanvasOperations.getColor(colorCircleType.getValue().toString()),
                            algCircleType.getValue().toString(), false);
                    redrawElements(graphTable, objectList);
                } catch (Exception e) {
                    e.printStackTrace();
                    parameterErrorField.setText("Допущена ошибка.");
                }
            }
        });

        // Нажатие операции над Овалом
        graphOvalButton.setOnAction(actionEvent -> {
            if (algOvalType.getValue() != null && colorOvalType.getValue() != null)
            {
                try {
                    graphProcessing(objectList, Integer.parseInt(ovalCenterX.getText()), Integer.parseInt(ovalCenterY.getText()),
                            Integer.parseInt(ovalLeftRadious.getText()), Integer.parseInt(ovalTopRadious.getText()),
                            CanvasOperations.getColor(colorOvalType.getValue().toString()),
                            algOvalType.getValue().toString(), true);
                    redrawElements(graphTable, objectList);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    parameterErrorField.setText("Допущена ошибка.");
                }
            }
        });
    }

    @FXML
    private void getTime()
    {

        timeCircleButton.setOnAction(actionEvent -> {
            int tempCenterX = 500, tempCenterY = 500;
            int tempTopRadious = 1000;

            Color tempColor = Color.RED;

            int tempLeftRadious = 1000;

            //Defining the x axis
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Время");

            //Defining the y axis
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Радиус");

            LineChart<Number, Number> linechart = new LineChart<>(xAxis, yAxis);

            XYChart.Series<Number, Number> seriesCanonicalEquation = new XYChart.Series<>();
            seriesCanonicalEquation.setName("Каноническое");

            XYChart.Series<Number, Number> seriesParametricalEquation = new XYChart.Series<>();
            seriesParametricalEquation.setName("Параметрическое");

            XYChart.Series<Number, Number> seriesBrezenchemsEquation = new XYChart.Series<>();
            seriesBrezenchemsEquation.setName("Брезенхема");

            XYChart.Series<Number, Number> seriesMiddlePointAlgorithmEquation = new XYChart.Series<>();
            seriesMiddlePointAlgorithmEquation.setName("Средней точки");

            XYChart.Series<Number, Number> seriesLibraryEquation = new XYChart.Series<>();
            seriesLibraryEquation.setName("Библиотечная");

            long canonicalEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
                    tempLeftRadious, tempTopRadious,
                    tempColor,
                    "Каноническое уравнение", false, graphTable);
            long parametricalEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
                    tempLeftRadious, tempTopRadious,
                    tempColor,
                    "Параметрическое уравнение", false, graphTable);
            long brezenchemsEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
                    tempLeftRadious, tempTopRadious,
                    tempColor,
                    "Алгоритм Брезенхема", false, graphTable);
            long middlePointAlgorithmEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
                    tempLeftRadious, tempTopRadious,
                    tempColor,
                    "Алгоритм средней точки", false, graphTable);
            long libraryEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
                    tempLeftRadious, tempTopRadious,
                    tempColor,
                    "Построение при помощи библиотечной функции", false, graphTable);


            for (; tempLeftRadious < 6000; tempLeftRadious += 1000, tempTopRadious += 1000) {
                // Вычисление времени работы
                long startTime1 = System.nanoTime();
                new CanonicalEquation(tempCenterX, tempCenterY, tempLeftRadious, tempLeftRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
                long endTime1 = System.nanoTime();

                long startTime2 = System.nanoTime();
                new ParametricEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
                long endTime2 = System.nanoTime();

                long startTime3 = System.nanoTime();
                new BrezenchemsEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
                long endTime3 = System.nanoTime();

                long startTime4 = System.nanoTime();
                new MiddlePointAlgorithmEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
                long endTime4 = System.nanoTime();

                long startTime5 = System.nanoTime();
                new LibraryEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
                long endTime5 = System.nanoTime();
                System.out.println();

                seriesCanonicalEquation.getData().add(new XYChart.Data<>(tempLeftRadious, (endTime1 - startTime1) * 1.15));
                seriesParametricalEquation.getData().add(new XYChart.Data<>(tempLeftRadious, (endTime2 - startTime2) * 1.15));
                seriesBrezenchemsEquation.getData().add(new XYChart.Data<>(tempLeftRadious, (endTime3 - startTime3) * 1.15));
                seriesMiddlePointAlgorithmEquation.getData().add(new XYChart.Data<>(tempLeftRadious, (endTime4 - startTime4) * 1.15));
                seriesLibraryEquation.getData().add(new XYChart.Data<>(tempLeftRadious, (endTime5 - startTime5) * 1.15));
            }

            linechart.getData().add(seriesCanonicalEquation);
            linechart.getData().add(seriesParametricalEquation);
            linechart.getData().add(seriesBrezenchemsEquation);
            linechart.getData().add(seriesMiddlePointAlgorithmEquation);
            linechart.getData().add(seriesLibraryEquation);

            //Creating a Group object
            Group root = new Group(linechart);

            //Creating a scene object
            Scene scene = new Scene(root, 600, 400);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            //Setting title to the Stage
            alert.setTitle("Line Chart");

            VBox vBox = new VBox();
            vBox.getChildren().addAll(linechart);

            //Adding scene to the stage
            alert.setGraphic(vBox);

            alert.showAndWait();

            //Displaying the contents of the stage
//            alert.show();

            // Показ диаграммы на экран
//            onShowHistogramStats(canonicalEquation, parametricalEquation, brezenchemsEquation,
//                    middlePointAlgorithmEquation, libraryEquation);
        });

        timeOvalButton.setOnAction(actionEvent -> {

            int tempLeftRadious = 50000, tempTopRadious = 50000;
            Color tempColor = Color.RED;

            int tempCenterX = 100, tempCenterY = 100;

            // Вычисление времени работы
//            long canonicalEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
//                    tempLeftRadious, tempTopRadious,
//                    tempColor,
//                    "Каноническое уравнение", true, graphTable);
//            long parametricalEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
//                    tempLeftRadious, tempTopRadious,
//                    tempColor,
//                    "Параметрическое уравнение", true, graphTable);
//            long brezenchemsEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
//                    tempLeftRadious, tempTopRadious,
//                    tempColor,
//                    "Алгоритм Брезенхема", true, graphTable);
//            long middlePointAlgorithmEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
//                    tempLeftRadious, tempTopRadious,
//                    tempColor,
//                    "Алгоритм средней точки", true, graphTable);
//            long libraryEquation = getTimeOfWorkingFunctionDrawingLine(tempCenterX, tempCenterY,
//                    tempLeftRadious, tempTopRadious,
//                    tempColor,
//                    "Построение при помощи библиотечной функции", true, graphTable);

            long startTime1 = System.nanoTime();
            new CanonicalEquation(tempCenterX, tempCenterY, tempLeftRadious, tempLeftRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
            long endTime1 = System.nanoTime();

            long startTime2 = System.nanoTime();
            new ParametricEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
            long endTime2 = System.nanoTime();

            long startTime3 = System.nanoTime();
            new BrezenchemsEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
            long endTime3 = System.nanoTime();

            long startTime4 = System.nanoTime();
            new MiddlePointAlgorithmEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
            long endTime4 = System.nanoTime();

            long startTime5 = System.nanoTime();
            new LibraryEquation(tempCenterX, tempCenterY, tempLeftRadious, tempTopRadious, false, tempColor).draw(graphTable.getGraphicsContext2D());
            long endTime5 = System.nanoTime();

            graphTable.getGraphicsContext2D().clearRect(0, 0, graphTable.getWidth(), graphTable.getHeight());

            // Показ диаграммы на экран
            onShowHistogramStats(endTime1 - startTime1, endTime2 - startTime2, endTime3 - startTime3,
                    endTime4 - startTime4, endTime5 - startTime5);
        });
    }

    private double[] rotateLine(double startCoordinateX, double startCoordinateY, double endCoordinateX, double endCoordinateY, double degreeOfRotation, double spectrRadious)
    {

        // Высисление поворота координат
        double rotatedEndCoordinateX = startCoordinateX + (((endCoordinateX - startCoordinateX) * cos(degreeOfRotation) - (endCoordinateY - startCoordinateY) * sin(degreeOfRotation)));
        double rotatedEndCoordinateY = startCoordinateY + (((endCoordinateX - startCoordinateX) * sin(degreeOfRotation)) + (endCoordinateY - startCoordinateY) * cos(degreeOfRotation));

        return new double[] {rotatedEndCoordinateX, rotatedEndCoordinateY};
    }

    @FXML
    private void onSpectr()
    {
        System.out.println("Тут");
        try {
            spectrCircleButton.setOnAction(actionEvent -> {
                for (int tempNumberOfRadious = 1; tempNumberOfRadious <= Integer.parseInt(numberOfSpectrCircle.getText()); tempNumberOfRadious++)
                {
                    System.out.println("Рисует");
                    graphProcessing(objectList, Integer.parseInt(circleCenterX.getText()), Integer.parseInt(circleCenterY.getText()),
                            tempNumberOfRadious * Integer.parseInt(radiousDeltaSpectrCircle.getText()) + Integer.parseInt(circleRadious.getText()),
                            tempNumberOfRadious * Integer.parseInt(radiousDeltaSpectrCircle.getText()) + Integer.parseInt(circleRadious.getText()),
                            CanvasOperations.getColor(colorCircleType.getValue().toString()),
                            algCircleType.getValue().toString(), false);
                    redrawElements(graphTable, objectList);
                }
            });

            spectrOvalButton.setOnAction(actionEvent -> {
                for (int tempNumberOfRadious = 1; tempNumberOfRadious <= Integer.parseInt(numberOfSpectrOval.getText()); tempNumberOfRadious++)
                {
                    double newTempLeftRadious = tempNumberOfRadious * Integer.parseInt(radiousDeltaSpectrOval.getText()) + Integer.parseInt(ovalLeftRadious.getText());
                    double newTempTopRadious = (Integer.parseInt(ovalLeftRadious.getText()) * newTempLeftRadious) / Integer.parseInt(ovalTopRadious.getText());

                    graphProcessing(objectList,
                            Integer.parseInt(ovalCenterX.getText()), Integer.parseInt(ovalCenterY.getText()),
                            newTempLeftRadious, newTempTopRadious,
                            CanvasOperations.getColor(colorOvalType.getValue().toString()),
                            algOvalType.getValue().toString(), true);

                    redrawElements(graphTable, objectList);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            parameterErrorField.setText("Допущена ошибка.");
        }
    }
}
