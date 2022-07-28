import java.util.Timer;
import java.util.TimerTask;

public class Launcher {
	public static void main(String[] args) {
		int delay = 3_000;
		new Timer().schedule(getTask(), delay);

	}

	private static TimerTask getTask() {
		return new TimerTask() {
			@Override
			public void run() {
				System.out.println(
						new Processes().getOpenedApplicationNames());
			}
		};
	}
}
