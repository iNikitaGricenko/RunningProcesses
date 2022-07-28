import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.String.format;

public class Processes {

	public Application getOpenedApplicationNames() {
		try {
			String systemOS = System.getProperty("os.name");

			if ("Linux".equals(systemOS)) {
				return linuxRunningProcesses();
			}
			throw new RuntimeException(format("OS %s is unsupported", systemOS));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Application linuxRunningProcesses() throws IOException {
//		timeSkip();

		String xid = getXid();
		String name = getApplicationName(xid);
		String title = getApplicationTitle(xid);
		return new Application(xid, name, title);
	}

	private String getXid() throws IOException {
		return getBufferedReader("xprop -root _NET_ACTIVE_WINDOW")
				.readLine()
				.split(" ")[4];
	}

	private String getApplicationName(String xid) throws IOException {
		return getBufferedReader(format("xprop -id %s WM_CLASS", xid))
				.readLine()
				.split(" = ")[1]
				.split(",")[1];
	}

	private String getApplicationTitle(String xid) throws IOException {
		return getBufferedReader(format("xprop -id %s _NET_WM_NAME", xid))
				.readLine()
				.split(" = ")[1];
	}

	private BufferedReader getBufferedReader(String command) throws IOException {
		Process process;
		process = Runtime.getRuntime().exec(command);
		return new BufferedReader(
				new InputStreamReader(process.getInputStream()));
	}

	private static void timeSkip() {
		for (double i = 0; i < 1_999_999_999d; i++) {}
	}
}
