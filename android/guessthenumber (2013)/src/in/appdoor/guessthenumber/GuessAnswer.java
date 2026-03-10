package in.appdoor.guessthenumber;

public enum GuessAnswer {
		TOO_LOW("Too Low!!!"), TOO_HIGH("Too High!"), CORRECT("Correct!!!");
		
		private String message;
		
		private GuessAnswer(String message) {
			this.message = message;
		}
		
		public String getMessage() {
			return this.message;
		}
	}