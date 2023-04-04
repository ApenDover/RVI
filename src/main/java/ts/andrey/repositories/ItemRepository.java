package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ts.andrey.model.Item;
import ts.andrey.model.Supplier;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findItemsBySupplierAndDeliveryWeekIsNotNullAndStatusIs(Supplier supplier, String status);
    List<Item> findItemsBySupplierAndStatusIs(Supplier supplier, String status);
    List<Item> findItemsByOrderWeekAndStatusIs(int orderWeek, String status);
    List<Item> findItemsByOrderWeekAndSupplierAndStatusIs(int orderWeek, Supplier supplier, String status);
}
