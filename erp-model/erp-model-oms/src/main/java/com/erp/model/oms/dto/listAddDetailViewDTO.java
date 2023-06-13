package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class listAddDetailViewDTO implements Serializable {
    /**
     * 表id
     */
    private String id;

    /**
     * sku编号
     */
    private List<String> skuNoList;

}
