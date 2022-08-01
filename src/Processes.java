import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;
import lombok.SneakyThrows;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinNT.*;
import static java.lang.String.format;

public class Processes {

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
		String name = getApplicationName(xid);
		String title = getApplicationTitle(xid);
		getAppIcon(xid);
		return new Application(xid, name, title);
	}

	private Application getWindowsActiveWindowInfo() throws IOException {
		String pid = getWindowsAppPid(); // returns 12032 (just pid)
		String appProcess = getWindowsAppProcess(); // returns app name in example C:\Program Files\JetBrains\IntelliJ IDEA Ultimate\bin\idea64.exe
		String appTitle = getWindowsAppTitle(); // returns app title in example RunningProcesses – Processes.java

//		throw new RuntimeException("get Running Processes on windows is currently unsupported");
		return new Application(pid, appProcess, appTitle);
	}

	private String getWindowsAppPid() {
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		int processId = User32.INSTANCE.GetWindowThreadProcessId(hwnd, new IntByReference());
		return String.valueOf(processId);
	}

	private String getWindowsAppTitle() {
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		char[] buffer = new char[1024 * 2];
		User32.INSTANCE.GetWindowText(User32.INSTANCE.GetForegroundWindow(), buffer, 1024);

		return Native.toString(buffer);
	}

	private String getWindowsAppProcess() {
		char[] bufferTwo = new char[1024 * 2];
		IntByReference pointer = new IntByReference();
//		User32.INSTANCE.GetWindowModuleFileName(User32.INSTANCE.GetForegroundWindow(), bufferTwo, 1024);
		User32.INSTANCE.GetWindowThreadProcessId(User32.INSTANCE.GetForegroundWindow(), pointer);
		HANDLE process = Kernel32.INSTANCE.OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, pointer.getValue());
		Psapi.INSTANCE.GetModuleFileNameExW(process, null, bufferTwo, 1024);
		return Native.toString(bufferTwo);
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

	private void getAppIcon(String xid) throws IOException {
		getBufferedReader("xprop -id $(xprop -root | awk '/_NET_ACTIVE_WINDOW\\(WINDOW\\)/{print $NF}') | awk '/_NET_WM_PID\\(CARDINAL\\)/{print $NF}'")
				.lines()
				.forEach(System.out::println);
	}

	private BufferedReader getBufferedReader(String command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		return new BufferedReader(
				new InputStreamReader(process.getInputStream()));
	}
}
