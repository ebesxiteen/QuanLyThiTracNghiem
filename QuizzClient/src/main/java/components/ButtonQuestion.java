package components;

import javafx.scene.control.Button;

public class ButtonQuestion extends Button {
	private boolean isCorrect;
	public ButtonQuestion() {
	}
	public boolean isCorrect() {
		return isCorrect;
	}
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

}
