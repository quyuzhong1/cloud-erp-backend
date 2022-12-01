package com.erp.server.dmp.entity.mabang;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class ShopEntity {
    private String id;
    private String accountUsername;
    private String accountStoreName;
    private String name;
    private String amazonsite;
    private Integer status;
    private String datacenter_tokenid;
    private String shopEmployeeId;
    private String financeCode;
    private String platformId;
    private String platformName;
    private String merchantid;
    private String tokenflag;
    private String fbaflag;
}
