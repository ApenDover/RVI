package ts.andrey.exel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ts.andrey.entity.DistributionCenter;
import ts.andrey.entity.Item;
import ts.andrey.entity.ItemOutlay;
import ts.andrey.entity.Supplier;
import ts.andrey.service.ItemService;
import ts.andrey.service.SupplierService;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

@Getter
public class ReadExel {
    private TreeSet<Supplier> supplierTreeSet;
    private TreeSet<Item> itemTreeSet;
    private TreeSet<ItemOutlay> itemOutlayTreeSet;

    public ReadExel(String fileName, SupplierService supplierService, ItemService itemService) {

        try {

            final var workbook = new XSSFWorkbook(fileName);
            final var titleNumberHashMap = readTitleList1(workbook);
            final var titleNumberHashMapList2 = readTitleList2(workbook);
            this.supplierTreeSet = readSupplier(workbook, titleNumberHashMap, titleNumberHashMapList2);
            this.itemTreeSet = readItem(workbook, titleNumberHashMap, supplierService);
            this.itemOutlayTreeSet = readItemOutlay(workbook, titleNumberHashMap, itemService, supplierService);
            workbook.close();

        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
        }

    }

    private static TreeSet<Item> readItem(Workbook wb, HashMap<String, Integer> titleNumberHashMap, SupplierService supplierService) {
        final var itemArrayList = new TreeSet<Item>();
        final var sheet = wb.getSheetAt(0);
        int i = 4;
        while (sheet.getRow(i) != null) { //  от 4 до ниже
            final var row = sheet.getRow(i);
            final var plu = (int) row.getCell(titleNumberHashMap.get("plu")).getNumericCellValue();
            final var status = row.getCell(titleNumberHashMap.get("status")).getStringCellValue();
            final var name = row.getCell(titleNumberHashMap.get("itemName")).getStringCellValue();
            final var quantum = (int) row.getCell(titleNumberHashMap.get("quantum")).getNumericCellValue();
            final var countStore = (int) row.getCell(titleNumberHashMap.get("countStore")).getNumericCellValue();
            final var orderWeek = (int) row.getCell(titleNumberHashMap.get("orderWeek")).getNumericCellValue();
            final var deliveryWeek = (int) row.getCell(titleNumberHashMap.get("deliveryWeek")).getNumericCellValue();
            final var recommendedOrder = (int) row.getCell(titleNumberHashMap.get("recommendedOrder")).getNumericCellValue();
            final var stockDC = (int) row.getCell(titleNumberHashMap.get("stockDC")).getNumericCellValue();
            final var stockStore = (int) row.getCell(titleNumberHashMap.get("stockStore")).getNumericCellValue();

            final var supplier = supplierService.findByName(row.getCell(titleNumberHashMap.get("supplierName")).getStringCellValue()); // по имени производителя?

            List<DistributionCenter> distributionCenter = new ArrayList<>();
            List<ItemOutlay> itemOutlayList = new ArrayList<>();

            Item item = new Item(plu, supplier, status, name, quantum, countStore, orderWeek, deliveryWeek, recommendedOrder, stockDC, stockStore, false, distributionCenter, itemOutlayList);
            itemArrayList.add(item);
            i++;

        }
        return itemArrayList;
    }

    private static TreeSet<Supplier> readSupplier(Workbook wb, HashMap<String, Integer> titleNumberHashMap, HashMap<String, Integer> titleNumberHashMapList2) {

        final var supplierArrayLists = new TreeSet<Supplier>();
        final var supplierMinOrder = new HashMap<>();
        final var sheetTwo = wb.getSheetAt(1);
        int k = 1;
        while (sheetTwo.getRow(k) != null) {
            final var row = sheetTwo.getRow(k);
            final var name = row.getCell(titleNumberHashMapList2.get("supplierName")).getStringCellValue();
            int minOrder = (int) row.getCell(titleNumberHashMapList2.get("minOrder")).getNumericCellValue();
            supplierMinOrder.put(name, minOrder);
            k++;
        }

        Sheet sheet = wb.getSheetAt(0);
        int i = 4;
        while (sheet.getRow(i) != null) { //  от 4 до ниже
            final var row = sheet.getRow(i);
            final var name = row.getCell(titleNumberHashMap.get("supplierName")).getStringCellValue();
            final var minOrder = (int) supplierMinOrder.get(name);
//            int piecesInPallet = supplierPiecesInPallet.get(name);
            int lt = (int) row.getCell(titleNumberHashMap.get("lt")).getNumericCellValue();

            List<Item> itemList = new ArrayList<>();

            Supplier supplier = new Supplier(name, minOrder, null, false, lt, itemList);
            i++;

            supplierArrayLists.add(supplier);
        }
        return supplierArrayLists;
    }

    private static TreeSet<ItemOutlay> readItemOutlay(Workbook wb, HashMap<String, Integer> titleNumberHashMap, ItemService itemService, SupplierService supplierService) {
        final var itemOutlayArrayList = new TreeSet<ItemOutlay>();
        final var sheet = wb.getSheetAt(0);
        int i = 4;
        while (sheet.getRow(i) != null) { //  от 4 до ниже
            final var row = sheet.getRow(i);
            final var item = itemService.findOne((int) row.getCell(titleNumberHashMap.get("plu")).getNumericCellValue());
            for (int k = 1; k < 54; k++) {
                if (titleNumberHashMap.containsKey("outlayCount" + k)) {
                    int outlayCount = (int) row.getCell(titleNumberHashMap.get("outlayCount" + k)).getNumericCellValue();
                    int deliveryCount = (int) row.getCell(titleNumberHashMap.get("deliveryCount" + k)).getNumericCellValue();
                    final var itemOutlay = new ItemOutlay(k, outlayCount, deliveryCount, item);
                    itemOutlayArrayList.add(itemOutlay);

                    final var deliveryWeek = itemService.findMinDeliveryWeek(supplierService.findByName(row.getCell(titleNumberHashMap.get("supplierName")).getStringCellValue()));
                    final var lt = (int) row.getCell(titleNumberHashMap.get("lt")).getNumericCellValue();

                    if (!item.isPromo()) {
                        if ((deliveryWeek + lt) < 54 & k >= deliveryWeek & k <= deliveryWeek + lt) {
                            final var promo = (int) row.getCell(titleNumberHashMap.get("promo" + k)).getNumericCellValue();
                            if (promo > 0) item.setPromo(true);
                        }
                        if ((deliveryWeek + lt) > 53) {
                            final var step = deliveryWeek + lt - 52;
                            if (k < step | k >= deliveryWeek) {
                                final var promo = (int) row.getCell(titleNumberHashMap.get("promo" + k)).getNumericCellValue();
                                if (promo > 0) item.setPromo(true);
                            }
                        }
                    }
                }
            }
            i++;
        }
        return itemOutlayArrayList;

    }

    private static HashMap<String, Integer> readTitleList1(Workbook wb) {
        final var titleNumberHashMap = new HashMap<String, Integer>();
        final var sheet = wb.getSheetAt(0);
        final var row = sheet.getRow(3);
        int i = 0; // << номер колонки
        while (row.getCell(i) != null) {
            final var cell = row.getCell(i);
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
        final var titleNumberHashMap = new HashMap<String, Integer>();
        var i = 0;
        final var sheetTwo = wb.getSheetAt(1);
        final var rowTwo = sheetTwo.getRow(0);
        while (rowTwo.getCell(i) != null) {
            final var cell = rowTwo.getCell(i);
            if (Objects.requireNonNull(cell.getCellType()) == CellType.STRING) {
                String s = cell.getRichStringCellValue().getString();
                if (s.equals("Поставщик")) {
                    titleNumberHashMap.put("supplierName", i);
                }
                if (s.equals("Объем к заказу мин")) {
                    titleNumberHashMap.put("minOrder", i);
                }
            }
            i++;
        }

        return titleNumberHashMap;
    }

    public static void writeWorkbook(Workbook wb, String fileName) {
        try {
            final var fileOut = new FileOutputStream(fileName);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
        }
    }

}

