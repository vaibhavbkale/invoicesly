package com.vaibhavbkale.invoicesly.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesItem {
    private Integer srNo;
    private String description;
    private String hsnSac;
    private Double quantityKg;
    private Double rate;
    private Integer nos;
    private Double discPercent;
    private Double amount;
}
