package com.vaibhavbkale.invoicesly.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseItem {
    private String itemName;
    private Double weightKg;
    private Double pricePerKg;
    private Double gstPercent;
    private Double shortageKg;
    private Double chotariPercent;
    private Double weightCharges;
}
