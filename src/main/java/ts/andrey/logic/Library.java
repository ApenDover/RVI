package ts.andrey.logic;

import ts.andrey.model.ItemOutlay;

import java.util.List;
import java.util.TreeSet;

public class Library {
    private static TreeSet<ItemOutlay> itemOutlayList = new TreeSet<>();

    public static TreeSet<ItemOutlay> getItemOutlayList() {
        return itemOutlayList;
    }

    public static void setItemOutlayList(List<ItemOutlay> itemOutlayList) {
        itemOutlayList.addAll(itemOutlayList);
    }
}
