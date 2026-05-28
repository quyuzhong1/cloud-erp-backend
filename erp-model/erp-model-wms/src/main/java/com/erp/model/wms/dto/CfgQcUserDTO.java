package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 质检员配置请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2026-05-27
*/
@Data
@NoArgsConstructor
public class CfgQcUserDTO implements Serializable {


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
        * 供应商名称
        */
        private String supplierName;

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

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 入库质检员
        */
        private String stockInQcUserName;

        /**
        * 出库质检员
        */
        private String stockOutQcUserName;

        /**
        * 外检质检员
        */
        private String outsideQcUserName;

        /**
        * 在库质检员
        */
        private String insideQcUserName;

        /**
        * 新品入库质检员
        */
        private String newProductStockInQcUserName;

        /**
        * B2B外检质检员
        */
        private String b2bOutsideQcUserName;

        /**
        * 退货质检员
        */
        private String returnQcUserName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
        * 更新时间
        */
        private LocalDateTime updateTime;

        /**
        * 更新人名称
        */
        private String updateUserName;

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
    * 详情/编辑
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 入库质检员id
        */
        private String stockInQcUserId;

        /**
        * 入库质检员名称
        */
        private String stockInQcUserName;

        /**
        * 出库质检员id
        */
        private String stockOutQcUserId;

        /**
        * 出库质检员名称
        */
        private String stockOutQcUserName;

        /**
        * 外检质检员id
        */
        private String outsideQcUserId;

        /**
        * 外检质检员名称
        */
        private String outsideQcUserName;

        /**
        * 在库质检员id
        */
        private String insideQcUserId;

        /**
        * 在库质检员名称
        */
        private String insideQcUserName;

        /**
        * 新品入库质检员id
        */
        private String newProductStockInQcUserId;

        /**
        * 新品入库质检员名称
        */
        private String newProductStockInQcUserName;

        /**
        * B2B外检质检员id
        */
        private String b2bOutsideQcUserId;

        /**
        * B2B外检质检员名称
        */
        private String b2bOutsideQcUserName;

        /**
        * 退货质检员id
        */
        private String returnQcUserId;

        /**
        * 退货质检员名称
        */
        private String returnQcUserName;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 供应商id
         * /scm/drop/down/supplier/pagingSelect
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 64,message = "供应商id最大长度不能超过64位")
        private String supplierId;

        /**
        * 仓库id
         * /warehouse/list
        */
        private String warehouseId;

        /**
        * 入库质检员id
        */
        private String stockInQcUserId;

        /**
        * 入库质检员名称
        */
        private String stockInQcUserName;

        /**
        * 出库质检员id
        */
        private String stockOutQcUserId;

        /**
        * 出库质检员名称
        */
        private String stockOutQcUserName;

        /**
        * 外检质检员id
        */
        private String outsideQcUserId;

        /**
        * 外检质检员名称
        */
        private String outsideQcUserName;

        /**
        * 在库质检员id
        */
        private String insideQcUserId;

        /**
        * 在库质检员名称
        */
        private String insideQcUserName;

        /**
        * 新品入库质检员id
        */
        private String newProductStockInQcUserId;

        /**
        * 新品入库质检员名称
        */
        private String newProductStockInQcUserName;

        /**
        * B2B外检质检员id
        */
        private String b2bOutsideQcUserId;

        /**
        * B2B外检质检员名称
        */
        private String b2bOutsideQcUserName;

        /**
        * 退货质检员id
        */
        private String returnQcUserId;

        /**
        * 退货质检员名称
        */
        private String returnQcUserName;

    }

    /**
    * 供应商下拉选择DTO
    */
    @Data
    @NoArgsConstructor
    public static class SupplierSelectDTO {

        /**
        * 供应商id
        */
        private String id;

        /**
        * 供应商编码
        */
        private String code;

        /**
        * 供应商名称
        */
        private String name;

    }

    /**
    * 质检员下拉选择DTO
    */
    @Data
    @NoArgsConstructor
    public static class QcUserSelectDTO {

        /**
        * 用户id
        */
        private String id;

        /**
        * 用户名称
        */
        private String name;

    }

    /**
    * 导入Excel DTO
    */
    @Data
    @NoArgsConstructor
    public static class ImportExcelDTO {

        /**
        * 行号
        */
        private Integer rowNum;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 入库质检员
        */
        private String stockInQcUserName;

        /**
        * 出库质检员
        */
        private String stockOutQcUserName;

        /**
        * 外检质检员
        */
        private String outsideQcUserName;

        /**
        * 在库质检员
        */
        private String insideQcUserName;

        /**
        * 新品入库质检员
        */
        private String newProductStockInQcUserName;

        /**
        * B2B外检质检员
        */
        private String b2bOutsideQcUserName;

        /**
        * 退货质检员
        */
        private String returnQcUserName;

        /**
        * 错误信息
        */
        private String errorMsg;

    }

}