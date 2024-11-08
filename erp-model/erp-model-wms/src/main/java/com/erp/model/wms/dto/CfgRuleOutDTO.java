package com.erp.model.wms.dto;

import com.common.core.anno.StateEnumValue;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.enums.PickingSourceTypeEnum;
import com.common.core.entity.ConditionElement;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 出库配置规则请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
 */
@Data
@NoArgsConstructor
public class CfgRuleOutDTO implements Serializable {

    /**
     * 设备分拣口配置
     */
    @Data
    @NoArgsConstructor
    public static class EquipmentSortingPortConditionDTO {
        /**
         * 设置类型 wms/common/enumDropDown?type=EquipmentSortingPortType
         */
        private String type;

        /**
         * 比较符 wms/common/enumDropDown?type=EquipmentSortingPortCompare
         */
        private String compare;

        /**
         * 对应值
         */
        private List<String> valueList = new ArrayList<>();

        /**
         * 分拣口 wms/dict/drop/down?type=equipmentSortingPort
         */
        private String port;
    }

    /**
     * 设备分拣口配置
     */
    @Data
    @NoArgsConstructor
    public static class EquipmentSortingPortDTO {
        /**
         * 设备分拣口配置条件
         */
        private List<EquipmentSortingPortConditionDTO> sortingConditionDTOList = new ArrayList<>();
    }


    /**
     * B2C称重量方允许偏差配置
     */
    @Data
    @NoArgsConstructor
    public static class B2cAllowableDeviationsConditionDetail {
        /**
         * 左括号
         */
        @Size(max = 10, message = "左括号最大长度不能超过10位")
        private String leftBracket;

        /**
         * 条件的字段 wms/common/enumDropDown?type=AllowableDeviationsCondition
         */
        @NotBlank(message = "条件的字段不能为空")
        private String field;

        private String fieldName;

        /**
         * 下拉逻辑关系 wms/common/enumDropDown?type=AllowableDeviationsCompare
         */
        @NotBlank(message = "比较符不能为空")
        @Size(max = 30, message = "比较符最大长度不能超过30位")
        private String compare;

        /**
         * 对应的值
         */
        @NotBlank(message = "对应的值不能为空")
        @Size(max = 30, message = "对应的值最大长度不能超过30位")
        private String value;

        /**
         * 右括号
         */
        @Size(max = 10, message = "右括号最大长度不能超过10位")
        private String rightBracket;

        /**
         * 逻辑关系 or 和 and 下拉逻辑关系 wms/common/enumDropDown?type=AllowableDeviationsLogic
         */
        @StateEnumValue(strValues = {"or", "and"}, message = "逻辑关系有误")
        private String logic;

        /**
         * 序号
         */
        private Integer index;
    }
    /**
     * B2C称重量方允许偏差配置
     */
    @Data
    @NoArgsConstructor
    public static class B2cAllowableDeviationsCondition {

        /**
         * 值
         */
        private List<String> valList = new ArrayList<>();

        /**
         * 条件明细
         */
        private List<B2cAllowableDeviationsConditionDetail> conditionDetailList = new ArrayList<>();
    }
    /**
     * B2C称重量方允许偏差配置
     */
    @Data
    @NoArgsConstructor
    public static class B2cAllowableDeviations {

        /**
         * 设置类型 wms/dict/drop/down?type=AllowableDeviationsType
         */
        private String type;

        /**
         * 配置条件
         */
        private List<B2cAllowableDeviationsCondition> conditionDTOList = new ArrayList<>();

        /**
         * 为0正常出库开关
         */
        private boolean whenZeroNormalOutSwitch;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortingPortResultDTO {
        private String port;
        private Boolean updateError;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OverweightDTO {

        /**
         * 类型
         */
        private PickingSourceTypeEnum type;

        /**
         * 称重重量（kg）
         */
        private BigDecimal scanWeight;

        /**
         * 扫描长(cm)
         */
        private BigDecimal scanLength;

        /**
         * 扫描宽(cm)
         */
        private BigDecimal scanWidth;

        /**
         * 扫描高(cm)
         */
        private BigDecimal scanHeight;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortingPortRuleDTO {

        /**
         * 物流商id
         */
        @NotBlank(message = "物流商id不能为空")
        private String logisticsSupplierId;

        /**
         * 渠道id
         */
        @NotBlank(message = "渠道id不能为空")
        private String channelId;

        /**
         * 发货单id
         */
        @NotBlank(message = "发货单id不能为空")
        private String deliveryOrderId;

        /**
         * 称重重量（g）
         */
        private BigDecimal scanWeight;

        /**
         * 扫描长(cm)
         */
        private BigDecimal scanLength;

        /**
         * 扫描宽(cm)
         */
        private BigDecimal scanWidth;

        /**
         * 扫描高(cm)
         */
        private BigDecimal scanHeight;

        /**
         * 订单重量（g）
         */
        private BigDecimal orderWeight;

        /**
         * 订单包装长(cm)
         */
        private BigDecimal orderLength;

        /**
         * 订单包装宽(cm)
         */
        private BigDecimal orderWidth;

        /**
         * 订单包装高(cm)
         */
        private BigDecimal orderHeight;

        public void handleNullToZero(){
            this.setScanWeight(Objects.isNull(this.getScanWeight()) ? BigDecimal.ZERO : this.getScanWeight());
            this.setScanLength(Objects.isNull(this.getScanLength()) ? BigDecimal.ZERO : this.getScanLength());
            this.setScanWidth(Objects.isNull(this.getScanWidth()) ? BigDecimal.ZERO : this.getScanWidth());
            this.setScanHeight(Objects.isNull(this.getScanHeight()) ? BigDecimal.ZERO : this.getScanHeight());
            this.setOrderWeight(Objects.isNull(this.getOrderWeight()) ? BigDecimal.ZERO : this.getOrderWeight());
            this.setOrderLength(Objects.isNull(this.getOrderLength()) ? BigDecimal.ZERO : this.getOrderLength());
            this.setOrderWidth(Objects.isNull(this.getOrderWidth()) ? BigDecimal.ZERO : this.getOrderWidth());
            this.setOrderHeight(Objects.isNull(this.getOrderHeight()) ? BigDecimal.ZERO : this.getOrderHeight());
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfgOverweightDetailDTO {

        /**
         * 分类  wms/common/enumDropDown?type=OverweightType
         */
        @NotBlank(message = "装箱超重配置-分类不能为空")
        private String overweightType;

        /**
         * 单箱超重重量（kg）
         */
        @DecimalMin(value = "0.00", message = "单箱超重重量不能为负数")
        private BigDecimal maxWeight;
        /**
         * 单箱最低重量（kg）
         */
        @DecimalMin(value = "0.00", message = "单箱最低重量不能为负数")
        private BigDecimal minWeight;
        /**
         * 单箱最大尺寸长(cm)
         */
        @DecimalMin(value = "0.00", message = "单箱最大尺寸长不能为负数")
        private BigDecimal maxLength;
        /**
         * 单箱最大尺寸宽(cm)
         */
        @DecimalMin(value = "0.00", message = "单箱最大尺寸宽不能为负数")
        private BigDecimal maxWidth;
        /**
         * 单箱最大尺寸高(cm)
         */
        @DecimalMin(value = "0.00", message = "单箱最大尺寸高不能为负数")
        private BigDecimal maxHeight;
        /**
         * 单箱最大周长(cm)
         */
        @DecimalMin(value = "0.00", message = "单箱最大周长不能为负数")
        private BigDecimal maxCirc;

        /**
         * 超重允许出库
         */
        private boolean greaterThanWeightCanOut;

        /**
         * 低于重量允许出库
         */
        private boolean lessThanWeightCanOut;

        /**
         * 超尺寸允许出库
         */
        private boolean sizeNotPassCanOut;

        /**
         * 单据审核设置校验状态
         */
        private boolean checkStatusWhenApprove = true;

        public void check(){
            if(Objects.nonNull(maxWeight) && Objects.nonNull(minWeight) && maxWeight.compareTo(minWeight)<0){
                throw new ServiceException("单箱超重重量不可小于最低重量");
            }
        }
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfgOverweightDTO {

        /**
         * 装箱配置明细
         */
        @Valid
        private List<CfgOverweightDetailDTO> cfgOverweightDetailDTOList = new ArrayList<>();

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfgProductPackingDetail {

        /**
         * 分类  wms/common/enumDropDown?type=OverweightType
         */
        private String overweightType;

        /**
         * 装入的产品属性id /plm/dict/list?type=declareProperty
         */
        private List<String> canPackingPropertyIds = new ArrayList<>();

        /**
         * 不可装入的产品属性id /plm/dict/list?type=declareProperty
         */
        private List<String> cannotPackingPropertyIds = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfgProductPacking {
        /**
         * 产品装箱配置详情
         */
        private List<CfgProductPackingDetail> cfgProductPackingDetailList = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class CheckDTO {

        private Boolean result;

        private String msg;
    }
    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 设备分拣口
         */
        private EquipmentSortingPortDTO equipmentSortingPortDTO = new EquipmentSortingPortDTO();

        /**
         * B2c称重量方允许偏差
         */
        private B2cAllowableDeviations b2cAllowableDeviations = new B2cAllowableDeviations();

        /**
         * 装箱超重配置
         */
        @Valid
        private CfgOverweightDTO cfgOverweight = new CfgOverweightDTO();

        /**
         * 产品装箱配置
         */
        private CfgProductPacking cfgProductPacking = new CfgProductPacking();


        /**
         * 中转配置
         */
        @Valid
        private List<TransferDTO> transferDTOList = new ArrayList<>();
    }

    @Data
    public static class TransferDTO{
        /**
         * 类型下拉：/wms/common/enumDropDown?type=StockOutTransferType
         */
        private String type;

        /**
         * 规则列表
         * 仓库：/wms/warehouse/list
         * 国家：/sys/dict/country/list
         * field下拉：/wms/common/enumDropDown?type=StockOutTransferField
         * compare下拉：/wms/common/enumDropDown?type=StockOutTransferCompare
         */
        @Valid
        private List<TransferConditionElement> conditionList;
    }

    @Data
    public static class TransferConditionElement{
        /**
         * 左括号
         */

        private String leftBracket;

        /**
         * 对应字段
         */
        @NotBlank(message = "条件字段不能为空")
        private String field;

        /**
         * 选项逻辑关系 大于 等于 等等
         */
        @NotBlank(message = "比较符号不能为空")
        private String compare;

        /**
         * 对应的值
         */
        @NotEmpty(message = "值字段不能为空")
        private List<String> valueList;

        private String value;

        /**
         * 右括号
         */
        private String rightBracket;

        /**
         * 逻辑关系 and 或者or
         */
        private String logic;

        /**
         * 对应的值类型
         */
        private String valueType;
    }

    /**
     * 匹配中转规则
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchTransferRuleDTO{
        /**
         * 类型
         * StockOutTransferTypeEnum
         */
        @NotBlank
        private String type;

        /**
         * 收货国家ID
         */
        private String receiveCountry;

        /**
         * 目的仓ID
         */
        private String destWarehouse;

        /**
         * 发货仓id
         */
        private String fromWarehouse;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchTransferDTO{
        /**
         * 需要中转的仓库（发货仓库）
         */
        @NotBlank(message = "发货仓库Id")
        private String warehouseId;

        /**
         * 中转规则不能为空
         */
        @NotNull(message = "匹配中转规则不能为空")
        private MatchTransferRuleDTO matchTransferRuleDTO;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchTransferResultDTO{

        /**
         * 是否中转
         */
        private Boolean isTransit;

        /**
         * 中转仓库
         */
        private String transitWarehouseId;

    }

}