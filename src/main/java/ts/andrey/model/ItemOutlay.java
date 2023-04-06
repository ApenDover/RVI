package ts.andrey.model;

import javax.persistence.*;
import java.util.Comparator;
import java.util.Objects;


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
    private Item itemPlu;

    public ItemOutlay(int week, int outlayCount, int deliveryCount, Item itemPlu) {
        this.week = week;
        this.outlayCount = outlayCount;
        this.deliveryCount = deliveryCount;
        this.itemPlu = itemPlu;
    }

    public ItemOutlay() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOutlayCount() {
        return outlayCount;
    }

    public void setOutlayCount(int outlayCount) {
        this.outlayCount = outlayCount;
    }

    public int getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(int deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    public Item getItemPlu() {
        return itemPlu;
    }

    public int getItemP(){
        return getItemPlu().getPlu();
    }

    public void setItemPlu(Item itemPlu) {
        this.itemPlu = itemPlu;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    @Override
    public int compareTo(ItemOutlay o) {
        return Comparator.comparing(ItemOutlay::getItemP).thenComparing(ItemOutlay::getWeek).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemOutlay)) return false;
        ItemOutlay that = (ItemOutlay) o;
        return getWeek() == that.getWeek() && getItemPlu().equals(that.getItemPlu());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWeek(), getItemPlu());
    }
}