package ts.andrey.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Distribution_center")
public class DistributionCenter {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "address")
    private String address;
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "distributionCenterList")
    private List<Item> itemList;

}
