package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ts.andrey.model.Item;
import ts.andrey.model.Supplier;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findItemsBySupplierAndDeliveryWeekIsNotNullAndStatusIs(Supplier supplier, String status);

    @Query("select i from Item i "
            + "where i.supplier.id = :supplierId "
            + "and i.status = :status")
    List<Item> findItemsBySupplierAndStatusIs(
            @Param("supplierId") int supplierId,
            @Param("status") String status);

    List<Item> findItemsByOrderWeekAndStatusIs(int orderWeek, String status);

    List<Item> findItemsByOrderWeekAndSupplierAndStatusIs(int orderWeek, Supplier supplier, String status);

}
