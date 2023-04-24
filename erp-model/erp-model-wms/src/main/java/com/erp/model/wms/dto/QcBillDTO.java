package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcBill
 * @Description TODO
 * @Date 2023-04-14 15:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcBillDTO implements Serializable {


    /**
     * 添加质检规则
     */
    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO extends PermissionsDTO {

        /**
         * 质检单id
         */
        private String id;


        /**
         * 质检日期
         */
        @NotNull(message = "质检日期不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        //@NotBlank(message = "采购订单id不能为空")
        private String purchaseOrderId;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检部门id
         */
        private String qcDeptId;


        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 产品信息
         * 从这个 接口获取http://172.16.100.11:3002/project/83/interface/api/9511
         */
        @Valid
        private QcProductDTO.AddDTO qcProduct;


        /**
         * 质检信息
         */
        @Valid
        private QcInfoDTO.AddDTO qcInfo;


        /**
         * 质检报告明细
         * 来源  http://172.16.100.11:3002/project/92/interface/api/9574
         */
        @Valid
        private List<QcReportDetailDTO.AddDTO> reportDetailList;

        /**
         * 质检单备注 集合
         */
        private List<QcRemarkDTO.AddDTO> remarkList;


    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        private String searchType;

        private String typeName;

        private Integer count;


    }


    /**
     * 质检单详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends PermissionsDTO {

        /**
         * 质检单id
         */
        private String id;


        /**
         * code
         */
        private String code;

        /**
         * 质检日期
         */
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * 采购订单code
         */
        private String purchaseOrderCode;


        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;


        /**
         * 仓库 id
         */
        private String warehouseId;


        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检员
         */
        private String qcUserName;


        /**
         * 质检部门id
         */
        private String qcDeptId;

        /**
         * 质检状态
         */
        private QcBillStatusEnum qcStatus;

        /**
         * 质检状态名
         */
        private String qcStatusName;

        /**
         * 质检部门
         */
        private String qcDeptName;

        /**
         * 产品信息
         * 从这个 接口获取http://172.16.100.11:3002/project/83/interface/api/9511
         */
        @Valid
        private QcProductDTO.ViewDTO qcProduct;


        /**
         * 质检信息
         */
        @Valid
        private QcInfoDTO.ViewDTO qcInfo;


        /**
         * 质检报告明细
         * 来源  http://172.16.100.11:3002/project/92/interface/api/9574
         */
        @Valid
        private List<QcReportDetailDTO.ViewDTO> reportDetailList;

        /**
         * 质检单备注 集合
         */
        private List<QcRemarkDTO.AddDTO> remarkList;


    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * all 全部
         * waitQc 待质检
         * finishQc 完成质检
         * cancel 已取消
         */
        @StateEnumValue(strValues = {"all", "waitQc", "finishQc", "cancel"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;


        /**
         *质检单号
         */
        private String code;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;


        /**
         * SKU no
         */
        private List<String> skuNoList;


        /**
         * 质检状态
         * 来源 http://172.16.100.11:3002/project/92/interface/api/9673
         */
        private List<String> qcStatusList;


        /**
         * 质检类型
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890
         */
        private String qcType;


        /**
         * 是否内检
         * true 内部检验
         */
        private Boolean isInside;


        /**
         * 供应商id集合
         */
        private List<String> supplierIdList;


        /**
         * 质检结果集合
         * 来源 http://172.16.100.11:3002/project/92/interface/api/10024
         */
        private List<String> qcResultList;


        /**
         * 处理措施
         */
        private List<String> handleModeDictList;


        /**
         * 仓库id 集合
         */
        private List<String> warehouseIdList;


        /**
         * 质检员集合
         */
        private List<String> qcUserIdList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

    }


    /**
     * 仓库分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 质检单id
         */
        private String id;


        /**
         * code
         */
        private String code;

        /**
         * 质检日期
         */
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * 采购订单code
         */
        private String purchaseOrderCode;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检员
         */
        private String qcUserName;


        /**
         * 质检状态
         * draft 暂存
         * waitQc 待质检
         * exemption 免检
         * finishQc 已质检
         * cancel 取消
         */
        private QcBillStatusEnum qcStatus;

        /**
         * 质检状态名
         */
        private String qcStatusName;


        /**
         * 质检类型
         */
        private QcTypeEnum qcType;

        /**
         * 质检类型名
         */
        private String qcTypeName;

        /**
         * 是否内检  true 是
         */
        private Boolean isInside;


        /**
         * 内检 类型
         */
        private String insideType;


        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 名
         */
        private String skuName;


        /**
         * sku 名
         */
        private String skuNo;

        /**
         * 总量
         */
        private Integer totalQty;

        /**
         * 质检量
         */
        private Integer qcQty;


        /**
         * 质检合格量
         */
        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        private Integer qcBadQty;

        /**
         * 质检结果
         */
        private QcResultEnum qcResult;

        /**
         * 质检结果名
         */
        private String qcResultName;

        /**
         * 处理措施
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        private String handleModeDict;

        /**
         * 处理措施名
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        private String handleModeName;

        /**
         * 不良现象
         */
        private String badDescription;


        /**
         * 质检合格率
         */
        private BigDecimal qcGoodRate;

        /**
         * 质检不良率
         */
        private BigDecimal qcBadRate;


        /**
         * 仓库 id
         */
        private String warehouseId;


        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 备注
         */
        private String remark;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }


    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }


    /**
     * 分配质检员
     */
    @Data
    @NoArgsConstructor
    public class AssignDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        @NotBlank(message = "质检员不能为空")
        private String qcUserId;
    }
}
