package ts.andrey.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderRvi)) return false;
        OrderRvi orderRvi = (OrderRvi) o;
        return getPlu() == orderRvi.getPlu() && getOrderCount() == orderRvi.getOrderCount() && getWeekOfArrival() == orderRvi.getWeekOfArrival() && getTz() == orderRvi.getTz() && Objects.equals(getOrderNumber(), orderRvi.getOrderNumber()) && Objects.equals(getSupplierName(), orderRvi.getSupplierName()) && Objects.equals(getProductNameRus(), orderRvi.getProductNameRus()) && Objects.equals(getProductNameEng(), orderRvi.getProductNameEng()) && Objects.equals(getPrice(), orderRvi.getPrice()) && Objects.equals(getRegionOfLoading(), orderRvi.getRegionOfLoading()) && Objects.equals(getWeekOfLoading(), orderRvi.getWeekOfLoading()) && Objects.equals(getEtd(), orderRvi.getEtd()) && Objects.equals(getEta(), orderRvi.getEta()) && Objects.equals(getCifFcaDap(), orderRvi.getCifFcaDap()) && Objects.equals(getDestination(), orderRvi.getDestination()) && Objects.equals(getPurposeOfOrder(), orderRvi.getPurposeOfOrder()) && Objects.equals(getDateOrder(), orderRvi.getDateOrder()) && Objects.equals(getMetkaPromo(), orderRvi.getMetkaPromo()) && Objects.equals(getComment(), orderRvi.getComment());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderNumber(), getSupplierName(), getPlu(), getProductNameRus(), getProductNameEng(), getOrderCount(), getPrice(), getRegionOfLoading(), getWeekOfLoading(), getWeekOfArrival(), getEtd(), getEta(), getCifFcaDap(), getDestination(), getPurposeOfOrder(), getDateOrder(), getMetkaPromo(), getComment(), getTz());
    }
}
