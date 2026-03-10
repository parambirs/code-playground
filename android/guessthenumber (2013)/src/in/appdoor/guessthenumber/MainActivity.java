package in.appdoor.guessthenumber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity implements OnKeyboardActionListener {

	private GuessGame game;
	private TextView label;
	private TextView status;
	private int lastGuess = -1;
	private int guessCount = 0;

	private final static int CODE_DELETE = -5; // Keyboard.KEYCODE_DELETE
	private final static int CODE_RETURN = 13; // Keyboard.KEYCODE_RETURN

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		game = GuessGame.getInstance();
		label = (TextView) findViewById(R.id.message);
		status = (TextView) findViewById(R.id.status);
		new CustomKeyboard(this, R.id.keyboardview, R.xml.numkbd, this);

		showNewGameMessage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void showNewGameMessage() {
		clearStatus();
		showMessage("Guess???");
	}

	public void checkGuess() {
		if (isNumeric(label.getText().toString())) {
			guessCount++;			
			int guess = Integer.parseInt(label.getText().toString());			
			lastGuess = guess;
			GuessAnswer answer = game.checkGuess(guess);
			showGuessResult(answer);
			
		}
	}

	private void showMessage(String msg) {
		label.setText(msg);
	}
	
	private void showStatus(){
		String msg = "Last Guess: " + lastGuess + ", Total Guesses: " + guessCount;
		status.setText(msg);
	}
	
	private void clearStatus(){
		guessCount =  0;
		status.setText("");
	}

	private void showGuessResult(GuessAnswer answer) {
		showMessage(answer.getMessage());
		showStatus();
		if (answer == GuessAnswer.CORRECT) {
			new AlertDialog.Builder(this)
					.setMessage("Correct! You took " + guessCount + " guesses")
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									game.newGame();
									showNewGameMessage();
								}
							}).show();
		} 
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// NOTE We can say '<Key android:codes="49,50" ... >' in the xml file;
		// all codes come in keyCodes, the first in this list in primaryCode

		String text = getLabelText();
		String newChar = Character.toString((char) primaryCode);

		if (primaryCode == CODE_DELETE) {
			if (text.length() > 0 && isNumeric(text)) {
				setLabelText(text.substring(0, text.length() - 1));
			}
		} else if (primaryCode == CODE_RETURN) {
			checkGuess();
		} else { // insert character
			if (isNumeric(text) && text.length() < 2) {
				setLabelText(text + newChar);
			} else if (!isNumeric(text)) {// some message is being displayed...
											// clear it...
				setLabelText(newChar);
			}
		}
	}

	private boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	private String getLabelText() {
		return label.getText() != null ? label.getText().toString() : "";
	}

	private void setLabelText(String s) {
		label.setText(s);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub.
		switch (item.getItemId()) {
		case R.id.action_how_to_play:
			new AlertDialog.Builder(this)
			.setTitle("How To Play")
			.setMessage("Guess a number between 1 and 100")
			.setPositiveButton(R.string.ok, null).show();
			return true;		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPress(int primaryCode) {

	}

	@Override
	public void onRelease(int primaryCode) {

	}

	@Override
	public void onText(CharSequence text) {

	}

	@Override
	public void swipeDown() {

	}

	@Override
	public void swipeLeft() {

	}

	@Override
	public void swipeRight() {

	}

	@Override
	public void swipeUp() {

	}

}
