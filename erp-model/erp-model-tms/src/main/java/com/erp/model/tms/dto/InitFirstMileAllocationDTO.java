package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>
 * 期初头程分摊请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
*/
@Data
@NoArgsConstructor
public class InitFirstMileAllocationDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
        */
        private String status;
        /**
         * 单据状态名称
         */
        private String statusName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 明细记录
         */
        private List<InitFirstMileAllocationDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细
         */
        private List<InitFirstMileAllocationDetailDTO.AddDTO> detailList;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 明细
         */
        private List<InitFirstMileAllocationDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
        */
//        @NotBlank(message = "单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核不能为空")
        @Size(max = 30,message = "单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核最大长度不能超过30位")
        private String status;

        /**
         * 单据编码
         */
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingVO {
        private String id;
        /**
         * 单据编号【可排序】
         */
        private String code;
        /**
         * 审核状态【可排序】
         */
        private String status;
        /**
         * 审核状态名称
         */
        private String statusName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 物流单id
         */
        private String logisticsBillId;

        /**
         * 发货单id
         */
        private String sourceId;

        /**
         * 发货单明细id
         */
        private String sourceDetailId;

        /**
         * 发货单编号【可排序】
         */
        private String sourceCode;

        /**
         * 数据来源类型：delivery=发货单
         */
        private String sourceType;

        /**
         * 业务单号【可排序】
         */
        private String businessCode;

        /**
         * 业务来源类型：FBA=FBA，第三方仓=thirdWarehouse
         */
        private String businessType;

        /**
         * 店铺id【可排序】
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 仓库ID【可排序】
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号【可排序】
         */
        private String skuNo;
        /**
         * 平台SKU
         */
        private String platformSkuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 上线前签收数量【可排序】
         */
        private Integer initReceiveQty;

        /**
         * 期初在途头程费用【可排序】
         */
        private BigDecimal initTransitCost;
        /**
         * 期初在途头程关税【可排序】
         */
        private BigDecimal initTransitTariff;
        /**
         * 期初暂估头程费用【可排序】
         */
        private BigDecimal initEstimatedCost;

        /**
         * 期初暂估头程关税【可排序】
         */
        private BigDecimal initEstimatedTariff;

        /**
         * 分摊重量【可排序】
         */
        private BigDecimal weightAllocation;

        /**
         * 产品成本【可排序】
         */
        private String productCost;

        /**
         * 汇率【可排序】
         */
        private BigDecimal exchangeRate;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;
    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    /**
     * 对账单
     */
    @Data
    @NoArgsConstructor
    public static class ReconciliationDTO {

        /**
         * detailId集合（期初明细表id）
         */
        @NotNull(message = "detailId集合不能为空")
        private List<String> detailIds;

        /**
         * 对账单id（为空说明是新生成）
         */
        private String reconciliationId;

        /**
         * 周期
         */
        @NotNull(message = "周期不能为空")
        private List<LocalDate> dateList;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<InitFirstMileAllocationDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {
        /**
         * 导入文件
         */
        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;
        /**
         * 明细
         */
        private List<InitFirstMileAllocationDetailDTO.AddDTO> detailList;
    }
}