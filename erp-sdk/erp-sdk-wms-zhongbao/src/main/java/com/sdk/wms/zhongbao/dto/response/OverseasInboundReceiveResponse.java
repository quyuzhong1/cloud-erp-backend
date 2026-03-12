package com.sdk.wms.zhongbao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundReceiveResponse
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverseasInboundReceiveResponse extends PageResponse{
    //数据列表
    private List<OverseasInboundReceive> list;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OverseasInboundReceive {
        //批次号
        private String batchNo;
        //关联订单号
        private String orderNo;
        //业务类型:1=>库存初始化,2=>入库上架,3=>订单锁定,4=>订单取消,5=>订单出库,6=>退件上架,7=>人工调整,8=>人工调整撤销
        private Integer bizType;
        //操作类型:1=>增加,2=>其他,-1=>减少
        private Integer optType;
        //操作数量
        private Integer optQty;
        //结余数量
        private Integer balanceQty;
        //可售数量
        private Integer saleQty;
        //锁定数量
        private Integer lockQty;
        //次品数量
        private Integer badQty;
        //库存总数量
        private Integer totalQty;
        //入库数量
        private Integer asnQty;
        //出库数量
        private Integer outQty;
        //退货数量
        private Integer returnQty;
        //良品退货数量
        private Integer goodReturnQty;
        //次品退货数量
        private Integer badReturnQty;
        //销毁数量
        private Integer destroyQty;
        //转移数量
        private Integer transferQty;
        //货值(USD)
        private Double totalPrice;
        //周转天数
        private Integer turnoverDays;
        //库存状态:1=>未出库,2=>部分出库,3=>全部出库
        private Integer status;
        //创建时间
        private String createTime;
        private Warehouse openWarehouse;
        private Product openProduct;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Warehouse {
        //仓库代码
        private String warehouseCode;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Product {
        //产品SKU
        private String productSku;
        //产品名称
        private String name;
        //英文名称
        private String nameEn;
    }
}
