package com.abhi.Banking_SpringBootWeb_Project.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer user_id;
    private String name;

    @Column(name = "mobile_no" , unique = true)
    private String mobileNo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
