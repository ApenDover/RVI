package ts.andrey.exel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ts.andrey.model.DistributionCenter;
import ts.andrey.model.Item;
import ts.andrey.model.ItemOutlay;
import ts.andrey.model.Supplier;
import ts.andrey.service.ItemOutlayService;
import ts.andrey.service.ItemService;
import ts.andrey.service.SupplierService;

import java.io.FileOutputStream;
import java.util.*;

public class ReadExel {

    private String fileName;
    private TreeSet<Supplier> supplierTreeSet = new TreeSet<>();
    private TreeSet<Item> itemTreeSet = new TreeSet<>();
    private TreeSet<ItemOutlay> itemOutlayTreeSet = new TreeSet<>();

    public ReadExel(String fileName, SupplierService supplierService, ItemService itemService, ItemOutlayService itemOutlayService) {
        this.fileName = fileName;
        ArrayList<Supplier> allSupplier = new ArrayList<>(supplierService.findAll());
        try {
            Workbook workbook = new XSSFWorkbook(fileName);
            HashMap<String, Integer> titleNumberHashMap = readTitleList1(workbook);
            HashMap<String, Integer> titleNumberHashMapList2 = readTitleList2(workbook);
            supplierTreeSet = readSupplier(workbook, titleNumberHashMap, titleNumberHashMapList2, supplierService);
            supplierTreeSet.forEach(supplier -> {
                if (!allSupplier.contains(supplier)) supplierService.save(supplier);
            });
            itemTreeSet = readItem(workbook, titleNumberHashMap, supplierService);
            itemTreeSet.forEach(itemService::save);
            itemOutlayTreeSet = readItemOutlay(workbook, titleNumberHashMap, itemService, supplierService);
            itemOutlayTreeSet.forEach(itemOutlay -> {
                itemOutlay.setId(1);
                itemOutlayService.save(itemOutlay);
            });
//            System.out.println();
            workbook.close();
        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
        }
    }

    private static TreeSet<Item> readItem(Workbook wb, HashMap<String, Integer> titleNumberHashMap, SupplierService supplierService) {
        TreeSet<Item> itemArrayList = new TreeSet<>();
        Sheet sheet = wb.getSheetAt(0);
        int i = 4;
        while (sheet.getRow(i) != null) { //  от 4 до ниже
            Row row = sheet.getRow(i);
            int plu = (int) row.getCell(titleNumberHashMap.get("plu")).getNumericCellValue();
            String status = row.getCell(titleNumberHashMap.get("status")).getStringCellValue();
            String name = row.getCell(titleNumberHashMap.get("itemName")).getStringCellValue();
            int quantum = (int) row.getCell(titleNumberHashMap.get("quantum")).getNumericCellValue();
            int countStore = (int) row.getCell(titleNumberHashMap.get("countStore")).getNumericCellValue();
            int orderWeek = (int) row.getCell(titleNumberHashMap.get("orderWeek")).getNumericCellValue();
            int deliveryWeek = (int) row.getCell(titleNumberHashMap.get("deliveryWeek")).getNumericCellValue();
            int recommendedOrder = (int) row.getCell(titleNumberHashMap.get("recommendedOrder")).getNumericCellValue();
            int stockDC = (int) row.getCell(titleNumberHashMap.get("stockDC")).getNumericCellValue();
            int stockStore = (int) row.getCell(titleNumberHashMap.get("stockStore")).getNumericCellValue();

            Supplier supplier = supplierService.findByName(row.getCell(titleNumberHashMap.get("supplierName")).getStringCellValue()); // по имени производителя?

            List<DistributionCenter> distributionCenter = new ArrayList<>();

            List<ItemOutlay> itemOutlayList = new ArrayList<>();

            Item item = new Item(plu, supplier, status, name, quantum, countStore, orderWeek, deliveryWeek, recommendedOrder, stockDC, stockStore, false, distributionCenter, itemOutlayList);
            itemArrayList.add(item);
            i++;

        }
        return itemArrayList;
    }

    private static TreeSet<Supplier> readSupplier(Workbook wb, HashMap<String, Integer> titleNumberHashMap, HashMap<String, Integer> titleNumberHashMapList2, SupplierService supplierService) {

        TreeSet<Supplier> supplierArrayLists = new TreeSet<>();
        HashMap<String, Integer> supplierMinOrder = new HashMap<>();
        HashMap<String, Integer> supplierPiecesInPallet = new HashMap<>();
        Sheet sheetTwo = wb.getSheetAt(1);
        int k = 1;
        while (sheetTwo.getRow(k) != null) {
            Row row = sheetTwo.getRow(k);
            String name = row.getCell(titleNumberHashMapList2.get("supplierName")).getStringCellValue();
            int minOrder = (int) row.getCell(titleNumberHashMapList2.get("minOrder")).getNumericCellValue();
            int piecesInPallet = (int) row.getCell(titleNumberHashMapList2.get("piecesInPallet")).getNumericCellValue();
            supplierMinOrder.put(name, minOrder);
            supplierPiecesInPallet.put(name, piecesInPallet);
            k++;
        }

        Sheet sheet = wb.getSheetAt(0);
        int i = 4;
        while (sheet.getRow(i) != null) { //  от 4 до ниже
            Row row = sheet.getRow(i);
            String name = row.getCell(titleNumberHashMap.get("supplierName")).getStringCellValue();
            int minOrder = supplierMinOrder.get(name);
            int piecesInPallet = supplierPiecesInPallet.get(name);
            int lt = (int) row.getCell(titleNumberHashMap.get("lt")).getNumericCellValue();
            List<Item> itemList = new ArrayList<>();
            Supplier supplier = new Supplier(name, minOrder, null, false, lt, itemList, piecesInPallet);
            i++;
            supplierArrayLists.add(supplier);
        }
        return supplierArrayLists;
    }

    private static TreeSet<ItemOutlay> readItemOutlay(Workbook wb, HashMap<String, Integer> titleNumberHashMap, ItemService itemService, SupplierService supplierService) {
        TreeSet<ItemOutlay> itemOutlayArrayList = new TreeSet<>();
        Sheet sheet = wb.getSheetAt(0);
        int i = 4;
        while (sheet.getRow(i) != null) { //  от 4 до ниже
            Row row = sheet.getRow(i);
            Item item = itemService.findOne((int) row.getCell(titleNumberHashMap.get("plu")).getNumericCellValue());
            for (int k = 1; k < 54; k++) {
                if (titleNumberHashMap.containsKey("outlayCount" + k)) {
                    int outlayCount = (int) row.getCell(titleNumberHashMap.get("outlayCount" + k)).getNumericCellValue();
                    int deliveryCount = (int) row.getCell(titleNumberHashMap.get("deliveryCount" + k)).getNumericCellValue();
                    ItemOutlay itemOutlay = new ItemOutlay(k, outlayCount, deliveryCount, item);
                    itemOutlayArrayList.add(itemOutlay);

                    int deliveryWeek = itemService.findMinDeliveryWeek(supplierService.findByName(row.getCell(titleNumberHashMap.get("supplierName")).getStringCellValue()));
                    int lt = (int) row.getCell(titleNumberHashMap.get("lt")).getNumericCellValue();

                    if (!item.isPromo()) {
                        if ((deliveryWeek + lt) < 54 & k >= deliveryWeek & k <= deliveryWeek + lt) {
                            int promo = (int) row.getCell(titleNumberHashMap.get("promo" + k)).getNumericCellValue();
                            if (promo > 0) item.setPromo(true);
                        }
                        if ((deliveryWeek + lt) > 53) {
                            int step = deliveryWeek + lt - 52;
                            if (k < step | k >= deliveryWeek) {
                                int promo = (int) row.getCell(titleNumberHashMap.get("promo" + k)).getNumericCellValue();
                                if (promo > 0) item.setPromo(true);
                            }
                        }
                    }
//                    System.out.println(itemOutlayArrayList.size());
//                    System.out.println();
                }
            }
            i++;
        }
        return itemOutlayArrayList;
    }

    private static HashMap<String, Integer> readTitleList1(Workbook wb) {
        HashMap<String, Integer> titleNumberHashMap = new HashMap<>();
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(3);
        int i = 0; // << номер колонки
        while (row.getCell(i) != null) {
            Cell cell = row.getCell(i);
            switch (cell.getCellType()) {
                case STRING:
                    String s = cell.getRichStringCellValue().getString();
                    if (s.equals("Код PLU")) {
                        titleNumberHashMap.put("plu", i);
                    }
                    if (s.equals("PLU")) {
                        titleNumberHashMap.put("itemName", i);
                    }
                    if (s.equals("Статус PLU")) {
                        titleNumberHashMap.put("status", i);
                    }
                    if (s.equals("Сток РЦ")) {
                        titleNumberHashMap.put("stockDC", i);
                    }
                    if (s.equals("Сток СМ")) {
                        titleNumberHashMap.put("stockStore", i);
                    }
                    if (s.equals("Производитель")) {
                        titleNumberHashMap.put("supplierName", i);
                    }
                    if (s.equals("LT")) {
                        titleNumberHashMap.put("lt", i);
                    }
                    if (s.equals("ACT кол-во СМ")) {
                        titleNumberHashMap.put("countStore", i);
                    }
                    if (s.equals("Шт./квант")) {
                        titleNumberHashMap.put("quantum", i);
                    }
                    if (s.equals("Рекомендованная неделя размещения заказа")) {
                        titleNumberHashMap.put("orderWeek", i);
                    }
                    if (s.equals("Рекомендованный к заказу объем")) {
                        titleNumberHashMap.put("recommendedOrder", i);
                    }
                    if (s.equals("Рекомендованная неделя прихода")) {
                        titleNumberHashMap.put("deliveryWeek", i);
                    }
                    break;
                case NUMERIC:
                case FORMULA:
                    if (i > 46 & i < 100) // outlayCount СМ
                    {
                        int rez = (int) cell.getNumericCellValue();
                        titleNumberHashMap.put("outlayCount" + rez, i);
                    }
                    if (i > 152 & i < 206) // deliveryCount СМ
                    {
                        int rez = (int) cell.getNumericCellValue();
                        titleNumberHashMap.put("deliveryCount" + rez, i);
                    }
                    if (i > 258 & i < 312) // deliveryCount СМ
                    {
                        int rez = (int) cell.getNumericCellValue();
                        titleNumberHashMap.put("promo" + rez, i);
                    }
                    break;
            }
            i++;
        }
        return titleNumberHashMap;
    }

    private static HashMap<String, Integer> readTitleList2(Workbook wb) {
        HashMap<String, Integer> titleNumberHashMap = new HashMap<>();
        int i = 0;
        Sheet sheetTwo = wb.getSheetAt(1);
        Row rowTwo = sheetTwo.getRow(0);
        while (rowTwo.getCell(i) != null) {
            Cell cell = rowTwo.getCell(i);
            if (Objects.requireNonNull(cell.getCellType()) == CellType.STRING) {
                String s = cell.getRichStringCellValue().getString();
                if (s.equals("Поставщик")) {
                    titleNumberHashMap.put("supplierName", i);
                }
                if (s.equals("Объем к заказу мин")) {
                    titleNumberHashMap.put("minOrder", i);
                }
                if (s.equals("В паллете")) {
                    titleNumberHashMap.put("piecesInPallet", i);
                }
            }
            i++;
        }
        return titleNumberHashMap;
    }

    public static void writeWorkbook(Workbook wb, String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
        }
    }
}

