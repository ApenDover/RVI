package ts.andrey.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;
import ts.andrey.logic.ThisWeekNumber;
import ts.andrey.entity.Supplier;

import java.util.List;


@Component
public class ItemDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ItemDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Integer> allOrderWeek(Supplier supplier) {
        int nowWeek = new ThisWeekNumber().getWeek();
        return jdbcTemplate.query("SELECT DISTINCT order_week from item where supplier_id = ? and order_week >= ?;", new Object[]{supplier.getId(), nowWeek}, new SingleColumnRowMapper<>(Integer.class));
    }
}
