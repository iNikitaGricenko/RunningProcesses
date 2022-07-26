import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toList;

public class Processes {

		public Collection<String> getOpenedApplicationNames() {
				String systemOS = System.getProperty("os.name");

				if ("Linux".equals(systemOS)) {
						return linuxRunningProcesses();
				}
				return Collections.emptyList();
		}

		private Collection<String> linuxRunningProcesses() {
				try {
						Process process = Runtime.getRuntime().exec("xlsclients");

						BufferedReader input = new BufferedReader(
								new InputStreamReader(process.getInputStream()));

						return input.lines()
								.map(s -> s.split(" {2}", 0)[1])
								.collect(toList());
				} catch (IOException e) {
						throw new RuntimeException(e);
				}
		}

}
