package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class NewProductDTO implements Serializable {
    private String id;

    private String skuNo;

    private LocalDate newListingTime;
}
