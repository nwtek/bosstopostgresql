import static spark.Spark.*;

public class BossRest {
    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello from yonder!");
    }
}