package com.erp.model.wms.dto.third;

import com.common.business.dto.ReceiverDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.enums.WarehouseOperationTypeEnum;
import io.seata.common.util.StringUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ThirdWarehouseCreateFbaOutboundReq extends ThirdWarehouseAuth {

    private String sourceId;
    private String sourceCode;
    private String soCode;
    /**
     * 客户单号，唯一，取ERP的B2B三方发货单单号
     */
    private String referenceNo;
    /**
     * 仓库，取ERP发货通知单的发货仓库映射的三方仓编码
     */
    private String thirdWarehouseCode;
    /**
     * 收货国家，取发货通知的客户的收货国家二字码
     */
    private String receiverCountryCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 派送方式，取发货通知下推时选择的派送方式
     * EXPRESS:渠道订单
     * SELF:自提订单
     * TRUCK:卡车订单
     * TRUCK_SELF:卡车自提
     */
    private String deliveryMethod;
    /**
     * 产品代码(派送方式为【渠道订单】时必填)
     * 取发货通知下推时选择的物流渠道编码
     */
    private String channelCode;

    /**
     * 平台发货号，默认N/A
     */
    private String platformShipNo;

    /**
     * 货件追踪编码，默认N/A
     */
    private String platformRefNo;

    /**
     * 文件URL地址(仅支持jpg、png、gif、zip、pdf的文件)
     */
    private String fileUrl;

    /**
     * 是否为FBA地址 1：是 0：否
     * 默认0
     */
    private Integer fbaAddressFlag = 0;

    /**
     * 取发货通知备注
     */
    private String remark;

    /**
     * 收件地址(fbaAddressFlag为1时必填)
     */
    private String shortName;

    /**
     * 收件人，取发货通知的收件人姓名
     */
    private String receiverName;
    /**
     * 收件人电话，取发货通知的联系电话
     */
    private String telNumber;
    /**
     * 收件人电话拓展，默认为空
     */
    private String telNumberExt;

    /**
     * 收件人省州，取发货通知的省/州
     */
    private String province;

    /**
     * 收件人城市，取发货通知的城市
     */
    private String city;
    /**
     * 收件人地址1，取发货通知的详细地址
     */
    private String address1;
    /**
     * 收件人地址2，
     */
    private String address2;
    /**
     * 收件人地址3，
     */
    private String address3;
    /**
     * 收件人邮编，取发货通知的邮编
     */
    private String postCode;
    /**
     * 收件人门牌号
     */
    private String houseNumber;
    /**
     * 收件人邮箱
     */
    private String email;

    /**
     * 入库单明细
     */
    private List<Item> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {
        private String id;

        private String skuId;
        private String skuNo;
        /**
         * 发货sku
         */
        private String deliverySkuNo;

        /**
         * 发货skuid
         */
        private String deliverySkuId;
        /**
         * 客户商品编码，取发货通知的库存SKU
         */
        private String warehousePlatformSku;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 单箱数量
         */
        private Integer perBoxQty;
        /**
         * 发货箱数
         */
        private Integer boxQty;
        /**
         * 规格编号（ZXGG0001）
         */
        private String boxSpecNo;
    }

    private List<WarehouseOperationTypeDTO> warehouseOperationTypeDTOList;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseOperationTypeDTO {
        /**
         * 指令信息
         */
        /**
         * 仓库操作类型
         * WarehouseOperationTypeEnum
         * 操作指令类型，取发货通知仓库操作类型
         * NO_OPEN_RELABLE：不开箱换SKU标
         * OPEN_RELABLE：开箱换SKU标
         * PASTE_LABEL：贴板标
         * PASTE_PACKAGE：贴箱唛
         * OTHER：其他
         */
        /**
         * zhongbao：
         * ThirdWarehouseOperationDescriptionEnum
         */
        private String warehouseOperationType;
        /**
         * 仓库操作描述
         * 指令描述，例如需开箱换标，请填写：开箱换标，更换标签为：xx
         * 取发货通知仓库操作描述
         */
        private String operationDesc;

        public static List<ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO> convert(String warehouseOperationType, String operationDesc){
            if(StringUtils.isBlank(warehouseOperationType)){
                return new ArrayList<>();
            }
            List<String> splitWarehouseOperationType =  Arrays.asList(warehouseOperationType.split(","));
            List<String> splitOperationDesc =  Arrays.asList(operationDesc.split(","));
            List<ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO> list = new ArrayList<>();
            for (int i = 0; i < splitWarehouseOperationType.size(); i++) {
                String type = splitWarehouseOperationType.get(i);
                String desc = splitOperationDesc.size()<= i ? "" : splitOperationDesc.get(i);
                list.add(new ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO(type,desc));
            }
            return list;
        }
    }
}
