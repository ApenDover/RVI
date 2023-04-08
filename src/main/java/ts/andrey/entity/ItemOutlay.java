package ts.andrey.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import java.util.Comparator;
import java.util.Objects;


@Getter
@Entity
@Table(name = "Item_outlay")
public class ItemOutlay implements Comparable<ItemOutlay> {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(name = "week")
    private int week;
    @Column(name = "outlay_count")
    private int outlayCount;
    @Column(name = "delivery_count")
    private int deliveryCount;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "item_plu", referencedColumnName = "plu")
    private Item item;

    public ItemOutlay(int id, int week, int outlayCount, int deliveryCount, Item item) {
        this.id = id;
        this.week = week;
        this.outlayCount = outlayCount;
        this.deliveryCount = deliveryCount;
        this.item = item;
    }

    public ItemOutlay() {
    }

    @Override
    public int compareTo(ItemOutlay o) {
        return Comparator.comparing(ItemOutlay::getItem).thenComparing(ItemOutlay::getWeek).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemOutlay)) return false;
        ItemOutlay that = (ItemOutlay) o;
        return getWeek() == that.getWeek() && getItem().equals(that.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getWeek(), getItem());
    }
}