package ts.andrey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ts.andrey.controller.MainController;
import ts.andrey.model.Item;
import ts.andrey.model.Supplier;

import java.util.HashMap;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Забыл вставить запись в аргумент или указать его.");
            System.exit(1);
        }

        SpringApplication application = new SpringApplication(Main.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        ApplicationContext context = SpringApplication.run(Main.class, args);
        MainController mainController = context.getBean(MainController.class);

        try {
//            HashMap<Supplier, HashMap<Item, Integer>> result = mainController.main("/Users/andrey/Downloads/RVI-justCount.xlsx"); // путь к основной табличке
            HashMap<Supplier, HashMap<Item, Integer>> result = mainController.main(args[0]); // путь к основной табличке


            result.forEach((s, integerIntegerHashMap) -> {
                System.out.println("--------------");
                System.out.println(s.getName() + ": ");
                final int[] sum = {0};
                integerIntegerHashMap.forEach((item, integer) -> {
                    if (s.getMinOrder() < 500) integer = integer * item.getQuantum();
                    System.out.println(item.getPlu() + " - " + integer);
                    sum[0] = sum[0] + integer;
                });
                System.out.println(sum[0] + " / " + s.getMinOrder());
            });

            HashMap<Supplier, HashMap<Item, Integer>> actualResult = new HashMap<>();

            result.forEach((key, value) -> {
                HashMap<Item, Integer> actualMap = new HashMap<>();
                value.forEach((key1, value1) -> actualMap.put(key1, value1 * key1.getQuantum()));
                actualResult.put(key, actualMap);
            });

            mainController.writeExel(args[1], actualResult); // куда сохранять заказ
//            mainController.writeExel("/Users/andrey/Downloads/ORDER2.xlsx", actualResult); // куда сохранять заказ

        } catch (Exception e) {
            System.out.println(e.getMessage() + " - " + e.getStackTrace());
        }

        SpringApplication.exit(context);

    }
}