package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.naming.directory.SearchResult;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SkuDetailDTO implements Serializable {

    /**
     * sku
     */
    @NotEmpty(message = "sku不能为空")
    private String skuNo;

}
