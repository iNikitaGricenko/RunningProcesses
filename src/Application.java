import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

import static java.lang.String.format;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {

		private String name;
		private String title;

		@Override
		public String toString() {
				return format("Application {\nname = %s,\ntitle = %s\n}",
						name, title);
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) {
						return true;
				}
				if (o == null || getClass() != o.getClass()) {
						return false;
				}
				Application that = (Application) o;
				return Objects.equals(getName(), that.getName())
						&& Objects.equals(getTitle(), that.getTitle());
		}

		@Override
		public int hashCode() {
				return Objects.hash(getName(), getTitle());
		}
}
