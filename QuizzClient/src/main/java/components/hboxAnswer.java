package components;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class hboxAnswer {
	
    private HBox hBox = new HBox();
    private CheckBox checkBox = new CheckBox();
    private Label label = new Label();

    public hboxAnswer() {
        hBox.getChildren().addAll(checkBox, label);
        setUp();
    }

    void setUp() {
        // hBox
        hBox.setSpacing(20);
        hBox.setPrefHeight(38);
        hBox.setPrefWidth(424);
        hBox.setScaleX(1);
        hBox.setScaleY(1);
        hBox.setScaleZ(1);

        // Label
        String styleLabel = "-fx-font-size: 14px; -fx-font-family:System; ";
        HBox.setMargin(getLabel(), new Insets(0, 0, 0, 10));
        label.setPadding(new Insets(0,5,0,5));
        label.getStyleClass().add("round-layout");
        label.setStyle(styleLabel);
        label.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        label.setFocusTraversable(true);
        label.setCacheShape(true);
        label.setCenterShape(true);
        label.setScaleShape(true);
        label.setBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
        label.setDepthTest(javafx.scene.DepthTest.INHERIT);
        label.setPickOnBounds(true);
        label.setMinHeight(37);
        label.setPrefWidth(382);
        label.setPrefHeight(37);
        label.setLayoutX(0);
        label.setLayoutY(0);
        label.setScaleX(1);
        label.setScaleY(1);
        label.setScaleZ(1);
        // CheckBox
        String styleCheckBox = "-fx-font-size: 14px; -fx-font-family:System; ";
        HBox.setMargin(getCheckBox(), new Insets(7, 0, 0, 5));
        checkBox.setStyle(styleCheckBox);
        checkBox.setPadding(new Insets(10,0,0,0));
        checkBox.setFocusTraversable(true);
        checkBox.setCacheShape(true);
        checkBox.setCenterShape(true);
        checkBox.setScaleShape(true);
        checkBox.setBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
        checkBox.setDepthTest(javafx.scene.DepthTest.INHERIT);
        checkBox.setPickOnBounds(true);
        checkBox.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        checkBox.setGraphicTextGap(4);
        checkBox.setAlignment(javafx.geometry.Pos.CENTER);
        checkBox.setMinHeight(11);
        checkBox.setPrefWidth(13);
        checkBox.setPrefHeight(11);
        checkBox.setLayoutX(0);
        checkBox.setLayoutY(0);
        checkBox.setScaleX(1);
        checkBox.setScaleY(1);
        checkBox.setScaleZ(1);
        checkBox.setSelected(false);
    }

    public HBox getHBox() {
        return hBox;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public Label getLabel() {
        return label;
    }

 
}
