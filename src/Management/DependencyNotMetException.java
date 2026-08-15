package Management;

public class DependencyNotMetException extends RuntimeException {

	public DependencyNotMetException(String msg) {
		super(msg);
	}
}
