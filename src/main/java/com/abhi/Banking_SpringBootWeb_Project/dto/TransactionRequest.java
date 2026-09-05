package com.abhi.Banking_SpringBootWeb_Project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private long receiverAccountNo;
    private long senderAccountNo;
    private BigDecimal amount;
}
