package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ts.andrey.entity.Item;
import ts.andrey.entity.ItemOutlay;

import java.util.List;
@Repository
public interface ItemOutlayRepository extends JpaRepository<ItemOutlay, Integer> {
    List<ItemOutlay> findItemsOutlayByItemPluAndWeekAfterAndWeekBefore(Item item, int after, int before);

}
