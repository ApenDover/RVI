package ts.andrey.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ts.andrey.model.DistributionCenter;

@Repository
public interface DistributionCenterRepository extends JpaRepository<DistributionCenter, Integer> {
}
