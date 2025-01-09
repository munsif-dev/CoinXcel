package com.munsif.CoinXcel.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.munsif.CoinXcel.Domain.USER_ROLE;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;


@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String fullname;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Embedded
    private TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
    private USER_ROLE role = USER_ROLE.COSTUMER;


}
