package massim.competition2015.scenario.exceptions;

public class WrongParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WrongParameterException() {
		super();
	}

	public WrongParameterException(String message) {
		super(message);
	}

	public WrongParameterException(Throwable cause) {
		super(cause);
	}

	public WrongParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongParameterException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
