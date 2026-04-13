import java.nio.file.Paths;

public class Demo1 {

    public static void main(String[] args) {

    System.out.println(Paths.get(System.getProperty("allure.results.directory", "allure-results")));

    }
    public void hel(){}
}
