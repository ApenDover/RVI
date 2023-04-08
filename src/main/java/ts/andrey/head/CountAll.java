package ts.andrey.head;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ts.andrey.entity.Item;
import ts.andrey.entity.ItemOutlay;
import ts.andrey.entity.OrderRvi;
import ts.andrey.entity.Supplier;
import ts.andrey.exel.ReadExel;
import ts.andrey.logic.ThisWeekNumber;
import ts.andrey.service.DistributionCenterService;
import ts.andrey.service.ItemOutlayService;
import ts.andrey.service.ItemService;
import ts.andrey.service.OrderRviService;
import ts.andrey.service.SupplierService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ts.andrey.constants.Constants.CRITICAL_TOVAR_ZAPAS;
import static ts.andrey.constants.Constants.DAYS_IN_A_WEEK;
import static ts.andrey.constants.Constants.PALLET_TO_ED_COUNT;
import static ts.andrey.constants.Constants.WEEK_IN_YEAR;

@Component
public class CountAll {
    private DistributionCenterService distributionCenterService;
    private ItemOutlayService itemOutlayService;
    private ItemService itemService;
    private SupplierService supplierService;
    private OrderRviService orderRviService;
    private int orderPalletCount = 0;
    private int firstWeek = 0;
    private int lastWeek = 0;
    private int countPalletNeeded = 0;
    private ArrayList<Item> removed = new ArrayList<>();
    private HashMap<Item, Integer> specialQuantumSupplier = new HashMap<>();
    private HashMap<Item, Integer> tz = new HashMap<>();

    private static TreeSet<Supplier> supplierTreeSet;
    private static TreeSet<Item> itemTreeSet;
    private static TreeSet<ItemOutlay> itemOutlayTreeSet;
    private static ArrayList<OrderRvi> orderRviTreeSet = new ArrayList<>();

    @Autowired
    public CountAll(DistributionCenterService distributionCenterService, ItemOutlayService itemOutlayService, ItemService itemService, SupplierService supplierService, OrderRviService orderRviService) {
        this.distributionCenterService = distributionCenterService;
        this.itemOutlayService = itemOutlayService;
        this.itemService = itemService;
        this.supplierService = supplierService;
        this.orderRviService = orderRviService;
    }

    public HashMap<Supplier, HashMap<Item, Integer>> count(String exelPath) {


//        supplierService.removeAll();
//        itemService.removeAll();
//        itemOutlayService.removeAll();

        ReadExel.read(exelPath, supplierService, itemService); // тут создал все объекты

        supplierTreeSet = ReadExel.getSupplierTreeSet();
        itemTreeSet = ReadExel.getItemTreeSet();
        itemOutlayTreeSet = ReadExel.getItemOutlayTreeSet();

        final var allSupplier = ReadExel.getSupplierTreeSet();

        final var finPackage = new HashMap<Supplier, HashMap<Item, Integer>>();

        for (Supplier supplier : allSupplier) {
            orderPalletCount = 0; // сколько палет заказано
            lastWeek = 0;
            firstWeek = 0;
            countPalletNeeded = 0; // сколько палет осталось до кванта

            final var allItemsSupplier = itemTreeSet.stream().filter(item -> item.getSupplier().equals(supplier)).collect(Collectors.toSet()); // << достаем все товары поставщика

            final var item = allItemsSupplier
                    .stream()
                    .filter(it -> it.getOrderWeek() == new ThisWeekNumber().getWeek())
                    .findFirst().orElse(null);

            if (item != null) { // есть хоть один товар на текущей неделе

                firstWeek = item.getOrderWeek();
                lastWeek = item.getDeliveryWeek();

                final var itemStockHashMap = new HashMap<>(tovarZapas(allItemsSupplier, itemOutlayTreeSet)); // << считаем товарный запас для каждого товара
                tz.putAll(itemStockHashMap);

//                System.out.println(" ############# ");
//                itemStockHashMap.forEach((i, integer) -> System.out.println(item.getPlu() + " - " + integer));
//                System.out.println(" ############# ");

//                System.out.println("\nосновной расчет закончен\n");


                itemStockHashMap.forEach((it, integer) -> {
                    if (integer * DAYS_IN_A_WEEK >= CRITICAL_TOVAR_ZAPAS) {
                        removed.add(item);
                    }
                });

//                SELECT DISTINCT order_week from item where supplier_id = ? and order_week >= ?;
//                final var allOrderWeek = new TreeSet<>(itemDAO.allOrderWeek(supplier)); // << отсортированные все недели на которых рекомендован заказ
                final var allOrderWeek = itemTreeSet.stream().filter(item1 -> item1.getSupplier().equals(supplier)).map(Item::getOrderWeek).collect(Collectors.toCollection(TreeSet::new));

                final var supplierPackage = new HashMap<Item, Integer>();


                for (Integer w : allOrderWeek) { // << перебираем все недели от текущей к последней и добиваем заказы
// считает только текущую, бесполезная херь
                    final var thisRound = countAllStroke(w, supplier);
                    thisRound.forEach((it, integer) -> {
                        if (supplierPackage.containsKey(it)) {
                            int sum = supplierPackage.get(it) + thisRound.get(it);
                            supplierPackage.put(it, sum);
                        } else {
                            supplierPackage.put(it, integer);
                        }
                    });

                }

                supplierPackage.size();

                HashMap<Item, Integer> memoryItemBeforeAddRecommendedOrder = new HashMap<>();
                allItemsSupplier.forEach(it -> {
                    memoryItemBeforeAddRecommendedOrder.put(item, item.getRecommendedOrder());
                });

                if (countPalletNeeded > 0) {
                    while (countPalletNeeded > 0) {
                        final var itemStockHashMapAddOne = new HashMap<>(tovarZapas(allItemsSupplier, itemOutlayTreeSet)); // << считаем товарный запас для каждого товара добавив квант к рекомендованному заказу

                        final var minTzItem = itemStockHashMapAddOne.entrySet()
                                .stream()
                                .min(Comparator.comparingInt(Map.Entry::getValue))
                                .get().getKey(); // взяли товар с минимальным товарным запаcом

                        tz.put(minTzItem, itemStockHashMapAddOne.get(minTzItem)); // актуализируем товарный запас для выбранного

                        minTzItem.setRecommendedOrder(minTzItem.getRecommendedOrder() + minTzItem.getQuantum()); // добавляем 1 квант товара
                        if (supplierPackage.containsKey(minTzItem)) {
                            final var orderCount = supplierPackage.get(minTzItem);
                            if (supplier.getMinOrder() > PALLET_TO_ED_COUNT) {
//                                System.out.println("добавляю " +  minTzItem.getQuantum());
                                supplierPackage.put(minTzItem, orderCount + minTzItem.getQuantum());
                                orderPalletCount = orderPalletCount + minTzItem.getQuantum();
                                countPalletNeeded = countPalletNeeded - minTzItem.getQuantum();
                            } else {
//                                System.out.println("добавляю " + 1);
                                supplierPackage.put(minTzItem, orderCount + 1);
                                orderPalletCount = orderPalletCount + 1;
                                countPalletNeeded = countPalletNeeded - 1;
                            }
                        } else {
                            if (supplier.getMinOrder() > PALLET_TO_ED_COUNT) {
//                                System.out.println("добавляю " +  minTzItem.getQuantum());
                                supplierPackage.put(minTzItem, minTzItem.getQuantum());
                                orderPalletCount = orderPalletCount + minTzItem.getQuantum();
                                countPalletNeeded = countPalletNeeded - minTzItem.getQuantum();
                            } else {
//                                System.out.println("добавляю " + 1);
                                supplierPackage.put(minTzItem, 1);
                                orderPalletCount = orderPalletCount + 1;
                                countPalletNeeded = countPalletNeeded - 1;
                            }
                        }
                        minTzItem.setRecommendedOrder(minTzItem.getRecommendedOrder() + minTzItem.getQuantum());
//                        addToPackage = addToPackage + (orderPalletCount - beforeAdd);
                    }
                }
                finPackage.put(supplier, supplierPackage); // << кладем в корзину исходя из недели и кванта поставщика
                memoryItemBeforeAddRecommendedOrder.forEach(Item::setRecommendedOrder);
            }
//            System.out.println("Добавил " + addToPackage);
        }

        return finPackage;
    }

    private HashMap<Item, Integer> countAllStroke(int orderWeek, Supplier supplier) {

        final var supplierPackage = new HashMap<Item, Integer>(); // << создаем корзину

//        public List<Item> findAllByOrderWeekAndSupplier(int orderWeek, Supplier supplier) {
//            return itemRepository.findItemsByOrderWeekAndSupplierAndStatusIs(orderWeek, supplier, "Активная");
//        }

//        final var itemSet = new TreeSet<>(itemService.findAllByOrderWeekAndSupplier(orderWeek, supplier)); //  <<  берем все товары на данной неделе для поставщика

        final var itemSet = itemTreeSet
                .stream()
                .filter(item -> item.getSupplier().equals(supplier)
                        && item.getStatus().equals("Активная")
                        && item.getOrderWeek() == orderWeek).
                collect(Collectors.toSet());

        // расчет заказа для текущей недели

        if (new ThisWeekNumber().getWeek() == orderWeek) {
            for (Item item : itemSet) {
                if (removed.contains(item)) {
                    continue;
                }
                lastWeek = item.getDeliveryWeek();
                firstWeek = item.getOrderWeek();

                final var orderCount = item.getRecommendedOrderRound() / item.getQuantum(); // << количество к заказу палетов в большую сторону

                if (supplier.getMinOrder() > PALLET_TO_ED_COUNT) {
                    // нужно все переводить в штуки
                    specialQuantumSupplier.put(item, supplier.getMinOrder() / item.getQuantum());
                    supplierPackage.put(item, orderCount);
//                    System.out.println(item.getSupplier().getName() + " - " + item.getPlu() + " - добавил " + orderCount + " штук");
                } else {
                    supplierPackage.put(item, orderCount);
//                    System.out.println(item.getSupplier().getName() + " - " + item.getPlu() + " - добавил " + orderCount + " палет");
                }
                // << добавляем в корзину
                orderPalletCount = orderPalletCount + orderCount; // << общее количество паллетов к заказу
            }
            // сколько не хватает палетов до закрытия последнего кванта поставщика

            countPalletNeeded = orderPalletCount;
            while (countPalletNeeded > supplier.getMinOrder()) {
                countPalletNeeded = countPalletNeeded - supplier.getMinOrder();
            }
            countPalletNeeded = supplier.getMinOrder() - countPalletNeeded; // << сколько нужно еще палет до кванта

//            System.out.println("Текущая неделя заказ добавлен: " + orderPalletCount + "/" + supplier.getMinOrder());
//            System.out.println("До кванта еще " + countPalletNeeded);

        } else {  // << не хватило палет, добиваем из рекомендаций на последующие недели
            var howAdd = countPalletNeeded;
            for (Item item : itemSet) {
                if (removed.contains(item)) {
                    continue;
                }
                if (howAdd > 0) { // если еще нужно добавлять палеты
                    final var countMaxQuantum = item.getRecommendedOrderRound() / item.getQuantum();
                    if (countPalletNeeded >= countMaxQuantum) { // если максимальное кол-во палетов по рекомендации имеет столько, то добавляем все возможные с этого товара
//                        System.out.println("Добавляю для " + item.getPlu() + " - " + countMaxQuantum);
                        supplierPackage.put(item, countMaxQuantum);
                        orderPalletCount = orderPalletCount + countMaxQuantum;
                        howAdd = howAdd - countMaxQuantum;
                        countPalletNeeded = howAdd;
                    } else { // << иначе просто закрываем потребность этим товаром
//                        System.out.println("Добавляю для " + item.getPlu() + " - " + countPalletNeeded);
                        supplierPackage.put(item, countPalletNeeded);
                        orderPalletCount = orderPalletCount + countPalletNeeded;
                        howAdd = howAdd - countPalletNeeded;
                        countPalletNeeded = 0;
                    }
                }
            }
//            System.out.println("Всего добавлено: " + orderPalletCount + "/" + supplier.getMinOrder());
//            System.out.println("До кванта еще " + countPalletNeeded);
        }
//        System.out.println("Всего добавлено: " + orderPalletCount + "/" + supplier.getMinOrder());
//        System.out.println("До кванта еще " + countPalletNeeded);

        return supplierPackage;

    }

    // Считаем для каждого товара товарный запас. Нужно для плавной догрузки в квант.
    private HashMap<Item, Integer> tovarZapas(Set<Item> itemSet, TreeSet<ItemOutlay> itemOutlayTreeSet) {

        final var itemStockHashMap = new LinkedHashMap<Item, Integer>();
        var checkEmptyWeekOrder = false;
        for (Item item : itemSet) {
            TreeSet<ItemOutlay> itemOutlayListPlu = new TreeSet<>();
            if (!(firstWeek == lastWeek & lastWeek == 0)) {
                checkEmptyWeekOrder = true;
                if (firstWeek < lastWeek) { // если все в пределах года
                    itemOutlayListPlu.addAll(itemOutlayTreeSet
                            .stream()
                            .filter(itemOutlay -> itemOutlay.getItemPlu().equals(item)
                                    && itemOutlay.getWeek() > firstWeek
                                    && itemOutlay.getWeek() < lastWeek + 1)
                            .collect(Collectors.toSet()));
//                    itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, firstWeek, lastWeek + 1)));
                } else { // если затрагиваем следующий год
                    itemOutlayListPlu.addAll(itemOutlayTreeSet
                            .stream()
                            .filter(itemOutlay -> itemOutlay.getItemPlu().equals(item)
                                    && itemOutlay.getWeek() > firstWeek
                                    && itemOutlay.getWeek() < WEEK_IN_YEAR + 1)
                            .collect(Collectors.toSet()));
//                    itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, firstWeek, WEEK_IN_YEAR + 1)));
                    itemOutlayListPlu.addAll(itemOutlayTreeSet
                            .stream()
                            .filter(itemOutlay -> itemOutlay.getItemPlu().equals(item)
                                    && itemOutlay.getWeek() > 0
                                    && itemOutlay.getWeek() < lastWeek + 1)
                            .collect(Collectors.toSet()));
//                    itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, 0, lastWeek + 1)));
                }
            } else {
//                itemOutlayListPlu.addAll(new TreeSet<>(itemOutlayService.findAllByItemPlu(item, 0, WEEK_IN_YEAR + 1)));
                itemOutlayListPlu.addAll(itemOutlayTreeSet
                        .stream()
                        .filter(itemOutlay -> itemOutlay.getItemPlu().equals(item)
                                && itemOutlay.getWeek() > 0
                                && itemOutlay.getWeek() < WEEK_IN_YEAR + 1)
                        .collect(Collectors.toSet()));
            }
            var i = 0; // просчитанный номер недели
            var i2 = 0; // просчитанный номер недели 2 этап - товарный запас, < 150
            var result = 0;
            var t = item.getStockStore() + item.getStockDC(); // вычисляемый stock

            for (ItemOutlay io : itemOutlayListPlu) { // отсортировано
                i++;
                t = t - io.getOutlayCount() + io.getDeliveryCount();
                if (item.getDeliveryWeek() == io.getWeek()) break;
            }

            if (t < 0) t = 0;

            t = t + item.getRecommendedOrderRound();

//            final var allActual = new TreeSet<>(itemOutlayService.findAllByItemPlu(item, 0, 54));

            final var allActual = itemOutlayTreeSet.stream().filter(itemOutlay -> itemOutlay.getItemPlu().equals(item)).collect(Collectors.toSet());

            final var allActualAfter = allActual.stream().filter(itemOutlay -> itemOutlay.getWeek() > lastWeek)
                    .collect(Collectors.toCollection(TreeSet::new));
            final var allActualBefore = allActual.stream().filter(itemOutlay -> itemOutlay.getWeek() < lastWeek)
                    .collect(Collectors.toCollection(TreeSet::new));
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
            var medium = 0;
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

    public void writeExel(String path, HashMap<Supplier, HashMap<Item, Integer>> result) throws IOException {

//        final var allOrderRvi = new ArrayList<>(orderRviService.findAll());

        final var workbook = new XSSFWorkbook();
        final var sheet = workbook.createSheet("order");
        final var row = sheet.createRow(0);
        final var orderTitlesArray = new String[]{"ORDER NUMBER", "NAME OF SUPPLIER",
                "PLU Number", "PRODUCT NAME IN RUSSIAN", "PRODUCT NAME IN ENGLISH",
                "ORDER (Q-TY)", "Price ", "REGION OF loading", "Week of loading", "Week of Arrival",
                "ETD", "ETA", "CIF/FCA/DAP", "Destination", "Цель закупки", "Дата заказа", "Метка промо", "Комментарий"};
        for (int i = 0; i < orderTitlesArray.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(orderTitlesArray[i]);
        }

        var r = 0;

        for (Supplier s : result.keySet()) {// перебираем всех поставщиков результата

//            final var orderRviList = new ArrayList<OrderRvi>();

//            final var deliveryWeek = itemService.findMinDeliveryWeek(s);

            itemTreeSet.removeAll(itemTreeSet
                    .stream()
                    .filter(item -> item.getDeliveryWeek() == 0)
                    .collect(Collectors.toSet()));

            final var deliveryWeek = Collections.min(itemTreeSet
                    .stream()
                    .filter(item ->
                            item.getSupplier().equals(s))
                    .collect(Collectors.toSet())
                    .stream()
                    .map(Item::getDeliveryWeek)
                    .collect(Collectors.toSet()));

            for (Map.Entry<Item, Integer> entry : result.get(s).entrySet()) { // записываем в базу общий заказ
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                String promoString = "";
                if (entry.getKey().isPromo()) promoString = "Да";
                else promoString = "Нет";

                OrderRvi orderRvi = new OrderRvi();
                orderRvi.setDateOrder(dateFormat.format(new Date()));
                orderRvi.setSupplierName(s.getName());
                orderRvi.setPlu(entry.getKey().getPlu());
                orderRvi.setProductNameRus(entry.getKey().getName());
                orderRvi.setOrderCount(entry.getValue());
                orderRvi.setWeekOfArrival(deliveryWeek);
                orderRvi.setDestination("Софьино");
                orderRvi.setPurposeOfOrder("Регуляр");
                orderRvi.setMetkaPromo(promoString);
                Item t = entry.getKey();
                orderRvi.setTz(tz.get(t));
//                if (!allOrderRvi.contains(orderRvi)) orderRviService.save(orderRvi);
                orderRviTreeSet.add(orderRvi);
            }

            for (OrderRvi orderRvi : orderRviTreeSet) {
                r++;
                Row rowData = sheet.createRow(r);
                final var cellSupplierName = rowData.createCell(1);
                cellSupplierName.setCellValue(s.getName());
                final var cellPluNumber = rowData.createCell(2);
                cellPluNumber.setCellValue(orderRvi.getPlu());
                final var cellPluName = rowData.createCell(3);
                cellPluName.setCellValue(orderRvi.getProductNameRus());
                final var cellOrderCount = rowData.createCell(5);
                cellOrderCount.setCellValue(orderRvi.getOrderCount());
                final var cellDeliveryWeek = rowData.createCell(9);
                cellDeliveryWeek.setCellValue(orderRvi.getWeekOfArrival());
                final var cellDestination = rowData.createCell(13);
                cellDestination.setCellValue(orderRvi.getDestination());
                final var cellTarget = rowData.createCell(14);
                cellTarget.setCellValue(orderRvi.getPurposeOfOrder());
                final var dateOrder = rowData.createCell(15);
                dateOrder.setCellValue(orderRvi.getDateOrder());
                final var promo = rowData.createCell(16);
                promo.setCellValue(orderRvi.getMetkaPromo());
                final var cellComment = rowData.createCell(17);
                cellComment.setCellValue((orderRvi.getTz() * DAYS_IN_A_WEEK) + " дн. ТЗ");
            }
            orderRviTreeSet.clear();

        }
        ReadExel.writeWorkbook(workbook, path);
    }
}
