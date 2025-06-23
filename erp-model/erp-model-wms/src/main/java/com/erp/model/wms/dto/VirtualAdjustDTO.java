package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.erp.model.wms.dto.excel.VirtualAdjustDetailExcelDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 * 虚拟仓调整单主表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
*/
@Data
@NoArgsConstructor
public class VirtualAdjustDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;
         /**
          * 类型名称
          */
         private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
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
         /**
          * 勾选的id集合
          */
         private List<String> ids;
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
        private String  id;
        private String  detailId;

        /**
        * 单据编号【可排序】
        */
        private String code;

        /**
        * 审核状态 【可排序】
        */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 单据日期【可排序】
        */
        private LocalDate billDate;

        /**
        * 审核时间【可排序】
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称【可排序】
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 备注【可排序】
        */
        private String remark;
        /**
        * 调整明细备注【可排序】
        */
        private String detailRemark;

        /**
        * 创建时间【可排序】
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
        /**
         * skuId 【可排序】
         */
        private String skuId;
        /**
         * skuNo 【可排序】
         */
        private String skuNo;
        /**
         * 产品名称 【可排序】
         */
        private String productName;
        /**
         * 虚拟仓id 【可排序】
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称【可排序】
         */
        private String virtualWarehouseName;
        /**
         * 库存状态【可排序】
         */
        private String inventoryStatus;
        /**
         * 库存状态名称
         */
        private String inventoryStatusName;
        /**
         * 调整前数量【可排序】
         */
        private Integer qty;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        private String  id;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 审核状态 
        */
        private String approveStatus;
        private String approveStatusName;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 备注
        */
        private String remark;
        /**
         * 明细
         */
        private List<VirtualAdjustDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 调整明细
         */
        private List<VirtualAdjustDetailDTO.AddDTO> detailList;
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
         * 调整明细
         */
        private List<VirtualAdjustDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
        * 单据编号
        */
        private String code;
        /**
         * 审核状态
         */
        @NotBlank(message = "审核状态不能为空")
        private String approveStatus;
        /**
        * 调整日期
        */
        @NotNull(message = "调整日期不能为空")
        private LocalDate billDate;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<VirtualAdjustDetailExcelDTO> successList;

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
        private List<VirtualAdjustDetailDTO.AddDTO> detailList;
    }
}