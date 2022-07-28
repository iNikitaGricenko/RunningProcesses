import java.util.Objects;

import static java.lang.String.format;

public class Application {

		private String xid;
		private String name;
		private String title;

		public Application() {
		}

		public Application(String xid, String name, String title) {
				this.xid = xid;
				this.name = name;
				this.title = title;
		}

		public String getXid() {
				return xid;
		}

		public void setXid(String xid) {
				this.xid = xid;
		}

		public String getName() {
				return name;
		}

		public void setName(String name) {
				this.name = name;
		}

		public String getTitle() {
				return title;
		}

		public void setTitle(String title) {
				this.title = title;
		}

		@Override
		public String toString() {
				return format("Application {\nxid = %s,\nname = %s,\ntitle = %s\n}",
						xid, name, title);
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
				return getXid().equals(that.getXid())
						&& Objects.equals(getName(), that.getName())
						&& Objects.equals(getTitle(), that.getTitle());
		}

		@Override
		public int hashCode() {
				return Objects.hash(getXid(), getName(), getTitle());
		}
}
