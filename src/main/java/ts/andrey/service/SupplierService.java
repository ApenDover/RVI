package ts.andrey.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ts.andrey.model.Supplier;
import ts.andrey.repositories.SupplierRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierService {
    @Autowired
    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findOne(int id) {
        Optional<Supplier> foundSupplier = supplierRepository.findById(id);
        return foundSupplier.orElse(null);
    }
    public Supplier findByName(String name) {
        Optional<Supplier> foundSupplier = Optional.ofNullable(supplierRepository.findSupplierByName(name));
        return foundSupplier.orElse(null);
    }

    @Transactional
    public Supplier save(Supplier order) {
        Supplier supplier = supplierRepository.save(order);
        return supplier;
    }

    @Transactional
    public void removeAll() {
        supplierRepository.deleteAll();
    }
}
