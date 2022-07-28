import java.util.Timer;
import java.util.TimerTask;

public class Launcher {

	private static final int DELAY = 3_000;

	public static void main(String[] args) {
		TimerTask task = getTask(() -> printApplication("second"));
		new Timer().schedule(task, DELAY);

		printApplication("first");
	}

	private static TimerTask getTask(final Runnable runnable) {
		return new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}

	private static void printApplication(String textBefore) {
		System.out.println(textBefore +"\n" + new Processes().getOpenedApplicationNames());
	}
}
