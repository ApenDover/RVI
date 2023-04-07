package ts.andrey.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ts.andrey.entity.OrderRvi;
import ts.andrey.repositories.OrderRviRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderRviService {
    @Autowired
    private final OrderRviRepository orderRviRepository;

    @Autowired
    public OrderRviService(OrderRviRepository orderRviRepository) {
        this.orderRviRepository = orderRviRepository;
    }

    public List<OrderRvi> findAll() {
        return orderRviRepository.findAll();
    }

    @Transactional
    public OrderRvi save(OrderRvi orderRvi) {
        OrderRvi order = orderRviRepository.save(orderRvi);
        return order;
    }

    @Transactional
    public void removeAll() {
        orderRviRepository.deleteAll();
    }
}
