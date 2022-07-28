import java.util.Timer;
import java.util.TimerTask;

public class Launcher {

	public static void main(String[] args) {
		int delay = 3_000;
		runLater(() -> printActiveApplication("second run"), delay);

		printActiveApplication("first run");
	}

	private static void printActiveApplication(String textBefore) {
		System.out.println(textBefore + " " + new Processes().getOpenedApplicationNames());
	}

	private static void runLater(final Runnable runnable, int delay) {
		Timer timer = new Timer();
		timer.schedule(getRunnableTask(runnable), delay);
		timer.schedule(getRunnableTask(() -> timer.cancel()), delay*2);
	}

	private static TimerTask getRunnableTask(final Runnable runnable) {
		return new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}
}
