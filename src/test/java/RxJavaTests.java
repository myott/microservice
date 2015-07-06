import junit.framework.TestCase;
import org.junit.Test;
import rx.Observable;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by matthewyott on 7/5/15.
 */
public class RxJavaTests extends TestCase {

	@Test
	public void testSomeStuff() throws Exception {
		Service.getProfiles().subscribe(profile -> {
			System.out.println(profile.toString());
		}, throwable -> {
			System.out.println("Error" + throwable.getMessage());
		});
	}

	public static class Service {
		static public Observable<Profile> getProfiles() {
			return Observable.create(s -> {
				IntStream.rangeClosed(1, new Random(100).nextInt() + 50).forEach(i -> {
					long id = new Random(100).nextLong() + 1;
					Profile p = new Profile(id, "Foo" + id);
					if (id % 5 == 0) {
						p.role = Role.admin;
					}
					if (id % 11 == 0) {
						s.onError(new Throwable("Bad profile:" + id));
					}
					s.onNext(p);
				});
			});
		}
	}

	static class Profile {
		Role role = Role.user;
		String first;
		Long id;

		public Profile(Long id, String first) {
			this.id = id;
			this.first = first;
		}

		@Override
		public String toString() {
			return "Profile{" +
					"role=" + role +
					", first='" + first + '\'' +
					", id=" + id +
					'}';
		}
	}

	enum Role {user, admin}
}
