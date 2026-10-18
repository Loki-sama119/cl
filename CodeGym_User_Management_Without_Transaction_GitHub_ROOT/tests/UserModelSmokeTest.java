import com.codegym.model.User;

/** Pure-Java smoke test; run with: javac -d .test-classes src/main/java/com/codegym/model/User.java tests/UserModelSmokeTest.java && java -cp .test-classes UserModelSmokeTest */
public class UserModelSmokeTest {
    public static void main(String[] args) {
        User first = new User("Minh", "minh@codegym.vn", "Viet Nam");
        check(first.getId() == 0, "unsaved user's id is zero");
        check("Minh".equals(first.getName()), "constructor stores name");
        check("minh@codegym.vn".equals(first.getEmail()), "constructor stores email");
        check("Viet Nam".equals(first.getCountry()), "constructor stores country");
        first.setId(12);
        first.setName("Kante");
        first.setEmail("kante@che.uk");
        first.setCountry("Kenia");
        check(first.getId() == 12, "id getter/setter");
        check("Kante".equals(first.getName()), "name getter/setter");
        check("kante@che.uk".equals(first.getEmail()), "email getter/setter");
        check("Kenia".equals(first.getCountry()), "country getter/setter");
        User existing = new User(3, "Alex", "alex@demo.com", "VN");
        check(existing.getId() == 3, "4-arg constructor stores id");
        System.out.println("PASS: 9 User model checks");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
