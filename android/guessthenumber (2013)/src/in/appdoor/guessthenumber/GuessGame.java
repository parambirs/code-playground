package in.appdoor.guessthenumber;

public class GuessGame {
	
	private int targetNumber;
	
	private static GuessGame GAME = new GuessGame();
	
	private GuessGame() {
		newGame();
	}
	
	public static GuessGame getInstance() {
		return GAME;
	}
	
	public GuessAnswer checkGuess(int guess) {
		if(guess < targetNumber) {
			return GuessAnswer.TOO_LOW;
		} else if(guess > targetNumber) {
			return GuessAnswer.TOO_HIGH;
		} else {
			return GuessAnswer.CORRECT;
		}
	}
	
	public void newGame() {
		targetNumber = (int)(Math.random() * 100 + 1);
	}
}
