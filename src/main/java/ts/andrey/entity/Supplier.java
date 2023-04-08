package ts.andrey.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Supplier implements Comparable<Supplier> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "min_order")
    private int minOrder;
    @Column(name = "country")
    private String country;
    @Column(name = "supply_way")
    private boolean supplyWay;
    @Column(name = "lt")
    private int lt;
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Item> itemList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Supplier)) return false;
        Supplier supplier = (Supplier) o;
        return getName().equals(supplier.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public int compareTo(Supplier o) {
        return o.getName().compareTo(getName());
    }
}
