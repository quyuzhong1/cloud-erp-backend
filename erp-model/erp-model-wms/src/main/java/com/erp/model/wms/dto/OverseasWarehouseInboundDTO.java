package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.RequestIdTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 海外仓入库单请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Data
@NoArgsConstructor
public class OverseasWarehouseInboundDTO implements Serializable {


    /**
     * 列表查询入参
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;


        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 平台类型: goodcang=谷仓，iml=艾姆勒
         */
        private String dictPlatform;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 入库类型
         */
        private String instockType;

        /**
         * 入库类型名称
         */
        private String instockTypeName;

        /**
         * 入库状态
         */
        private String instockStatus;

        /**
         * 入库状态名称
         */
        private String instockStatusName;

        /**
         * 发货仓名称
         */
        private String deliveryWarehouseName;

        /**
         * 发货仓ID
         */
        private String deliveryWarehouseId;

        /**
         * 中转仓名称
         */
        private String transferWarehouseName;

        /**
         * 中转仓ID
         */
        private String transferWarehouseId;

        /**
         * 目的仓名称
         */
        private String toWarehouseName;

        /**
         * 目的仓ID
         */
        private String toWarehouseId;

        /**
         * 物流方式
         */
        private String logisticsMethod;

        /**
         * 物流方式名称
         */
        private String logisticsMethodName;

        /**
         * 交货方式
         * /api/wms/common/enumDropDown?type=OverseasDeliveryMode
         */
        private String deliveryMode;

        /**
         * 交货方式名称
         */
        private String deliveryModeName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 最新签收时间
         */
        private LocalDateTime receiveTime;

        /**
         * 预计到达时间
         */
        private LocalDateTime estimatedArrivalDate;

        /**
         * 手动完结原因
         */
        private String finishReason;

        /**
         * 第三方唯一编码
         */
        private String overseasWarehouseInboundId;

        /**
         * 详情id
         */
        private String detailId;

        /**
         * 海外仓平台产品名称
         */
        private String platformProductName;

        /**
         * 海外仓平台SKU号
         */
        private String platformSkuNo;

        /**
         * ERP系统产品名称
         */
        private String productName;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * ERP的SKU ID
         */
        private String skuId;

        /**
         * 是否组合品：combination 组合 single 单品
         */
        private Boolean isCombination;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 在途数量
         */
        private Integer transportQty;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 收发差异
         */
        private Integer diffQty;

        /**
         * 签收时间
         */
        private LocalDateTime detailReceiveTime;

        /**
         * 签收状态：not=未签收，already=已签收
         */
        private String receiveStatus;

        /**
         * 签收类型：system=平台系统签收，manual=手动签收
         */
        private String receiveType;

        /**
         * 报关方式名称
         */
        private String customsTypeName;

        /**
         * 报关方式代号
         */
        private String customsType;

        /**
         * 快递单号
         */
        private String expressNo;

        /**
         * 物流产品代码
         * /api/wms/overseasWarehouseInbound/transferWareHouseList?code=中转仓代号
         */
        private String logisticsProductCode;

        /**
         * 物流产品名称
         */
        private String logisticsProductName;

        /**
         * 预计揽收日期
         */
        private LocalDate estimatedCollectDate;

        /**
         * 字典)省ID
         */
        private String dictProvinceId;

        /**
         * 字典)城市ID
         */
        private String dictCityId;

        /**
         * 字典)地区ID
         */
        private String dictDistrictId;

        /**
         * 字典)省名称
         */
        private String dictProvinceName;

        /**
         * 字典)城市名称
         */
        private String dictCityName;

        /**
         * 字典)地区名称
         */
        private String dictDistrictName;

        /**
         * 姓
         */
        private String firstName;

        /**
         * 名
         */
        private String lastName;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 详情地址
         */
        private String street;
    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 详情id
         */
        private String detailId;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 入库类型
         */
        private String instockType;

        /**
         * 入库类型名称
         */
        private String instockTypeName;

        /**
         * 入库状态
         */
        private String instockStatus;

        /**
         * 入库状态名称
         */
        private String instockStatusName;

        /**
         * 发货仓名称
         */
        private String deliveryWarehouseName;

        /**
         * 发货仓ID
         */
        private String deliveryWarehouseId;

        /**
         * 中转仓名称
         */
        private String transferWarehouseName;

        /**
         * 中转仓ID
         */
        private String transferWarehouseId;

        /**
         * 目的仓名称
         */
        private String toWarehouseName;

        /**
         * 目的仓ID
         */
        private String toWarehouseId;

        /**
         * 物流方式
         */
        private String logisticsMethod;

        /**
         * 备注
         */
        private String remark;

        /**
         * 最新签收时间
         */
        private LocalDate receiveTime;

        /**
         * 预计到达时间
         */
        private LocalDate estimatedArrivalDate;

        /**
         * 手动完结原因
         */
        private String finishReason;

        /**
         * 完结状态: not=未完结, auto=自动完结，manual=手动完结
         */
        private String finishStatus;

        /**
         * 物流跟踪号
         */
        private String trackingNo;

        /**
         * SKU信息
         */
        private List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailList;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 报关方式名称
         */
        private String customsTypeName;

        /**
         * 报关方式代号
         * /api/wms/common/enumDropDown?type=OverseasCustomsTypeNew
         */
        private String customsType;

        /**
         * 交货方式
         * /api/wms/common/enumDropDown?type=OverseasDeliveryMode
         */
        private String deliveryMode;

        /**
         * 交货方式名称
         */
        private String deliveryModeName;

        /**
         * 快递单号
         */
        private String expressNo;

        /**
         * 物流产品代码
         * /api/wms/overseasWarehouseInbound/transferWareHouseList?code=中转仓代号
         */
        private String logisticsProductCode;

        /**
         * 物流产品名称
         */
        private String logisticsProductName;

        /**
         * 预计揽收日期
         */
        private LocalDate estimatedCollectDate;

        /**
         * 字典)省ID
         */
        private String dictProvinceId;

        /**
         * 字典)城市ID
         */
        private String dictCityId;

        /**
         * 字典)地区ID
         */
        private String dictDistrictId;

        /**
         * 字典)省名称
         */
        private String dictProvinceName;

        /**
         * 字典)城市名称
         */
        private String dictCityName;

        /**
         * 字典)地区名称
         */
        private String dictDistrictName;

        /**
         * 姓
         */
        private String firstName;

        /**
         * 名
         */
        private String lastName;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 详情地址
         */
        private String street;

        /**
         * 平台类型: goodcang=谷仓，iml=艾姆勒，空=手动添加
         */
        private String dictPlatform;

        /**
         * 平台类型名称
         */
        private String dictPlatformName;

        /**
         * 地址邮编
         */
        private String zipcode;

        /**
         * 报关类型
         */
        private String declareType;

        /**
         * 报关类型名称
         */
        private String declareTypeName;
    }

    /**
     * 新增
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 发货单ID
         */
        @NotBlank(message = "发货单ID不能为空")
        private String sourceId;

    }

    /**
     * 修改
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ReceivedDTO {

        /**
         * 详情detailId
         */
        @NotBlank(message = "详情detailId不能为空")
        private String detailId;

        /**
         * 签收数量
         */
        @NotNull(message = "签收数量不能为空")
        private Integer receivedQty;

        /**
         * 签收日期
         */
        @NotNull(message = "签收日期不能为空")
        private LocalDate receiveDate;
    }

    /**
     * 手动完结
     */
    @Data
    @NoArgsConstructor
    public static class FinishDTO {

        /**
         * 海外入库单ID
         */
        @NotBlank(message = "海外入库单ID不能为空")
        private String id;

        /**
         * 完结原因
         */
        @NotNull(message = "完结原因不能为空")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 单号
         */
        private String code;

        /**
         * 入库类型
         */
        @NotNull(message = "入库类型不能为空")
        @JsonDeserialize(using = OverseasInstockTypeEnum.OverseasInStockTypeDeserializer.class)
        private OverseasInstockTypeEnum instockType;

        /**
         * 物流方式
         */
//        @NotNull(message = "物流方式不能为空")
        @JsonDeserialize(using = LogisticsMethodEnum.LogisticsMethodDeserializer.class)
        private LogisticsMethodEnum logisticsMethod;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 预计到达时间
         */
        @NotNull(message = "预计到达时间不能为空")
        private LocalDate estimatedArrivalDate;

        /**
         * 物流跟踪号
         */
        private String trackingNo;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 交货方式
         * /api/wms/common/enumDropDown?type=OverseasDeliveryMode
         */
        private String deliveryMode;

        /**
         * 快递单号
         */
        private String expressNo;

        /**
         * 中转仓ID
         */
        private String transferWarehouseId;

        /**
         * 前端不用传
         */
        private String transferWarehouseName;

        /**
         * 报关方式代号:
         * /api/wms/common/enumDropDown?type=OverseasCustomsTypeNew
         */
        private String customsType;

        /**
         * 前端不用传
         */
        private String customsTypeName;

        /**
         * 物流产品代码
         * /api/wms/overseasWarehouseInbound/transferWareHouseList?code=中转仓代号
         * 艾姆勒: /api/dict/drop/down?type=imlLogisticProduct
         */
        private String logisticsProductCode;

        /**
         * 前端不传
         */
        private String logisticsProductName;
        /**
         * 报关类型
         * 艾姆勒: /api/dict/drop/down?type=imlDeclareType
         */
        private String declareType;

        /**
         * 预计揽收日期
         */
        private LocalDate estimatedCollectDate;

        /**
         * 字典)省ID
         */
        private String dictProvinceId;

        /**
         * 字典)城市ID
         */
        private String dictCityId;

        /**
         * 字典)地区ID
         */
        private String dictDistrictId;

        /**
         * 前端不传
         */
        private String dictProvinceName;

        /**
         * 前端不传
         */
        private String dictCityName;

        /**
         * 前端不传
         */
        private String dictDistrictName;

        /**
         * 前端不传
         */
        private String platformProvinceId;

        /**
         * 前端不传
         */
        private String platformCityId;

        /**
         * 前端不传
         */
        private String platformDistrictId;

        /**
         * 姓
         */
        private String firstName;

        /**
         * 名
         */
        private String lastName;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 详情地址
         */
        private String street;

        /**
         * 地址邮编
         */
        private String zipcode;

        /**
         * 揽收时间起
         */
        private LocalDateTime collectStartTime;

        /**
         * 揽收时间止
         */
        private LocalDateTime collectEndTime;
        /**
         * 所有ID
         */
        public List<String> getAllDictCityId() {
            return Arrays.asList(this.dictProvinceId, this.dictCityId, this.dictDistrictId);
        }

        public void setBlankOtherBySelfHeadway() {
            this.setDeliveryMode("");
            this.setExpressNo("");
            this.setTransferWarehouseId("");
            setBankWithoutCollectAtHome();
        }



        public void setBlankOtherByTransferAgentAndSelfDelivery() {
            this.setLogisticsMethod(null);
            this.setTrackingNo("");
            setBankWithoutCollectAtHome();
        }

        private void setBankWithoutCollectAtHome() {
            this.setDictProvinceId("");
            this.setDictCityId("");
            this.setDictDistrictId("");
            this.setFirstName("");
            this.setLastName("");
            this.setMobile("");
            this.setStreet("");
            this.setZipcode("");
        }

        public void setBlankOtherByTransferAgentAndCollectAtHome() {
            this.setLogisticsMethod(null);
            this.setTrackingNo("");
            this.setExpressNo("");
        }
    }

    /**
     * 查询收货记录返回值
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveRecordView {
        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;
        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 签收数量
         */
        private String receiveUser;
        /**
         * 数据来源
         */
        private String sourceType;
        /**
         * 数据来源名称
         */
        private String sourceTypeName;
    }


    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class ViewListReqDTO {

        /**
         * 目标ID类型:
         * mainId=单据ID
         * detailId=详情ID
         */
        @NotNull(message = "目标ID类型不能为空")
        @JsonDeserialize(using = RequestIdTypeEnum.RequestIdEnumDeserializer.class)
        private RequestIdTypeEnum requestIdType;

        /**
         * 请求ID列表
         */
        @NotNull(message = "请求ID列表不能为空")
        @Size(min = 1, message = "请求ID至少有一个")
        private List<@NotBlank(message = "请求ID不能为空") String> requestIdList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountDTO {
        /**
         * 入库单状态:
         * OutstockTypeEnum
         * 获取路径：/wms/common/enumDropDown?type=OverseasInstockStatus
         * toBeShipped=待发货，toBeSigned=待签收，partialSigned=部分签收，signed=已签收，canceled=已取消，abnormal=异常，
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;

    }
}