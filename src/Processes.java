import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import lombok.SneakyThrows;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinNT.PROCESS_QUERY_INFORMATION;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_READ;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Processes {

	private static final int BUFFER_SIZE = 1024;

	private static User32 getUser32() {
		return User32.INSTANCE;
	}

	private static Kernel32 getKernel32() {
		return Kernel32.INSTANCE;
	}

	private static Psapi getPsapi() {
		return Psapi.INSTANCE;
	}

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
		List<String> nameAndTitle = getApplicationNameAndTitle(line -> line.split(" = ")[1]);
		String title = nameAndTitle.get(0);
		String name = nameAndTitle.get(1);

		return new Application(name, title);
	}

	public Application getWindowsActiveWindowInfo() {
		String appProcess = executeWindCommand((buffer, hwnd) -> {
			WinNT.HANDLE handle = getKernelHandle();
			return getPsapi().GetModuleFileNameExW(handle, null, buffer, BUFFER_SIZE);
		}); // returns app name like path, in example C:\Program Files\JetBrains\IntelliJ IDEA Ultimate\bin\idea64.exe

		String appTitle = executeWindCommand((buffer, hwnd) ->
				getUser32().GetWindowText(hwnd, buffer, BUFFER_SIZE)); // returns app title in example RunningProcesses – Processes.java

		return new Application(appProcess, appTitle);
	}

	private WinNT.HANDLE getKernelHandle() {
		IntByReference pointer = new IntByReference();
		getUser32().GetWindowThreadProcessId(getUser32().GetForegroundWindow(), pointer);
		return getKernel32().OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, pointer.getValue());
	}

	private String executeWindCommand(final BiFunction<char[], HWND, Integer> biFunction) {
		char[] buffer = new char[BUFFER_SIZE * 2];
		HWND hwnd = getUser32().GetForegroundWindow();
		biFunction.apply(buffer, hwnd);
		return Native.toString(buffer);
	}

	private List<String> getApplicationNameAndTitle(final Function<String, String> function) throws IOException {
		String[] command = {"/bin/bash", "-c", "xprop -id $(xprop -root _NET_ACTIVE_WINDOW | cut -d ' ' -f5) _NET_WM_NAME WM_CLASS"};
		Function<String, String> defaultFunction = line1 -> line1.replaceAll("\"", "");
		return getBufferedReader(command)
				.lines()
				.map(function)
				.map(defaultFunction)
				.collect(toList());
	}

	private BufferedReader getBufferedReader(String[] command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		return new BufferedReader(
				new InputStreamReader(process.getInputStream()));
	}
}
