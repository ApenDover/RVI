package ts.andrey.controller;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ts.andrey.DAO.ItemDAO;
import ts.andrey.exel.ReadExel;
import ts.andrey.logic.NowWeekNumber;
import ts.andrey.model.*;
import ts.andrey.service.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class MainController {
    private DistributionCenterService distributionCenterService;
    private ItemOutlayService itemOutlayService;
    private ItemService itemService;
    private SupplierService supplierService;
    private OrderRviService orderRviService;
    private ItemDAO itemDAO;
    private int orderPalletCount = 0;
    private int firstWeek = 0;
    private int lastWeek = 0;
    private int countPalletNeeded = 0;
    private ArrayList<Item> removed = new ArrayList<>();
    private HashMap<Item, Integer> specialQuantumSupplier = new HashMap<>();
    private HashMap<Item, Integer> tz = new HashMap<>();

    @Autowired
    public MainController(DistributionCenterService distributionCenterService, ItemOutlayService itemOutlayService, ItemService itemService, SupplierService supplierService, OrderRviService orderRviService, ItemDAO itemDAO) {
        this.distributionCenterService = distributionCenterService;
        this.itemOutlayService = itemOutlayService;
        this.itemService = itemService;
        this.supplierService = supplierService;
        this.orderRviService = orderRviService;
        this.itemDAO = itemDAO;
    }

    @GetMapping()
    public HashMap<Supplier, HashMap<Item, Integer>> main(String exelPath) {

//        System.out.println(supplierService.findAll().size());

//        supplierService.removeAll();
        itemService.removeAll();
        itemOutlayService.removeAll();

        new ReadExel(exelPath, supplierService, itemService, itemOutlayService);
//        System.out.println("supplier: " + readExel.getSupplierTreeSet().size());
//        System.out.println("item: " + readExel.getItemTreeSet().size());
//        System.out.println("itemOutlay: " + readExel.getItemOutlayTreeSet().size());

        HashMap<Supplier, HashMap<Item, Integer>> fin = new HashMap<>();

        ArrayList<Supplier> allSupplier = new ArrayList<>(supplierService.findAll());

        for (Supplier supplier : allSupplier) {

            orderPalletCount = 0; // сколько палет заказано
            lastWeek = 0;
            firstWeek = 0;
            countPalletNeeded = 0; // сколько палет осталось до кванта

            Set<Item> allItemsSupplier = new TreeSet<>(itemService.findAllBySupplier(supplier));// << берем все товары поставщика

            Item it = allItemsSupplier.stream().filter(item -> item.getOrderWeek() == new NowWeekNumber().getWeek()).findFirst().orElse(null);
            if (it != null) {
                firstWeek = it.getOrderWeek();
                lastWeek = it.getDeliveryWeek();

                HashMap<Item, Integer> itemStockHashMap = new HashMap<>(tradeStocks(allItemsSupplier));// << считаем товарный запас для каждого товара
                tz.putAll(itemStockHashMap);
                System.out.println(" ############# ");
                itemStockHashMap.forEach((item, integer) -> System.out.println(item.getPlu() + " - " + integer));
                System.out.println(" ############# ");

                System.out.println("\nосновной расчет закончен\n");

                itemStockHashMap.forEach((item, integer) -> {
                    if (integer * 7 >= 150) {
//                        System.out.println("Удаляю " + item.getPlu());
                        removed.add(item);
                    }
                });


                TreeSet<Integer> allOrderWeek = new TreeSet<>(itemDAO.allOrderWeek(supplier)); // << отсортированные все недели на которых рекомендован заказ

                HashMap<Item, Integer> back = new HashMap<>();
                for (Integer i : allOrderWeek) { // << перебираем все недели от текущей к последней и добиваем заказы
                    HashMap<Item, Integer> thisRound = countAllStroke(i, supplier);
                    thisRound.forEach((item, integer) -> {
                        if (back.containsKey(item)) {
                            int sum = back.get(item) + thisRound.get(item);
                            back.put(item, sum);
                        } else back.put(item, integer);
                    });
                }
//                System.out.println(supplier.getName() + " - order pallet: " + pallet + " / " + supplier.getMinOrder());

                HashMap<Item, Integer> memoryItemBeforeAddRecommendedOrder = new HashMap<>();
                allItemsSupplier.forEach(item -> {
                    memoryItemBeforeAddRecommendedOrder.put(item, item.getRecommendedOrder());
                });

//                System.out.println("Я добавил все необходимы и сейчас осталось добавить " + nowPallet + " / " + supplier.getMinOrder());
//                back.forEach((item, integer) -> System.out.println(item.getPlu() + " - " + integer));

                if (countPalletNeeded > 0)
//                    System.out.println("\n -- Плавное добавление по всем: " + "(" + nowPallet + ") " + pallet + "/" + supplier.getMinOrder() + " -- \n");

                    while (countPalletNeeded > 0) {
                        HashMap<Item, Integer> itemStockHashMapAddOne = new HashMap<>(tradeStocks(allItemsSupplier)); // << считаем товарный запас для каждого товара добавив квант к рекомендованному заказу
//                        System.out.println(" ########################### ");
                        itemStockHashMapAddOne.forEach((item, integer) -> System.out.println(item.getPlu() + " : " + integer + " tz."));
                        System.out.println(" ############# ");
                        Item minItem = itemStockHashMapAddOne.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey(); // взяли товар с минимальным товарный запас

                        tz.put(minItem, itemStockHashMapAddOne.get(minItem)); // актуализируем товарный запас для выбранного

                        minItem.setRecommendedOrder(minItem.getRecommendedOrder() + minItem.getQuantum());
                        if (back.containsKey(minItem)) {
                            int orderCount = back.get(minItem);
                            if (supplier.getMinOrder() > 500) {
                                back.put(minItem, orderCount + minItem.getQuantum());
                                orderPalletCount = orderPalletCount + minItem.getQuantum();
                                countPalletNeeded = countPalletNeeded - minItem.getQuantum();
                            } else {
                                back.put(minItem, orderCount + 1);
                                orderPalletCount = orderPalletCount + 1;
                                countPalletNeeded = countPalletNeeded - 1;
                            }
                        } else {
                            if (supplier.getMinOrder() > 500) {
                                back.put(minItem, minItem.getQuantum());
                                orderPalletCount = orderPalletCount + minItem.getQuantum();
                                countPalletNeeded = countPalletNeeded - minItem.getQuantum();
                            } else {
                                back.put(minItem, 1);
                                orderPalletCount = orderPalletCount + 1;
                                countPalletNeeded = countPalletNeeded - 1;
                            }
                        }
                        minItem.setRecommendedOrder(minItem.getRecommendedOrder() + minItem.getQuantum());
                    }
                fin.put(supplier, back); // << кладем в корзину исходя из недели и кванта поставщика
                memoryItemBeforeAddRecommendedOrder.forEach(Item::setRecommendedOrder);
            }
        }
        return fin;
    }

    private HashMap<Item, Integer> countAllStroke(int orderWeek, Supplier supplier) {
        Scanner scanner = new Scanner(System.in);

        HashMap<Item, Integer> back = new HashMap<>(); // << создаем корзину

        TreeSet<Item> itemSet = new TreeSet<>(itemService.findAllByOrderWeekAndSupplier(orderWeek, supplier)); //  <<  берем все товары на данной неделе для поставщика
        // расчет заказа для текущей недели

        if (new NowWeekNumber().getWeek() == orderWeek) {
            for (Item item : itemSet) {
                if (removed.contains(item)) {
                    continue;
                }
                lastWeek = item.getDeliveryWeek();
                firstWeek = item.getOrderWeek();

                int orderCount = item.getRecommendedOrderRound() / item.getQuantum(); // << количество к заказу палетов в большую сторону

                if (supplier.getMinOrder() > 500) {
                    // нужно все переводить в штуки
                    specialQuantumSupplier.put(item, supplier.getMinOrder() / item.getQuantum());
                    back.put(item, orderCount);
                    System.out.println(item.getSupplier().getName() + " - " + item.getPlu() + " - добавил " + orderCount + " штук");
                } else {
                    back.put(item, orderCount);
                    System.out.println(item.getSupplier().getName() + " - " + item.getPlu() + " - добавил " + orderCount + " палет");
                }
//                scanner.nextLine();
                // << добавляем в корзину
                orderPalletCount = orderPalletCount + orderCount; // << общее количество палетов к заказу
            }
            // сколько не хватает палетов до закрытия последнего кванта поставщика
            countPalletNeeded = orderPalletCount;
            while (countPalletNeeded > supplier.getMinOrder()) {
                countPalletNeeded = countPalletNeeded - supplier.getMinOrder();
            }
            countPalletNeeded = supplier.getMinOrder() - countPalletNeeded; // << сколько нужно еще палет до кванта
        } else {  // << не хватило палет, добиваем из рекомендаций на последующие недели
            int howAdd = countPalletNeeded;
//            itemSet.addAll(itemService.findAllBySupplierNotNull(supplier)); //233! правка
            for (Item item : itemSet) {
                if (removed.contains(item)) {
                    continue;
                }
                if (howAdd > 0) { // если еще нужно добавлять палеты
                    int countMaxQuantum = item.getRecommendedOrderRound() / item.getQuantum();
                    if (countPalletNeeded >= countMaxQuantum) { // если максимальное кол-во палетов по рекомендации имеет столько, то добавляем все возможные с этого товара
                        back.put(item, countMaxQuantum);
                        orderPalletCount = orderPalletCount + countMaxQuantum;
                        howAdd = howAdd - countMaxQuantum;
                        countPalletNeeded = howAdd;
                    } else { // << иначе просто закрываем потребность этим товаром
                        back.put(item, countPalletNeeded);
                        orderPalletCount = orderPalletCount + countPalletNeeded;
                        howAdd = howAdd - countPalletNeeded;
                        countPalletNeeded = 0;
                    }
//                    System.out.println(nowPallet);
                }
            }
        }
        return back;
    }

    // Считаем для каждого товара товарный запас. Нужно для плавной догрузки в квант.
    private HashMap<Item, Integer> tradeStocks(Set<Item> itemSet) {

        LinkedHashMap<Item, Integer> itemStockHashMap = new LinkedHashMap<>();
        boolean checkEmptyWeekOrder = false;
        for (Item item : itemSet) {
            TreeSet<ItemOutlay> itemOutlayListPlu = new TreeSet<>();
            if (!(firstWeek == lastWeek & lastWeek == 0)) {
                checkEmptyWeekOrder = true;
                if (firstWeek < lastWeek) { // если все в пределах года
                    itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, firstWeek, lastWeek + 1)));
                } else { // если затрагиваем следующий год
                    itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, firstWeek, 54)));
                    itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, 0, lastWeek + 1)));
                }
            } else {
                itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, 0, 54)));
            }
            int i = 0; // просчитанный номер недели
            int i2 = 0; // просчитанный номер недели 2 этап - товарный запас, < 150
            int result = 0;
            int t = item.getStockStore() + item.getStockDC(); // вычисляемый stock

            for (ItemOutlay io : itemOutlayListPlu) { // отсортировано
                i++;
                t = t - io.getOutlayCount() + io.getDeliveryCount();
                if (item.getDeliveryWeek() == io.getWeek()) break;
            }

            if (t < 0) t = 0;

            t = t + item.getRecommendedOrderRound();

            TreeSet<ItemOutlay> allActual = new TreeSet<>(itemOutlayService.findAllByItemPlu(item, 0, 54));
            TreeSet<ItemOutlay> allActualAfter = allActual.stream().filter(itemOutlay -> itemOutlay.getWeek() > lastWeek).collect(Collectors.toCollection(TreeSet::new));
            TreeSet<ItemOutlay> allActualBefore = allActual.stream().filter(itemOutlay -> itemOutlay.getWeek() < lastWeek).collect(Collectors.toCollection(TreeSet::new));
            if (checkEmptyWeekOrder) {
                for (ItemOutlay io : allActual) {
                    result = result + io.getOutlayCount() - io.getDeliveryCount();
                    if (result > t) break;
                    i2++;
                }
            } else {
                for (ItemOutlay io : allActualAfter) {
                    result = result + io.getOutlayCount() - io.getDeliveryCount();
                    if (result > t) break;
                    i2++;
                }

                for (ItemOutlay io : allActualBefore) {
                    if (result < t) {
                        result = result + io.getOutlayCount() - io.getDeliveryCount();
                        if (result > t) break;
                        i2++;
                    }
                }
            }
            int medium = 0;
            for (ItemOutlay io : allActual) {
                medium = medium + io.getOutlayCount();
            }
            medium = medium / allActual.size();

            while (result < t) {
                result = result + medium;
                if (result > t) break;
                i2++;
            }
            itemStockHashMap.put(item, i2);
//            System.out.println("расчет для " + item.getPlu() + " : " + i2 + " tz.");
        }

        return itemStockHashMap;
    }

    private HashMap<Item, Integer> countItemStockDeliveryOrderAdd(Set<Item> itemSet) {

        // Расчитываем для каждого товара товарный запас. Нужно для плавной догрузки в квант.
        HashMap<Item, Integer> itemStockHashMap = new HashMap<>();

        for (Item item : itemSet) {
            TreeSet<ItemOutlay> itemOutlayListPlu = new TreeSet<>(itemOutlayService.findAllByItemPlu(item, firstWeek, lastWeek + 1));

            int i = 0; // просчитанный номер недели
            int i2 = 0; // просчитанный номер недели 2 этап - товарный запас, < 150
            int result = 0;
            int t = item.getStockStore() + item.getStockDC(); // вычисляемый stock

            for (ItemOutlay io : itemOutlayListPlu) { // отсортировано
                i++;
                t = t - io.getOutlayCount() + io.getDeliveryCount();
                if (item.getDeliveryWeek() == io.getWeek()) break;
            }

            t = t + item.getRecommendedOrderRound() + item.getQuantum();

            TreeSet<ItemOutlay> allActual = new TreeSet<>(itemOutlayService.findAllByItemPlu(item, lastWeek, 54));

            for (ItemOutlay io : allActual) {
                if (io.getWeek() > i) {
                    result = result + io.getOutlayCount() - io.getDeliveryCount();
                    if (result > t) break;
                    i2++;
                }
                t = t - result;
            }
            itemStockHashMap.put(item, i2);
        }
        return itemStockHashMap;
    }

    public void writeExel(String path, HashMap<Supplier, HashMap<Item, Integer>> result) throws IOException {

        ArrayList<OrderRvi> allOrderRvi = new ArrayList<>(orderRviService.findAll());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("order");
        Row row = sheet.createRow(0);
        String[] orderTitlesArray = {"ORDER NUMBER", "NAME OF SUPPLIER", "PLU Number", "PRODUCT NAME IN RUSSIAN", "PRODUCT NAME IN ENGLISH", "ORDER (Q-TY)", "Price ", "REGION OF loading", "Week of loading", "Week of Arrival", "ETD", "ETA", "CIF/FCA/DAP", "Destination", "Цель закупки", "Дата заказа", "Метка промо", "Комментарий"};
        for (int i = 0; i < orderTitlesArray.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(orderTitlesArray[i]);
        }

        int r = 0;
//        System.out.println("сколько палет по поставщику");

        for (Supplier s : result.keySet()) {// перебираем всех поставщиков результата

            ArrayList<OrderRvi> orderRviList = new ArrayList<>();

            int carsCount = 0; // сколько автомобилей у поставщика
            int deliveryWeek = itemService.findMinDeliveryWeek(s);
            int palletSum = 0; // сколько паллет данного товара
            HashMap<Item, Integer> itemPalletCount = new HashMap<>();

//            System.out.println(s.getName() + " " + palletSum); //  сколько палет по поставщику

            for (Map.Entry<Item, Integer> entry : result.get(s).entrySet()) { // записываем в базу общий заказ
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                String promoString = "";
                if (entry.getKey().isPromo()) promoString = "Да";
                else promoString = "Нет";

                OrderRvi orderRvi = new OrderRvi();
                orderRvi.setDateOrder(dateFormat.format(new Date()));
                orderRvi.setSupplierName(s.getName());
                int maxId = 0;
                if (allOrderRvi.size() == 0) maxId = 1;
                else maxId = allOrderRvi.stream().max(Comparator.comparingInt(OrderRvi::getId)).get().getId();

                orderRvi.setOrderNumber(s.getName().substring(0, 3) + String.format("%04d%n", maxId));
                orderRvi.setPlu(entry.getKey().getPlu());
                orderRvi.setProductNameRus(entry.getKey().getName());
                orderRvi.setOrderCount(entry.getValue());
                orderRvi.setWeekOfArrival(deliveryWeek);
                orderRvi.setDestination("Софьино");
                orderRvi.setPurposeOfOrder("Регуляр");
                orderRvi.setMetkaPromo(promoString);
                Item t = entry.getKey();
                orderRvi.setTz(tz.get(t));
                if (!allOrderRvi.contains(orderRvi)) orderRviService.save(orderRvi);
                orderRviList.add(orderRvi);

                r++;
                Row rowData = sheet.createRow(r);
                Cell cellSupplierName = rowData.createCell(1);
                cellSupplierName.setCellValue(s.getName());
                Cell cellPluNumber = rowData.createCell(2);
                cellPluNumber.setCellValue(orderRvi.getPlu());
                Cell cellPluName = rowData.createCell(3);
                cellPluName.setCellValue(orderRvi.getProductNameRus());
                Cell cellOrderCount = rowData.createCell(5);
                cellOrderCount.setCellValue(orderRvi.getOrderCount());
                Cell cellDeliveryWeek = rowData.createCell(9);
                cellDeliveryWeek.setCellValue(orderRvi.getWeekOfArrival());
                Cell cellDestination = rowData.createCell(13);
                cellDestination.setCellValue(orderRvi.getDestination());
                Cell cellTarget = rowData.createCell(14);
                cellTarget.setCellValue(orderRvi.getPurposeOfOrder());
                Cell dateOrder = rowData.createCell(15);
                dateOrder.setCellValue(orderRvi.getDateOrder());
                Cell promo = rowData.createCell(16);
                promo.setCellValue(orderRvi.getMetkaPromo());
                Cell cellComment = rowData.createCell(17);
                cellComment.setCellValue(orderRvi.getTz() * 7 + " дн. товарный запас");

            }

        }
        ReadExel.writeWorkbook(workbook, path);
    }
}
