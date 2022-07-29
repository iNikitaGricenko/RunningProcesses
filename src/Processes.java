import com.sun.jna.Platform;
import com.sun.security.auth.module.UnixSystem;
import lombok.SneakyThrows;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



import static java.lang.String.format;

public class Processes {

	@SneakyThrows
	public Application getOpenedApplicationNames() {
		String systemOS = System.getProperty("os.name");

		if ("Linux".equals(systemOS)) {
			return linuxRunningProcesses();
		} else if ("Windows".equals(systemOS)) {
			return getWindowsActiveWindowInfo();
		} else if ("Mac".equals(systemOS)) {
			macOsRunningProcesses();
		}

		throw new RuntimeException(format("OS %s is unsupported", systemOS));
	}

	private static void macOsRunningProcesses() throws ScriptException {
		final String script="tell application \"System Events\"\n" +
				"\tname of application processes whose frontmost is tru\n" +
				"end";
		ScriptEngine appleScript = new ScriptEngineManager().getEngineByName("AppleScript");
		String result=(String)appleScript.eval(script);
		System.out.println(result);
	}

	private Application linuxRunningProcesses() throws IOException {
		String xid = getXid();
		String name = getApplicationName(xid);
		String title = getApplicationTitle(xid);
		return new Application(xid, name, title);
	}

	private Application getWindowsActiveWindowInfo() throws IOException {
		getBufferedReader("get-process | ? { $_.mainwindowhandle -eq $a }").readLine();

		//		https://stackoverflow.com/questions/1354254/get-current-active-windows-title-in-java

		throw new RuntimeException("get Running Processes on windows is currently unsupported");
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
		Process process = Runtime.getRuntime().exec(command);
		return new BufferedReader(
				new InputStreamReader(process.getInputStream()));
	}
}
