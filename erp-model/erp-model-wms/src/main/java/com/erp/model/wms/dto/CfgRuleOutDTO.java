package com.erp.model.wms.dto;

import com.common.core.anno.StateEnumValue;
import com.common.core.entity.ConditionElement;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.xpath.operations.Bool;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
         * 中转配置
         */
        private TransferDTO transferDTO = new TransferDTO();
    }

    @Data
    public static class TransferDTO{
        /**
         * 发货类型：/wms/dict/drop/down?type=transferDeliveryType
         */
        private String type;

        /**
         * 发货类型名称
         */
        private String typeName;

        /**
         * 规则列表
         * 仓库：/wms/warehouse/list
         * 国家：/sys/dict/country/list
         * field下拉：/wms/dict/drop/down?type=transferConditionField
         * field下拉：/wms/common/enumDropDown?type=StockOutTransferType
         * compare下拉：/wms/common/enumDropDown?type=StockOutTransferCompare
         */
        private List<ConditionElement> conditionList;
    }

    @Data
    public static class TransferConditionDTO{
        private String leftBracket;
        @NotBlank(message = "条件的字段不能为空")
        private String field;
        @NotBlank(message = "比较符不能为空")
        private String compare;
        private String value;
        private String rightBracket;
        private String logic;
        private String name;
    }

    /**
     * 匹配中转规则
     */
    @Data
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
        private String targetWarehouse;
    }
}