public class Launcher {
		public static void main(String[] args) {
				Processes myProcesses = new Processes();

				myProcesses.getOpenedApplicationNames()
						.stream()
						.forEach(System.out::println);
		}
}
