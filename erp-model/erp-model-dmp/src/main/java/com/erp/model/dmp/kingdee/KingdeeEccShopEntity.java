package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import com.erp.model.dmp.dto.CleanBaseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


/**
 * 网店管理实体类
 */
@NoArgsConstructor
@Data
@ToString
public class KingdeeEccShopEntity  extends CleanBaseDTO {
    @Alias("FId")
    private String id;
    @Alias("FNumber")
    private String fNumber;
    @Alias("FName")
    private String fName;
    @Alias("FSaleOrgId")
    private String fSaleOrgId;
    @Alias("FSaleOrgId.FNumber")
    private String fSaleOrgIdNumber;
    @Alias("FSaleOrgId.FName")
    private String fSaleOrgIdName;
    @Alias("FCreateOrgId")
    private String fCreateOrgId;
    @Alias("FCreateOrgId.FNumber")
    private String fCreateOrgIdNumber;
    @Alias("FCreateOrgId.FName")
    private String fCreateOrgIdName;
    @Alias("FShopType")
    private String fShopType;
    @Alias("FShopName")
    private String fShopName;
    @Alias("FStartDate")
    private String fStartDate;
    @Alias("FSettlementCurrency")
    private String fSettlementCurrency;
    @Alias("FSettlementCurrency.FName")
    private String fSettlementCurrencyName;
    @Alias("FCustomerId")
    private String fCustomerId;
    @Alias("FCustomerId.FNumber")
    private String fCustomerIdNumber;
    @Alias("FCustomerId.FName")
    private String fCustomerIdName;
    @Alias("FSettlementOrgId")
    private String fSettlementOrgId;
    @Alias("FSettlementOrgId.FNumber")
    private String fSettlementOrgIdNumber;
    @Alias("FSettlementOrgId.FName")
    private String fSettlementOrgIdName;
    @Alias("FDownloadByGY")
    private String fDownloadByGY;
    @Alias("FNick")
    private String fNick;
    @Alias("FGYShopType")
    private String fGYShopType;
    @Alias("FGYModifyDate")
    private String fGYModifyDate;
    @Alias("FStockOrgId")
    private String fStockOrgId;
    @Alias("FStockOrgId.FName")
    private String fStockOrgIdName;
    @Alias("FModifierId")
    private String fModifierId;
    @Alias("FModifierId.FName")
    private String fModifierIdName;

    @Alias("FCreatorId.FName")
    private String fCreatorIdName;
    @Alias("FCreateDate")
    private String fCreateDate;
    @Alias("FModifyDate")
    private String fModifyDate;
}
