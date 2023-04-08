package ts.andrey.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Str {

    private int id;

    private String orderNumber;

    private String supplierName;

    private int plu;

    private String productNameRus;

    private String productNameEng;

    private int orderCount;

    private String price;

    private String regionOfLoading;

    private String weekOfLoading;

    private int weekOfArrival;

    private String etd;

    private String eta;

    private String cifFcaDap;

    private String destination;

    private String purposeOfOrder;

    private String dateOrder;

    private String metkaPromo;

    private String comment;

    private int tz;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Str)) return false;
        Str str = (Str) o;
        return getId() == str.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
