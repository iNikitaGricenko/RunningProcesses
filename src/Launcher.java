import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Launcher {
		public static void main(String[] args) {

				String systemOS = System.getProperty("os.name");

				if ("Linux".equals(systemOS)) {
						linuxRunningProcesses().stream()
								.forEach(System.out::println);
				} else {
						windowsRunningProcesses().stream()
								.forEach(System.out::println);
				}
		}

		private static Collection<String> linuxRunningProcesses() {
				try {
						String userName = System.getProperty("user.name");
						Process process = Runtime.getRuntime()
								.exec(format("ps -U %s -au", userName));

						BufferedReader input = new BufferedReader(
								new InputStreamReader(process.getInputStream()));

						return input.lines()
								.filter(Launcher::checkForUser)
								.map(s -> s.split(":")[2].split(" ")[1])
								.collect(Collectors.toSet());
				} catch (IOException e) {
						throw new RuntimeException(e);
				}
		}

		private static Collection<String> windowsRunningProcesses() {
				try {
						String userName = System.getProperty("user.name");
						Process process = Runtime.getRuntime()
								.exec(format("tasklist /U'%s'", userName));

						BufferedReader input = new BufferedReader(
								new InputStreamReader(process.getInputStream()));

						return input.lines()
								.map(line -> line.split(" {2}")[0])
								.collect(Collectors.toSet());
				} catch (IOException e) {
						throw new RuntimeException(e);
				}
		}

		private static boolean checkForUser(String st) {
				String propertyUsername = System.getProperty("user.name");
				if (propertyUsername.length() > 7) {
						propertyUsername = propertyUsername.substring(0, 7) + "+";
				}
				return st.contains(propertyUsername);
		}
}
