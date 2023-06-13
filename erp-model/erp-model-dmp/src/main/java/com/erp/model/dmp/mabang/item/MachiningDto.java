package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/6/7 17:11
 **/

@Data
@NoArgsConstructor
@ToString
public class MachiningDto {

    private String defaultGridCode;

    private String nameCN;

    private String stockSku;

    private String warehouseName;

    private Integer quantity;

    private Integer warehouseId;

}
