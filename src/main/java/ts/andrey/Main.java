package ts.andrey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ts.andrey.head.CountAll;
import ts.andrey.entity.Item;
import ts.andrey.entity.Supplier;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        final var application = new SpringApplication(Main.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        final var context = SpringApplication.run(Main.class, args);
        final var countAll = context.getBean(CountAll.class);

        final var result = countAll.count("/Users/andrey/Downloads/RVI-justCount.xlsx"); // путь к основной табличке
//        final var result = countAll.main(args[0]); // путь к основной табличке

        final var actualResult = palletToPieces(result);

        try {
//            countAll.writeExel(args[1], actualResult); // куда сохранять заказ
            countAll.writeExel("/Users/andrey/Downloads/ORDER.xlsx", actualResult); // куда сохранять заказ

        } catch (IOException e) {
            System.out.println(e.getMessage() + " - " + Arrays.toString(e.getStackTrace()));
        }

        System.out.println();
        SpringApplication.exit(context);

    }

    private static HashMap<Supplier, HashMap<Item, Integer>> palletToPieces(HashMap<Supplier, HashMap<Item, Integer>> result) {

        result.forEach((s, integerIntegerHashMap) -> {
            System.out.println(s.getName().toUpperCase() + ": ");
            final int[] sum = {0};
            integerIntegerHashMap.forEach((item, integer) -> {
                if (s.getMinOrder() < 500) {
                    System.out.print(item.getPlu() + " - " + integer);
                    integer = integer * item.getQuantum();
                    System.out.println(" (" + integer + ")");
                } else {
                    System.out.println(item.getPlu() + " - " + integer);
                }
                sum[0] = sum[0] + integer;
            });
            System.out.println(sum[0] + " / " + s.getMinOrder());
            System.out.println("-------------------");
        });

        HashMap<Supplier, HashMap<Item, Integer>> actualResult = new HashMap<>();

        result.forEach((key, value) -> {
            HashMap<Item, Integer> actualMap = new HashMap<>();
            value.forEach((key1, value1) -> actualMap.put(key1, value1 * key1.getQuantum()));
            actualResult.put(key, actualMap);
        });
        return actualResult;
    }

}