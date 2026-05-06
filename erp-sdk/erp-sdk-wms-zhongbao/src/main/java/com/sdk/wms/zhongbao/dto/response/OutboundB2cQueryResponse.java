package com.sdk.wms.zhongbao.dto.response;

import lombok.*;

import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboundB2cQueryResponse extends PageResponse {

    private List<OutboundB2cQueryResponse.Query> list;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OpenB2c {
        //VAT税号（B2C）
        private String vatTaxNo;
        //销售平台（B2C）
        private String salePlatform;
        //平台店铺（B2C）
        private String platformStore;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Query {
        //订单类型:1=>B2C,2=>B2B
        private Integer orderType;
        //订单号
        private String orderNo;
        //自定义编号
        private String referenceNo;
        //物流跟踪号
        private String trackingNo;
        //拣货类型:1=>一票一件,2=>一票一件多个,3=>一票多件
        private Integer pickType;
        //公司名称
        private String companyName;
        //联系人名称
        private String contactName;
        //联系人电话
        private String contactMobile;
        //联系人分机号
        private String contactCellMobile;
        //联系人邮箱
        private String contactEmail;
        //州/省
        private String province;
        //城市
        private String city;
        //区/县
        private String district;
        //收件人门牌号
        private String doorplate;
        //详细地址
        private String address;
        //邮编
        private String postcode;
        //需要签名服务:-1=>否,1=>是
        private Integer isSign;
        //需要保险服务:-1=>否,1=>是
        private Integer isInsure;
        //保险金额(USD)
        private Double insurePrice;
        //加急类型:-1=>无,2=>VC订单,3=>Prime订单（B2C）
        private Integer primeType;
        //库存类型:1=>全部,2=>标准,3=>退件优先
        private Integer inventoryType;
        //产品总数
        private String totalProduct;
        //总重量(KG)
        private Double totalWeight;
        //订单出库时间
        private String outboundTime;
        //状态:-2=>异常,-1=>已取消,1=>草稿,2=>待审核,3=>已审核,4=>待出库,5=>已出库
        private Integer status;
        //订单异常原因
        private String errorReason;
        private OpenB2c openB2c;
        private List<Item> openItems;
        private List<Attachment> openAttachments;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        //产品SKU
        private String productSku;
        //申报名称
        private String declareName;
        //申报英文名称
        private String declareNameEn;
        //申报价值(USD)
        private String declareValue;
        //数量
        private String qty;
        //重量(KG)
        private String weight;
        //箱号（B2B）
        private String boxNo;
        //平台SKU编码（B2B）
        private String platformSku;
        //贴标数量（B2B）
        private String pasteQty;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attachment {
        //附件类型
        private String attachmentType;
        //文件类型(PNG/JPG/JPEG)
        private String fileType;
        private String base64;
        //文件名称
        private String fileName;
    }
}
