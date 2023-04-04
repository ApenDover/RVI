package ts.andrey.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ts.andrey.model.DistributionCenter;
import ts.andrey.repositories.DistributionCenterRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DistributionCenterService {
    @Autowired
    private final DistributionCenterRepository distributionCenterRepository;
    @Autowired
    public DistributionCenterService(DistributionCenterRepository distributionCenterRepository) {
        this.distributionCenterRepository = distributionCenterRepository;
    }

    public List<DistributionCenter> findAll() {
        return distributionCenterRepository.findAll();
    }

    public DistributionCenter findOne(int id) {
        Optional<DistributionCenter> foundDistributionCenter = distributionCenterRepository.findById(id);
        return foundDistributionCenter.orElse(null);
    }

    @Transactional
    public void save(DistributionCenter distributionCenter) {
        distributionCenterRepository.save(distributionCenter);
    }

    @Transactional
    public void removeAll() {
        distributionCenterRepository.deleteAll();
    }
}
