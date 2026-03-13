package com.sdk.wms.zhongbao.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/10 10:56
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
public class OverseasOutboundCreateRequest {

    /**
     * 自定义编号
     */
    @JSONField(name = "referenceNo")
    private String referenceNo;

    /**
     * 拣货类型
     */
    @JSONField(name = "pickType")
    private Integer  pickType;

    /**
     *联系人姓名
     */
    @JSONField(name = "contactName")
    private String contactName;

    /**
     *联系人电话
     */
    @JSONField(name = "contactMobile")
    private String contactMobile;

    /**
     *联系人邮箱
     */
    @JSONField(name = "contactEmail")
    private String contactEmail;

    /**
     *州/省
     */
    @JSONField(name = "province")
    private String province;

    /**
     *城市
     */
    @JSONField(name = "city")
    private String city;

    /**
     *详细地址
     */
    @JSONField(name = "address")
    private String address;

    /**
     *地址2
     */
    @JSONField(name = "address2")
    private String address2;

    /**
     *地址3
     */
    @JSONField(name = "address3")
    private String address3;

    /**
     *邮编
     */
    @JSONField(name = "postcode")
    private String postcode;

    /**
     *是否签名服务 -1=>否,1=>是
     */
    @JSONField(name = "isSign")
    private Integer  isSign;

    /**
     *是否保险服务 -1=>否,1=>是
     */
    @JSONField(name = "isInsure")
    private Integer isInsure;

    /**
     *保险金额usd
     */
    @JSONField(name = "insurePrice")
    private BigDecimal insurePrice;

    /**
     *备注
     */
    @JSONField(name = "remark")
    private String remark;

    /**
     *仓库代码
     */
    @JSONField(name = "warehouseCode")
    private String warehouseCode;

    /**
     *渠道代码
     */
    @JSONField(name = "shippingMethodCode")
    private String shippingMethodCode;

    /**
     *国家二字码
     */
    @JSONField(name = "code2")
    private String code2;

    /**
     *仓库操作指令
     */
    @JSONField(name = "b2bDto")
    private B2bDto b2bDto;

    /**
     * 订单明细
     */
    @JSONField(name = "itemDTOs")
    @NotNull(message = "订单明细不能为空")
    private List<ItemDTOs> itemDTOs;

    /**
     * 附件列表
     */
    @JSONField(name = "attachmentOpenDTOs")
    private List<attachmentOpenDTOs> attachmentOpenDTOs;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ItemDTOs {

        /**
         *产品SKU
         */
        @JSONField(name = "productSku")
        private String productSku;

        /**
         *数量
         */
        @JSONField(name = "qty")
        private Integer qty;

        /**
         *箱号
         */
        @JSONField(name = "boxNo")
        private String boxNo;

        /**
         *平台SKU编码
         */
        @JSONField(name = "platformSku")
        private String platformSku;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class attachmentOpenDTOs {

        /**
         *文件
         */
        @JSONField(name = "base64")
        private String base64;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class B2bDto {

        /**
         *货件编号
         */
        @JSONField(name = "shipmentNo")
        private String shipmentNo;

        /**
         *批次提货单号
         */
        @JSONField(name = "batchBolNo")
        private String batchBolNo;

        /**
         *是否更换包装
         */
        @JSONField(name = "isChangePackage")
        private Integer isChangePackage;

        /**
         *换条码类型
         */
        @JSONField(name = "changeBarcodeType")
        private Integer changeBarcodeType;

        /**
         *是否覆盖条码
         */
        @JSONField(name = "isCoverBarcode")
        private Integer isCoverBarcode;

        /**
         *换箱唛类型
         */
        @JSONField(name = "changeShippingMarkType")
        private Integer changeShippingMarkType;

        /**
         *是否覆盖箱唛
         */
        @JSONField(name = "isCoverShippingMark")
        private Integer isCoverShippingMark;

        /**
         *是否打托
         */
        @JSONField(name = "isPallet")
        private Integer isPallet;

        /**
         *是否双板打托
         */
        @JSONField(name = "isDoublePallet")
        private Integer isDoublePallet;

        /**
         *是否混托
         */
        @JSONField(name = "isMixedPallet")
        private Integer isMixedPallet;

        /**
         *贴托唛类型
         */
        @JSONField(name = "pasteCartonMarkType")
        private Integer pasteCartonMarkType;

        /**
         *限板数
         */
        @JSONField(name = "limitPlateNum")
        private Integer limitPlateNum;

        /**
         *限板高cm
         */
        @JSONField(name = "limitPlateHeight")
        private BigDecimal limitPlateHeight;

        /**
         *限板重kg
         */
        @JSONField(name = "limitPlateWeight")
        private BigDecimal limitPlateWeight;

        /**
         *是否主动SKU打托方案
         */
        @JSONField(name = "isPalletScheme")
        private Integer isPalletScheme;

    }
}
