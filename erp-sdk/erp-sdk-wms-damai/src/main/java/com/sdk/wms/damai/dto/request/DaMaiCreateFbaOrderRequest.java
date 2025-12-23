package com.sdk.wms.damai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DaMaiCreateFbaOrderRequest {

    //客户单号。唯一
    @NotBlank(message = "客户单号不能为空")
    @JsonProperty("custRefNo")
    private String custRefNo;
    //仓库
    @NotBlank(message = "仓库不能为空")
    @JsonProperty("whCode")
    private String whCode;
    //收货国家
    @NotBlank(message = "收货国家不能为空")
    @JsonProperty("consigneeCountryCode")
    private String consigneeCountryCode;
    /**
     * 派送方式
     * EXPRESS:渠道订单
     * SELF:自提订单
     * TRUCK:卡车订单
     * TRUCK_SELF:卡车自提
     */
    @NotBlank(message = "派送方式不能为空")
    @JsonProperty("deliverType")
    private String deliverType;
    //产品代码(派送方式为【渠道订单】时必填)
    @JsonProperty("carriersCode")
    private String carriersCode;
    //平台发货号
    @NotBlank(message = "平台发货号不能为空")
    @JsonProperty("platformShipNo")
    private String platformShipNo;
    //货件追踪编码
    @NotBlank(message = "货件追踪编码不能为空")
    @JsonProperty("platformRefNo")
    private String platformRefNo;
    //文件URL地址(仅支持jpg、png、gif、zip、pdf的文件)
    @NotBlank(message = "文件URL地址不能为空")
    @JsonProperty("fileUrl")
    private String fileUrl;
    //是否为FBA地址 1：是 0：否
    @NotBlank(message = "是否为FBA地址不能为空")
    @JsonProperty("fbaAddressFlag")
    private Integer fbaAddressFlag;
    //	收件地址(fbaAddressFlag为1时必填)
    @JsonProperty("shortName")
    private String shortName;
    //收件人
    @NotBlank(message = "收件人不能为空")
    @JsonProperty("consigneeName")
    private String consigneeName;
    //收件人电话
    @JsonProperty("consigneeTel")
    private String consigneeTel;
    //收件人电话拓展
    @JsonProperty("consigneeTelExt")
    private String consigneeTelExt;
    //收件人省州
    @JsonProperty("consigneeProvince")
    private String consigneeProvince;
    //收件人城市
    @JsonProperty("consigneeCity")
    private String consigneeCity;
    //收件人地址1
    @NotBlank(message = "收件人地址1不能为空")
    @JsonProperty("consigneeAddress1")
    private String consigneeAddress1;
    //收件人地址2
    @JsonProperty("consigneeAddress2")
    private String consigneeAddress2;
    //收件人地址3
    @JsonProperty("consigneeAddress3")
    private String consigneeAddress3;
    //收件人邮编
    @JsonProperty("consigneePostalCode")
    private String consigneePostalCode;
    //收件门牌号
    @JsonProperty("consigneeHouseNumber")
    private String consigneeHouseNumber;
    //收件人邮箱
    @JsonProperty("consigneeEmail")
    private String consigneeEmail;
    //平台店铺名称
    @JsonProperty("shopName")
    private String shopName;
    //平台店铺备注
    @JsonProperty("shopRemark")
    private String shopRemark;
    //单据备注
    @JsonProperty("remark")
    private String remark;
    //订单sku
    @NotEmpty(message = "订单sku不能为空")
    @JsonProperty("skuList")
    private List<SkuListDTO> skuList;
    //指令信息
    @NotEmpty(message = "指令信息不能为空")
    @JsonProperty("commandList")
    private List<CommandDTO> commandList;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class CommandDTO {
        /**
         * 操作指令类型:
         * NO_OPEN_RELABLE：不开箱换SKU标
         * OPEN_RELABLE：开箱换SKU标
         * PASTE_LABEL：贴板标
         * PASTE_PACKAGE：贴箱唛
         * OTHER：其他
         */
        @JsonProperty("commandType")
        private String commandType;
        /**
         * 指令描述，例如需开箱换标，请填写：开箱换标，更换标签为：xx
         */
        @JsonProperty("commandDesc")
        private String commandDesc;
        /**
         * 指令备注
         */
        @JsonProperty("commandRemark")
        private String commandRemark;
    }



    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class SkuListDTO {
        //客户商品编码
        @JsonProperty("custSkuCode")
        private String custSkuCode;
        //单箱商品数量
        @JsonProperty("skuQty")
        private Integer skuQty;
        //客户箱号
        @JsonProperty("custPackageNo")
        private String custPackageNo;
        //箱数
        @JsonProperty("packQty")
        private Integer packQty;
        //批次号
        @JsonProperty("custLotNo")
        private String custLotNo;
        //商品sn
        @JsonProperty("sn")
        private String sn;
    }
}
