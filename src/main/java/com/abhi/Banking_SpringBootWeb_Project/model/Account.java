package com.abhi.Banking_SpringBootWeb_Project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private int user_id;

    @Column(name = "account_no")
    private long accountNo;
    private BigDecimal balance = BigDecimal.valueOf(0.00);

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

}
