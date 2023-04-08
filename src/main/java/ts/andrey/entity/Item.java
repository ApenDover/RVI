package ts.andrey.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.CascadeType;
import javax.persistence.Id;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Entity
@Table(name = "Item")
public class Item implements Comparable<Item> {

    @Id
    @Column(name = "plu")
    private int plu;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id")
    private Supplier supplier;
    @Column(name = "status")
    private String status;
    @Column(name = "name")
    private String name;
    @Column(name = "quantum")
    private int quantum;
    @Column(name = "count_store")
    private int countStore;
    @Column(name = "order_week")
    private Integer orderWeek;
    @Column(name = "delivery_week")
    private Integer deliveryWeek;
    @Column(name = "recommended_order")
    private Integer recommendedOrder;
    @Column(name = "stock_distribution_center")
    private int stockDC;
    @Column(name = "stock_store")
    private int stockStore;
    @Column(name = "promo")
    private boolean promo;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "distribution_center_item",
            joinColumns = @JoinColumn(name = "item_plu", referencedColumnName = "plu"),
            inverseJoinColumns = @JoinColumn(name = "distribution_center_id", referencedColumnName = "id"))
    private List<DistributionCenter> distributionCenterList;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<ItemOutlay> itemOutlayList;

    public int getRecommendedOrderRound() {
        int k = (recommendedOrder + quantum) / quantum;
        if (recommendedOrder % quantum == 0) return recommendedOrder;
        else return quantum * k;
    }

    @Override
    public int compareTo(Item o) {
        return Integer.compare(this.getPlu(), o.getPlu());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return getPlu() == item.getPlu() && getSupplier().equals(item.getSupplier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlu(), getSupplier());
    }
}

