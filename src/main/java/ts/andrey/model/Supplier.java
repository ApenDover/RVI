package ts.andrey.model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

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

    public Supplier(String name, int minOrder, String country, boolean supplyWay, int lt, List<Item> itemList) {
        this.id = id;
        this.name = name;
        this.minOrder = minOrder;
        this.country = country;
        this.supplyWay = supplyWay;
        this.lt = lt;
        this.itemList = itemList;
    }

    public Supplier() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(int minOrder) {
        this.minOrder = minOrder;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isSupplyWay() {
        return supplyWay;
    }

    public void setSupplyWay(boolean supplyWay) {
        this.supplyWay = supplyWay;
    }

    public int getLt() {
        return lt;
    }

    public void setLt(int lt) {
        this.lt = lt;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

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
