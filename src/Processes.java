import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;
import lombok.SneakyThrows;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.sun.jna.platform.win32.WinDef.HWND;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;

public class Processes {

	private static final int SIZE = 1024;

	@SneakyThrows
	public Application getOpenedApplicationNames() {
		String systemOS = System.getProperty("os.name").toLowerCase();

		if (systemOS.contains("linux")) {
			return linuxRunningProcesses();
		} else if (systemOS.contains("win")) {
			return getWindowsActiveWindowInfo();
		} else if (systemOS.contains("mac")) {
			macOsRunningProcesses();
		}

		throw new RuntimeException(format("OS %s is unsupported", systemOS));
	}

	private static void macOsRunningProcesses() throws ScriptException {
		final String script="global frontApp, frontAppName, windowTitle\n" +
				"\n" +
				"tell application \"System Events\"\n" +
				"\tset frontApp to first application process whose frontmost is true\n" +
				"\tset frontAppName to name of frontApp\n" +
				"\ttell process frontAppName\n" +
				"\t\ttell (1st window whose value of attribute \"AXMain\" is true)\n" +
				"\t\t\tset windowTitle to value of attribute \"AXTitle\"\n" +
				"\t\tend tell\n" +
				"\tend tell\n" +
				"end tell\n" +
				"\n" +
				"return {frontAppName, windowTitle}";

		ScriptEngine appleScript = new ScriptEngineManager().getEngineByName("AppleScript");
		String result=(String)appleScript.eval(script);
		System.out.println(result);
	}

	private Application linuxRunningProcesses() throws IOException {
		String xid = getXid();
		List<String> nameAndTitle = getApplicationNameAndTitle(xid, line -> line.split(" = ")[1]);
		String name = nameAndTitle.get(0);
		String title = nameAndTitle.get(1);

		return new Application(xid, name, title);
	}

	private static final int BUFFER_SIZE = 1024;

	private static User32 getUser32() {
		return User32.INSTANCE;
	}

	private static Kernel32 getKernel32() {
		return Kernel32.INSTANCE;
	}

	public Application getWindowsActiveWindowInfo() {
		String pid = getWindowsAppPid(); // returns 12032 (just pid)

		String appProcess = executeWindCommand((buffer, hwnd) ->
				getUser32().GetWindowThreadProcessId(hwnd, new IntByReference())); // returns app name in example C:\Program Files\JetBrains\IntelliJ IDEA Ultimate\bin\idea64.exe

		String appTitle = executeWindCommand((buffer, hwnd) ->
				getUser32().GetWindowModuleFileName(hwnd, buffer, BUFFER_SIZE)); // returns app title in example RunningProcesses – Processes.java

		return new Application(pid, appProcess, appTitle);
	}

    private String getWindowsAppPid() {
		HWND hwnd = getUser32().GetForegroundWindow();
		int processId = getUser32().GetWindowThreadProcessId(hwnd, new IntByReference());
		return String.valueOf(processId);
	}

	private String executeWindCommand(final BiFunction<char[], HWND, Integer> biFunction) {
		char[] buffer = new char[BUFFER_SIZE * 2];
		HWND hwnd = getUser32().GetForegroundWindow();
		biFunction.apply(buffer, hwnd);
		return Native.toString(buffer);
	}

	private String getXid() throws IOException {
		return getBufferedReader("xprop -root _NET_ACTIVE_WINDOW")
				.readLine()
				.split(" ")[4];
	}

	private List<String> getApplicationNameAndTitle(String xid, final Function<String, String> function) throws IOException {
		Function<String, String> defaultFunction = line1 -> line1.replaceAll("\"", "");
		return getBufferedReader(format("xprop -id %s _NET_WM_NAME WM_CLASS", xid))
				.lines()
				.map(function)
				.map(defaultFunction)
				.collect(toList());
	}

	private BufferedReader getBufferedReader(String command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		return new BufferedReader(
				new InputStreamReader(process.getInputStream()));
	}
}
