package com.sdk.wms.zhongbao.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class OutboundB2cCreateRequest implements Serializable {
    //自定义编号
    private String referenceNo;
    //物流跟踪号
    private String trackingNo;
    //拣货类型:1=>一票一件,2=>一票一件多个,3=>一票多件
    private Integer pickType;
    //收件公司名称
    private String companyName;
    //联系人名称
    private String contactName;
    //联系人手机号
    private String contactMobile;
    //联系人分机号
    private String contactCellMobile;
    //联系人邮箱
    private String contactEmail;
    //省份
    private String province;
    //城市
    private String city;
    //区县
    private String district;
    //门牌号
    private String doorplate;
    //地址1
    private String address;
    //邮编
    private String postcode;
    //需要签名服务:-1=>否,1=>是
    private Integer isSign;
    //签名类型:1=>直接签名,2=>间接签名,3=>成人签名
    private Integer signType;
    //需要保险服务:-1=>否,1=>是
    private Integer isInsure;
    //保险金额(USD)
    private Double insurePrice;
    //加急类型:-1=>无,2=>VC订单,3=>Prime订单（B2C）
    private Integer primeType;
    //库存类型:1=>全部,2=>标准,3=>退件优先
    private Integer inventoryType;
    //备注
    private String remark;
    private String address2;
    private String address3;
    //仓库代码
    private String warehouseCode;
    //渠道代码
    private String shippingMethodCode;
    //国家二字码
    private String code2;
    private B2cDto b2cDto;
    private List<Item> itemDTOs;
    private List<Attachment> attachmentOpenDTOs;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class B2cDto {
        //VAT税号（B2C）
        private String vatTaxNo;
        //销售平台（B2C）
        private String salePlatform;
        //平台店铺（B2C）
        private String platformStore;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {
        //产品SKU
        private String productSku;
        //数量
        private Integer qty;
        //箱号（B2B）
        private String boxNo;
        //平台SKU编码（B2B）
        private String platformSku;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Attachment {
        //附件类型:SHIPMENT_LABEL（物流运单）、CARDBOARD_LABEL（卡板唛）、BOX_MARK（箱唛）、BOL、OTHER（其他）
        private String attachmentType;
        private String base64;
        //文件名称
        private String fileName;
    }
}
