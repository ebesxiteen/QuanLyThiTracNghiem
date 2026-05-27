package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import model.*;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.control.CheckBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.scene.Node;
import components.*;
import utils.Notification;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class ClientExamController implements Initializable {
	@FXML
	private AnchorPane answerPane;
	@FXML
	private AnchorPane mainPane;
	@FXML
	private VBox QuestionsVbox;
	@FXML
	private VBox answerVbox;
	@FXML
	private Label tfQuestion;
	@FXML
	private Label subjName;
	@FXML
	private Label examName;
	@FXML
	private Label lbScore;
	@FXML
	private Label lb_InfEx;
	@FXML
	private Button btnExport;
	@FXML
	private Button btnSubmit;
	@FXML
	private Label lbClock;
	
//import data 
	
    public static HostExam hostExam = Connect_Server.client.getHostExam();

    public static ArrayList<Question> listQuest = hostExam.getExamQuestions();
    public static ArrayList<Question> listQuestTemp=new ArrayList<Question>();
    
    ArrayList<Answer_Select> answer_selects = new ArrayList<Answer_Select>();
    Date start;
    
	private int oldId;
	private int correctCount;
	Submission submission;
	private Timeline timeline;
	private long timeSeconds=hostExam.getTimeLimit()*60;
	private long timeTemp;
	
	//LoadExamGui
	public void initExam(ArrayList<Question> listQuest) {
		try {
			start = new Date(System.currentTimeMillis());
			btnSubmit.setVisible(true);
			subjName.setVisible(true);
			examName.setVisible(true);
			lbScore.setVisible(false);
			lb_InfEx.setVisible(false);
			lbClock.setVisible(true);
			btnExport.setVisible(false);
			btnExport.setDisable(true);
			//Hiển thị bảng điều khiển câu hỏi
			QuestionsVbox.setSpacing(5);
			for (int i=0;i<listQuest.size();i++) { 
				ButtonQuestion button = new ButtonQuestion(); 
				button.setText(String.valueOf(i+1));
				button.setId("btnQuestion"+i); 
				button.getStyleClass().add("button-xanhnhat");
				button.setOnAction(this::btnQuestionClicked);
				if(i%2==0) {
					HBox hbox = new HBox();
					hbox.setId("hboxQuestion"+((i/4)+1));
					hbox.setSpacing(10);
					VBox.setMargin(hbox, new Insets(0, 10, 0, 10));
					QuestionsVbox.getChildren().add(hbox);
				}
				HBox root= (HBox) QuestionsVbox.lookup("#hboxQuestion"+((i/4)+1));
				root.getChildren().add(button);
				//Load cau hoi
				
			}
			tfQuestion.setText("Câu 1: "+listQuest.get(0).getContent());
			ButtonQuestion button = (ButtonQuestion) QuestionsVbox.lookup("#btnQuestion"+0);
			setOldId(0);
			button.setStyle("-fx-background-color: #ffffff;-fx-text-fill: #154FEF;-fx-border-color: #154FEF;");
			int i=0;
			for (Question quest:listQuest) {
				load_Scene_VBox(mainPane,quest);
				VBox vbox = (VBox)answerPane.lookup("#newVbox");
				vbox.setId("answerVbox"+i);
				System.out.println(vbox.getId());
				i++;
			}
			VBox vbox = (VBox)answerPane.lookup("#answerVbox"+0);
			vbox.setVisible(true);
		}
		catch(Exception e){
			Notification.Error("Error","Cannot load exam!");
		}
		
	}
	
	//load QuestionContent
	public void load_Scene_VBox(AnchorPane pane, Question question) {
        VBox vbox = new VBox();
        vbox.setId("newVbox");
        vbox.setSpacing(15);
            
            AnchorPane anchor = (AnchorPane) pane.lookup("#answerPane");
            anchor.getChildren().add(vbox);
            
            
            for(int i=0;i<question.getAnswers().size();i++) {
            	Answer answer = question.getAnswers().get(i);
            	hboxAnswer hbox = new hboxAnswer();
            	hbox.getHBox().setId("hbox"+i);
            	hbox.getCheckBox().setId(String.valueOf(answer.getAnswerId()));
            	hbox.getLabel().setText(answer.getContent());
            	hbox.getLabel().setId("content"+String.valueOf(answer.getAnswerId()));
            	vbox.getChildren().add(hbox.getHBox());
            	Boolean isCorrect = answer.isCorrect();
                answer_selects.add(new Answer_Select(answer, isCorrect));
            }
            vbox.setVisible(false);
            	setAnswer(vbox);
    }
	
    public void setAnswer(VBox vboxAnswer) {
        AnchorPane.setTopAnchor(vboxAnswer, 0.0);
        AnchorPane.setBottomAnchor(vboxAnswer, 0.0);
        AnchorPane.setRightAnchor(vboxAnswer, 0.0);
        AnchorPane.setLeftAnchor(vboxAnswer, 0.0);
    }
    //Dong ho dem gio
    private void startCountdown() { 
    	if (timeline != null) { 
    		timeline.stop(); 
    	} 
    	timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> { 
    		timeTemp--; 
    		if(timeTemp/60<10) {
    			if(timeTemp%60<10) {
    				lbClock.setText("0"+timeTemp/60+":0"+timeTemp%60+"s");
    			}
    			else lbClock.setText("0"+timeTemp/60+":"+timeTemp%60+"s");
    		}
    		else if(timeTemp%60<10) {
				lbClock.setText(timeTemp/60+":0"+timeTemp%60+"s");
			}
			else lbClock.setText(timeTemp/60+":"+timeTemp%60+"s");
    		
    		if (timeTemp <= 0) { 
    			timeline.stop(); 
    			submitExam();
    			
    		} })); 
    	timeline.setCycleCount(Timeline.INDEFINITE); 
    	timeline.play(); 
    	}
    
    //Chuyen cau hoi
    public void btnQuestionClicked(ActionEvent event) {
		ButtonQuestion button = (ButtonQuestion) event.getSource();
		ButtonQuestion old = (ButtonQuestion) ((HBox)QuestionsVbox.lookup("#hboxQuestion"+(getOldId()/4+1))).lookup("#btnQuestion"+getOldId());
		old.setStyle("");
		button.setStyle("-fx-background-color: #ffffff;-fx-text-fill: #154FEF;-fx-border-color: #154FEF;");
		int number=Integer.parseInt(button.getText());
		tfQuestion.setText("Câu " + number + ": " + listQuest.get(number-1).getContent());
		VBox vbox = (VBox) answerPane.lookup("#answerVbox" + (number - 1)); 
		if (vbox != null) { 
			vbox.setVisible(true); 
		} 
		else { 
		System.out.println("VBox không tồn tại: #answerVbox" + (number - 1)); 
		}
		vbox = (VBox) answerPane.lookup("#answerVbox" + getOldId()); 
		if (vbox != null) { 
			vbox.setVisible(false); 
		} 
		else { 
		System.out.println("VBox không tồn tại: #answerVbox" + getOldId()); 
		}
		setOldId(number-1);
	}
    
    //Chuyen cau hoi sau khi nop bai
	public void btnQuestionClickedSubmit(ActionEvent event) {
		ButtonQuestion button = (ButtonQuestion) event.getSource();
		ButtonQuestion old = (ButtonQuestion) ((HBox)QuestionsVbox.lookup("#hboxQuestion"+(getOldId()/4+1))).lookup("#btnQuestion"+getOldId());
		old.setStyle("");
		int number=Integer.parseInt(button.getText());
		tfQuestion.setText("Câu " + number + ": " + listQuest.get(number-1).getContent());
		if(!button.isCorrect()) {
			button.setStyle("-fx-background-color:  #D92D20;-fx-text-fill:  #ffffff;-fx-border-color: #D92D20;");
			tfQuestion.setStyle("-fx-background-color:  #D92D20;-fx-text-fill:  #ffffff;-fx-border-color: #D92D20;");
		}
		else {
			button.setStyle("-fx-background-color:  #099250;-fx-text-fill:  #ffffff;-fx-border-color: #099250;");
			tfQuestion.setStyle("-fx-background-color:  #099250;-fx-text-fill:  #ffffff;-fx-border-color: #099250;");
		}
		VBox vbox = (VBox) answerPane.lookup("#answerVbox" + (number - 1)); 
		if (vbox != null) { 
			vbox.setVisible(true); 
		} 
		else { 
		System.out.println("VBox không tồn tại: #answerVbox" + (number - 1)); 
		}
		vbox = (VBox) answerPane.lookup("#answerVbox" + getOldId()); 
		if (vbox != null) { 
			vbox.setVisible(false); 
		} 
		else { 
		System.out.println("VBox không tồn tại: #answerVbox" + getOldId()); 
		}
		setOldId(number-1);
	}
	public void btnExportClicked(ActionEvent event) {
		Platform.exit();
	}
	
	
	// Event Listener on Button[#btnSubmit].onAction
	@FXML
	public void btnSubmitClicked(ActionEvent event) {
		submitExam();
	}
	
	public void submitExam() {
		btnSubmit.setVisible(false);
		subjName.setVisible(false);
		examName.setVisible(false);
		lbScore.setVisible(true);
		lb_InfEx.setVisible(true);
		lbClock.setVisible(false);
		btnExport.setVisible(true);
		btnExport.setDisable(false);
		
		checkBoxesHandler();
		for(int i=0;i<listQuest.size();i++) {
			Question quest=listQuest.get(i);
			VBox vbox=(VBox) answerPane.lookup("#answerVbox"+i);
			HBox hbox=(HBox)QuestionsVbox.lookup("#hboxQuestion"+((i/4)+1));
			ButtonQuestion btn= (ButtonQuestion) hbox.lookup("#btnQuestion"+i);
			btn.setCorrect(answerHandler(quest,vbox));
			btn.setOnAction(this::btnQuestionClickedSubmit);
			if(btn.isCorrect()==false) {
				btn.getStyleClass().remove("button-xanhnhat");
				btn.getStyleClass().add("button-wrong");
				if(i==getOldId()){
					tfQuestion.setStyle("-fx-background-color:  #D92D20;-fx-text-fill:  #ffffff;-fx-border-color: #D92D20;");
					btn.setStyle("-fx-background-color:  #D92D20;-fx-text-fill:  #ffffff;-fx-border-color: #D92D20;");
				}
			}
			else {
				btn.getStyleClass().remove("button-xanhnhat");
				btn.getStyleClass().add("button-correct");
				if(i==getOldId()){
					tfQuestion.setStyle("-fx-background-color:  #099250;-fx-text-fill:  #ffffff;-fx-border-color: #099250;");
					btn.setStyle("-fx-background-color:  #099250;-fx-text-fill:  #ffffff;-fx-border-color: #099250;");
				}
			}
			listQuestTemp.add(getAnswerSelectedList(quest,vbox));
		}

		double diem=Connect_Server.client.submit(listQuestTemp, answer_selects,(timeSeconds-timeTemp));
		LocalDateTime currentDateTime = LocalDateTime.now(); 
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
		String formattedDateTime = currentDateTime.format(formatter);
		lbScore.setText("YOUR SCORE: "+diem);
		lb_InfEx.setText("Duration: "+(timeSeconds-timeTemp)/60+"m"+(timeSeconds-timeTemp)%60+"s - Submit at "+formattedDateTime);
	}
	
	
	public void checkBoxesHandler(){
	// Chọn tất cả các CheckBox và tắt khả năng click 
		
		for (javafx.scene.Node node : answerPane.getChildren()) { 
			VBox vbox = (VBox) node; 
			for (javafx.scene.Node node1 : vbox.getChildren()) { 
				HBox hbox = (HBox) node1;
				for (javafx.scene.Node node2 : hbox.getChildren()) { 
					if (node2 instanceof CheckBox) { 
						CheckBox checkBox = (CheckBox) node2; 
						checkBox.setDisable(true); 
					} 
				} 
			} 
		} 
	}
	
	//Xu li cac cau tra loi
	public boolean answerHandler(Question quest,VBox vbox) {
		for(int i=0;i<quest.getAnswers().size();i++) {
			Answer ans=quest.getAnswers().get(i);
			HBox hbox = (HBox) vbox.lookup("#hbox"+i);
			Label text = (Label)hbox.lookup("#content"+ans.getAnswerId());
			CheckBox cbox = (CheckBox)hbox.lookup("#"+ans.getAnswerId());
			if(ans.isCorrect()==true) {
				text.setStyle("-fx-background-color: #E1FBEA;-fx-text-fill: #099250;-fx-border-color: #099250;");
			}
		}
		for(int i=0;i<quest.getAnswers().size();i++) {
			Answer ans=quest.getAnswers().get(i);
			HBox hbox = (HBox) vbox.lookup("#hbox"+i);
			CheckBox cbox = (CheckBox)hbox.lookup("#"+ans.getAnswerId());
			if(ans.isCorrect()!=cbox.isSelected()) {
				return false;
			}
		}
		return true;
	}
	Question getAnswerSelectedList(Question quest, VBox vbox) {
		for(int i=0;i<quest.getAnswers().size();i++) {
			HBox hbox = (HBox) vbox.lookup("#hbox"+i);
			getNewAnswer(quest.getAnswers().get(i),hbox);
		}
		Question questTemp=quest;
		return questTemp;
	}
	void getNewAnswer(Answer ans,HBox hbox) {
		CheckBox cbox = (CheckBox)hbox.lookup("#"+ans.getAnswerId());	
		ans.setCorrect(cbox.isSelected());
	}
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
//		answerList1.add(answer1);
//		answerList1.add(answer2);
//		answerList1.add(answer3);
//		answerList1.add(answer4);
//		answerList1.add(answer9);
//		answerList2.add(answer5);
//		answerList2.add(answer6);
//		answerList2.add(answer7);
//		answerList2.add(answer8);
//		listQuest.add(q1);
//		listQuest.add(q2);
//		listQuest.add(q3);
//		listQuest.add(q4);
//		listQuest.add(q5);
		startCountdown();
		initExam(listQuest);
		timeTemp=timeSeconds;
		
    }
	public int getCorrectCount() {
		return correctCount;
	}
	public void setCorrectCount(int correctCount) {
		this.correctCount = correctCount;
	}
	public Submission getSubmission() {
		return submission;
	}
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}
	public int getOldId() { 
		return oldId; 
	} 
	public void setOldId(int oldId) 
	{ 
		this.oldId = oldId; 
	}

}
