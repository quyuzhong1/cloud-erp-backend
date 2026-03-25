package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

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
public class OverseasInboundCreateRequest implements Serializable {
    //订单号
    private String orderNo;
    //自定义编号
    private String referenceNo;
    //派送方式:1=>客户自发头程,2=>海外仓头程,3=>海外仓码头提货
    private Integer transitType;
    //目的仓库代码
    private String warehouseCode;
    //来源仓库代码(转仓入库)
    private String fromWarehouseCode;
    //运输方式:1=>整柜,2=>快递,3=>托盘
    private Integer shippingType;
    //柜型:-1=>无,1=>40HQ,2=>45HQ,3=>20GP,:4=>40GP,5=>45PLWD,6=>53HQ(运输方式shipping_type是整柜时必填)),
    private Integer containerType;
    //柜号
    private String containerNo;
    //上架方式:1=>按箱上架,2=>按PCS上架
    private Integer shelfMode;
    //物流跟踪号
    private String trackingNo;
    //入库单描述
    private String asnDesc;
    //1=>常规,2=>FBA退货,4=>快进快出
    private Integer orderType;
    //是否使用托盘入仓:1=>是,-1=>否
    private Integer isUsePallet;
    //是否混托:1=>是,-1=>否
    private Integer isMixedPallet;
    //托盘数量
    private Integer palletQty;
    //承运商代码
    private String carrierCode;
    //预计到达日期
    private String etaDate;
    //卸货方式:1=>Drop off,2=>Live unload
    private Integer unloadType;
    //还柜时间
    private String returnContainerDate;
    //库存属性:1=>普通,2=>预销(货柜内产品在平台已预售,到仓库后需快速发出)
    private Integer stockType;
    //拖柜通知邮箱
    private String emailOfTowingContainer;
    //备注
    private String remark;
    private List<Item> itemDTOs;
    private List<Attachment> attachmentOpenDTOs;

    @Data
    @Builder
    public static class Item {
        //产品SKU
        private String productSku;
        //箱号
        private String boxNo;
        //预报数量
        private Integer qty;
        //长(CM)
        private Double length;
        //宽(CM)
        private Double width;
        //高(CM)
        private Double height;
        //重量(KG)
        private Double weight;
        //包袋类型:1=>自带包装,2=>非自带包装
        private Integer packageType;
        //是否拍照:1=>是(退货单),-1=>否
        private String isPicture;
        //是否混箱:1=>是,-1=>否
        private String isMixedBox;
        //备注
        private String remark;
    }

    @Data
    @Builder
    public static class Attachment {
        //ITEM_LABEL（产品标签）、PALLETI_LABEL（托盘标签）、SHIPPING_LABEL（物流运单）、PACKING_LIST（装箱单）、BOL、CARTON_LABEL（纸箱标签）、OTHER（其他）
        private String attachmentType;
        private String base64;
        //文件名称
        private String fileName;
    }
}
