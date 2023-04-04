package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ts.andrey.model.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Supplier findSupplierByName(String name);
}
