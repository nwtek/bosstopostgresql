import static spark.Spark.get;

public class BossRest extends Main {
    public static void main(String[] args) {

        get("/parse/:id", (req, res) -> {
            String id = req.params(":id");

            //return "YOU PROVIDED THE FOLLOWING ID: " + id;
           return Main.queryAttachment(id);
        });

        get("/validate/:id", (req, res) -> {
            String id = req.params(":id");

            return "YOU ARE VALIDATING THE FOLLOWING ID: " + id;
        });
    }
}