package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ts.andrey.model.Item;
import ts.andrey.model.ItemOutlay;

import java.util.List;
@Repository
public interface ItemOutlayRepository extends JpaRepository<ItemOutlay, Integer> {
    List<ItemOutlay> findItemsOutlayByItemPluAndWeekAfterAndWeekBefore(Item item, int after, int before);

}
