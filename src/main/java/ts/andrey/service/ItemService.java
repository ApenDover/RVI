package ts.andrey.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ts.andrey.entity.Item;
import ts.andrey.entity.Supplier;
import ts.andrey.repositories.ItemRepository;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

@Service
@Transactional
public class ItemService {
    @Autowired
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public int findMinDeliveryWeek(Supplier supplier){
        TreeSet<Integer> allDeliveryWeek = new TreeSet<>();
        itemRepository.findItemsBySupplierAndDeliveryWeekIsNotNullAndStatusIs(supplier, "Активная").forEach(item -> {
            allDeliveryWeek.add(item.getDeliveryWeek());
        });
        allDeliveryWeek.remove(0);
        return Collections.min(allDeliveryWeek);
    }

    public List<Item> findAllBySupplier(Supplier supplier) {
        return itemRepository.findItemsBySupplierAndStatusIs(supplier, "Активная");
    }
    public List<Item> findAllBySupplierNotNull(Supplier supplier) {
        return itemRepository.findItemsBySupplierAndDeliveryWeekIsNotNullAndStatusIs(supplier, "Активная");
    }

    public List<Item> findAllByOrderWeek(int orderWeek) {
        return itemRepository.findItemsByOrderWeekAndStatusIs(orderWeek, "Активная");
    }

    public List<Item> findAllByOrderWeekAndSupplier(int orderWeek, Supplier supplier) {
        return itemRepository.findItemsByOrderWeekAndSupplierAndStatusIs(orderWeek, supplier, "Активная");
    }

    public Item findOne(int plu) {
        Optional<Item> foundItem = itemRepository.findById(plu);
        return foundItem.orElse(null);
    }

    @Transactional
    public void save(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void removeAll() {
        itemRepository.deleteAll();
    }
}
