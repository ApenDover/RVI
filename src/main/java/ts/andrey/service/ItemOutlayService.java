package ts.andrey.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ts.andrey.entity.Item;
import ts.andrey.entity.ItemOutlay;
import ts.andrey.repositories.ItemOutlayRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemOutlayService {
    @Autowired
    private final ItemOutlayRepository itemOutlayRepository;

    @Autowired
    public ItemOutlayService(ItemOutlayRepository itemOutlayRepository) {
        this.itemOutlayRepository = itemOutlayRepository;
    }

    public List<ItemOutlay> findAll() {
        return itemOutlayRepository.findAll();
    }

    public List<ItemOutlay> findAllByItemPlu(Item item, int after, int before) {
        return itemOutlayRepository.findItemsOutlayByItemPluAndWeekAfterAndWeekBefore(item, after, before);
    }

    public ItemOutlay findOne(int id) {
        Optional<ItemOutlay> foundItemOutlay = itemOutlayRepository.findById(id);
        return foundItemOutlay.orElse(null);
    }

    @Transactional
    public void save(ItemOutlay itemOutlay) {
        itemOutlayRepository.save(itemOutlay);
    }

    @Transactional
    public void removeAll() {
        itemOutlayRepository.deleteAll();
    }
}
