package ts.andrey.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "OrderRvi")
public class OrderRvi {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "plu_number")
    private int plu;

    @Column(name = "product_name_rus")
    private String productNameRus;

    @Column(name = "product_name_eng")
    private String productNameEng;

    @Column(name = "order_count")
    private int orderCount;

    @Column(name = "price")
    private String price;

    @Column(name = "region_of_loading")
    private String regionOfLoading;

    @Column(name = "Week_of_loading")
    private String weekOfLoading;

    @Column(name = "Week_of_Arrival")
    private int weekOfArrival;

    @Column(name = "ETD")
    private String etd;

    @Column(name = "ETA")
    private String eta;

    @Column(name = "CIF_FCA_DAP")
    private String cifFcaDap;

    @Column(name = "Destination")
    private String destination;

    @Column(name = "purpose_of_order")
    private String purposeOfOrder;

    @Column(name = "date_Order")
    private String dateOrder;

    @Column(name = "metka_promo")
    private String metkaPromo;

    @Column(name = "comment")
    private String comment;

    @Column(name = "tovar_zapas")
    private int tz;

    public OrderRvi() {
    }

    public OrderRvi(int id, String orderNumber, String supplierName, int plu, String productNameRus, String productNameEng, int orderCount, String price, String regionOfLoading, String weekOfLoading, int weekOfArrival, String etd, String eta, String cifFcaDap, String destination, String purposeOfOrder, String dateOrder, String metkaPromo, String comment, int tz) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.supplierName = supplierName;
        this.plu = plu;
        this.productNameRus = productNameRus;
        this.productNameEng = productNameEng;
        this.orderCount = orderCount;
        this.price = price;
        this.regionOfLoading = regionOfLoading;
        this.weekOfLoading = weekOfLoading;
        this.weekOfArrival = weekOfArrival;
        this.etd = etd;
        this.eta = eta;
        this.cifFcaDap = cifFcaDap;
        this.destination = destination;
        this.purposeOfOrder = purposeOfOrder;
        this.dateOrder = dateOrder;
        this.metkaPromo = metkaPromo;
        this.comment = comment;
        this.tz = tz;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public int getPlu() {
        return plu;
    }

    public void setPlu(int plu) {
        this.plu = plu;
    }

    public String getProductNameRus() {
        return productNameRus;
    }

    public void setProductNameRus(String productNameRus) {
        this.productNameRus = productNameRus;
    }

    public String getProductNameEng() {
        return productNameEng;
    }

    public void setProductNameEng(String productNameEng) {
        this.productNameEng = productNameEng;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRegionOfLoading() {
        return regionOfLoading;
    }

    public void setRegionOfLoading(String regionOfLoading) {
        this.regionOfLoading = regionOfLoading;
    }

    public String getWeekOfLoading() {
        return weekOfLoading;
    }

    public void setWeekOfLoading(String weekOfLoading) {
        this.weekOfLoading = weekOfLoading;
    }

    public int getWeekOfArrival() {
        return weekOfArrival;
    }

    public void setWeekOfArrival(int weekOfArrival) {
        this.weekOfArrival = weekOfArrival;
    }

    public String getEtd() {
        return etd;
    }

    public void setEtd(String etd) {
        this.etd = etd;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getCifFcaDap() {
        return cifFcaDap;
    }

    public void setCifFcaDap(String cifFcaDap) {
        this.cifFcaDap = cifFcaDap;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPurposeOfOrder() {
        return purposeOfOrder;
    }

    public void setPurposeOfOrder(String purposeOfOrder) {
        this.purposeOfOrder = purposeOfOrder;
    }

    public String getDateOrder() {
        return dateOrder;
    }

    public void setDateOrder(String dateOrder) {
        this.dateOrder = dateOrder;
    }

    public String getMetkaPromo() {
        return metkaPromo;
    }

    public void setMetkaPromo(String metkaPromo) {
        this.metkaPromo = metkaPromo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getTz() {
        return tz;
    }

    public void setTz(int tz) {
        this.tz = tz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderRvi)) return false;
        OrderRvi orderRvi = (OrderRvi) o;
        return getPlu() == orderRvi.getPlu() && getDateOrder().equals(orderRvi.getDateOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlu(), getDateOrder());
    }
}
