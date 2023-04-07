package ts.andrey.entity;

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

    @OneToMany(mappedBy = "itemPlu", cascade = CascadeType.ALL)
    private List<ItemOutlay> itemOutlayList;

    public Item(int plu, Supplier supplier, String status, String name, int quantum, int countStore, Integer orderWeek, Integer deliveryWeek, Integer recommendedOrder, int stockDC, int stockStore, boolean promo, List<DistributionCenter> distributionCenterList, List<ItemOutlay> itemOutlayList) {
        this.plu = plu;
        this.supplier = supplier;
        this.status = status;
        this.name = name;
        this.quantum = quantum;
        this.countStore = countStore;
        this.orderWeek = orderWeek;
        this.deliveryWeek = deliveryWeek;
        this.recommendedOrder = recommendedOrder;
        this.stockDC = stockDC;
        this.stockStore = stockStore;
        this.promo = promo;
        this.distributionCenterList = distributionCenterList;
        this.itemOutlayList = itemOutlayList;
    }

    public Item() {
    }

    public int getPlu() {
        return plu;
    }

    public void setPlu(int plu) {
        this.plu = plu;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public int getCountStore() {
        return countStore;
    }

    public void setCountStore(int countStore) {
        this.countStore = countStore;
    }

    public int getOrderWeek() {
        if (orderWeek == null) return 0;
        else return orderWeek;
    }

    public void setOrderWeek(int orderWeek) {
        this.orderWeek = orderWeek;
    }

    public int getDeliveryWeek() {
        if (deliveryWeek == null) return 0;
        else return deliveryWeek;
    }

    public void setDeliveryWeek(int deliveryWeek) {
        this.deliveryWeek = deliveryWeek;
    }

    public int getStockDC() {
        return stockDC;
    }

    public void setStockDC(int stockDC) {
        this.stockDC = stockDC;
    }

    public int getStockStore() {
        return stockStore;
    }

    public void setStockStore(int stockStore) {
        this.stockStore = stockStore;
    }

    public List<DistributionCenter> getDistributionCenterList() {
        return distributionCenterList;
    }

    public void setDistributionCenterList(List<DistributionCenter> distributionCenterList) {
        this.distributionCenterList = distributionCenterList;
    }

    public List<ItemOutlay> getItemOutlayList() {
        return itemOutlayList;
    }

    public void setItemOutlayList(List<ItemOutlay> itemOutlayList) {
        this.itemOutlayList = itemOutlayList;
    }

    public int getRecommendedOrderRound() {
        int k = (recommendedOrder + quantum) / quantum;
        if (recommendedOrder % quantum == 0) return recommendedOrder;
        else return quantum * k;
    }

    public Integer getRecommendedOrder() {
        return recommendedOrder;
    }

    public void setRecommendedOrder(int recommendedOrder) {
        this.recommendedOrder = recommendedOrder;
    }

    public void setOrderWeek(Integer orderWeek) {
        this.orderWeek = orderWeek;
    }

    public void setDeliveryWeek(Integer deliveryWeek) {
        this.deliveryWeek = deliveryWeek;
    }

    public void setRecommendedOrder(Integer recommendedOrder) {
        this.recommendedOrder = recommendedOrder;
    }

    public boolean isPromo() {
        return promo;
    }

    public void setPromo(boolean promo) {
        this.promo = promo;
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

