package dat3.apicommunication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NameInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String country;
    private int age;
    private String gender;

    public NameInfo(String name, String country, int age, String gender) {
        this.name = name;
        this.country = country;
        this.age = age;
        this.gender = gender;
    }
}
