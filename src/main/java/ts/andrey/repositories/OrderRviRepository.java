package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ts.andrey.entity.OrderRvi;

@Repository
public interface OrderRviRepository extends JpaRepository<OrderRvi, Integer> {
}
