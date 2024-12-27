package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OtherHistorySaleQtyDTO {

    private LocalDate orderDate;

    private String platform;

    private String shopId;

    private String skuNo;

    private Integer qty;
}
