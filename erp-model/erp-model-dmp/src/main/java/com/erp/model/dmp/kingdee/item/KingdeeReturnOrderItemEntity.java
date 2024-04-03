package com.erp.model.dmp.kingdee.item;

import cn.hutool.core.annotation.Alias;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class KingdeeReturnOrderItemEntity {
    @JSONField(name ="FBillNo")
    @JsonProperty("FBillNo")
    @Alias("FBillNo")
    private String fBillNo;
    @JSONField(name ="FOrderNo")
    @JsonProperty("FOrderNo")
    @Alias("FOrderNo")
    private String fOrderNo;
    @JSONField(name ="FAmount")
    @JsonProperty("FAmount")
    private String fAmount;
    @JSONField(name ="FMustqty")
    @JsonProperty("FMustqty")
    private String fMustQty;
    @JSONField(name ="FUnitID.FName")
    @JsonProperty("FUnitID.FName")
    private String fUnitName;
    @JSONField(name ="FMaterialId")
    @JsonProperty("FMaterialId")
    private String fMaterialId;
    @JSONField(name ="FMaterialId.FNumber")
    @JsonProperty("FMaterialId.FNumber")
    private String fMaterialNumber;
    @JSONField(name ="FMaterialName")
    @JsonProperty("FMaterialName")
    private String fMaterialName;
    @JSONField(name ="FAuxpropId")
    @JsonProperty("FAuxpropId")
    private String fAuxPropId;
    @JSONField(name ="FMaterialType")
    @JsonProperty("FMaterialType")
    private String fMaterialType;
    @JSONField(name ="FPrice")
    @JsonProperty("FPrice")
    private String fPrice;
    @JSONField(name ="FStockId")
    @JsonProperty("FStockId")
    private String fStockId;
    @JSONField(name ="FStockId.FNumber")
    @JsonProperty("FStockId.FNumber")
    private String fStockNumber;
    @JSONField(name ="FStockId.FName")
    @JsonProperty("FStockId.FName")
    private String fStockName;
    @JSONField(name ="FStockLocId.FF100014.FNumber")
    @JsonProperty("FStockLocId.FF100014.FNumber")
    private String fStockLocId;
    @JSONField(name ="FStockstatusId")
    @JsonProperty("FStockstatusId")
    private String fStockStatusId;
    @JSONField(name ="FNote")
    @JsonProperty("FNote")
    private String fNote;
    @JSONField(name ="FSrcBillNo")
    @JsonProperty("FSrcBillNo")
    private String fSrcBillNo;
    @JSONField(name ="FSrcBillTypeID")
    @JsonProperty("FSrcBillTypeID")
    private String fSrcBillTypeID;
    @JSONField(name ="FIsFree")
    @JsonProperty("FIsFree")
    private String fIsFree;
    @JSONField(name ="FMaterialModel")
    @JsonProperty("FMaterialModel")
    private String fMaterialModel;
    @JSONField(name ="FRealQty")
    @JsonProperty("FRealQty")
    private String fRealQty;
    @JSONField(name ="FSOBILLTYPEID")
    @JsonProperty("FSOBILLTYPEID")
    private String fSoBillTypeId;
    @JSONField(name ="FSalUnitQty")
    @JsonProperty("FSalUnitQty")
    private String fSalUnitQty;
    @JSONField(name ="FProjectNo")
    @JsonProperty("FProjectNo")
    private String fProjectNo;
    @JSONField(name ="F_ulz_KHSKU")
    @JsonProperty("F_ulz_KHSKU")
    private String f_ulz_KHSKU;
    /**
     * 价税合计
     */
    @JSONField(name ="FAllAmount")
    @JsonProperty("FAllAmount")
    private BigDecimal fAllAmount;
    @JSONField(name ="FReturnType")
    @JsonProperty("FReturnType")
    private String fReturnType;
    @JSONField(name ="FSOEntryId")
    @JsonProperty("FSOEntryId")
    private String fSOEntryId;

}

