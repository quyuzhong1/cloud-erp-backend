package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.AdvanceQueryDTO;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 样品台账统计请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleLedgerDTO implements Serializable {

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 状态
         */
        private String status;

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
         * 权限SQL
         */
        private String permissionSql;

        /**
         * 是否隐藏数量为0的记录
         */
        private Boolean hideZeroQty;
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
         * 创建用户ID
         */
        private String createUserId;

        /**
         * 创建用户姓名
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 更新用户ID
         */
        private String updateUserId;

        /**
         * 更新用户姓名
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private String updateTime;

        /**
         * 版本号
         */
        private Integer version;

        /**
         * 是否删除
         */
        private Boolean isDeleted;

        /**
         * 归属用户ID
         */
        private String userId;

        /**
         * 归属用户姓名
         */
        private String userName;

        /**
         * 归属部门ID
         */
        private String deptId;

        /**
         * 归属部门名称
         */
        private String deptName;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 使用方id
         */
        private String useUserId;

        /**
         * 使用方名称
         */
        private String useUserName;

        /**
         * 库存数量（可为正数或负数）
         */
        private Integer qty;

        /**
         * SKU ID
         */
        private String skuId;

    }

    /**
     * 导出参数
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
        * 归属用户ID
        */
        private String userId;

        /**
        * 归属用户姓名
        */
        private String userName;

        /**
        * 归属部门ID
        */
        private String deptId;

        /**
        * 归属部门名称
        */
        private String deptName;

        /**
        * SKU编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 使用方id
        */
        private String useUserId;

        /**
        * 使用方名称
        */
        private String useUserName;

        /**
        * 库存数量（可为正数或负数）
        */
        private Integer qty;


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
    public static class CommonDTO {

        /**
        * 归属用户ID
        */
        @NotBlank(message = "归属用户ID不能为空")
        @Size(max = 19,message = "归属用户ID最大长度不能超过19位")
        private String userId;

        /**
        * 归属用户姓名
        */
        @NotBlank(message = "归属用户姓名不能为空")
        @Size(max = 50,message = "归属用户姓名最大长度不能超过50位")
        private String userName;

        /**
        * 归属部门ID
        */
        @NotBlank(message = "归属部门ID不能为空")
        @Size(max = 19,message = "归属部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 归属部门名称
        */
        @NotBlank(message = "归属部门名称不能为空")
        @Size(max = 50,message = "归属部门名称最大长度不能超过50位")
        private String deptName;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 使用方id
        */
        @NotBlank(message = "使用方id不能为空")
        @Size(max = 50,message = "使用方id最大长度不能超过50位")
        private String useUserId;

        /**
        * 使用方名称
        */
        @NotBlank(message = "使用方名称不能为空")
        @Size(max = 50,message = "使用方名称最大长度不能超过50位")
        private String useUserName;

        /**
        * 库存数量（可为正数或负数）
        */
        @NotNull(message = "库存数量（可为正数或负数）不能为空")
        private Integer qty;


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {

        private String childId;

        private String type;

        private String skuNo;

        private List<String> skuNos;

        @NotBlank(message = "归属用户不能为空")
        private String userId;

    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SkuAvailableQtyDTO {
        /**
         * id
         */
        private String sampleLedgerId;

        /**
         * 归属用户ID
         */
        private String userId;

        /**
         * 归属用户姓名
         */
        private String userName;

        /**
         * 归属部门ID
         */
        private String deptId;

        /**
         * 归属部门名称
         */
        private String deptName;


        /**
         * 使用方id
         */
        private String useUserId;

        /**
         * 使用方名称
         */
        private String useUserName;

        /**
         * 产品
         */
        private String skuId;

        private String skuNo;

        private String productName;

        /**
         * 库存数量（可为正数或负数）
         */
        private Integer availableQty;

    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SampleScrapView {

        /**
         * 报废操作人ID
         */
        private String scrapUserId;

        /**
         * 报废操作人姓名
         */
        private String scrapUserName;

        /**
         * 明细
         */
        private List<SampleScrapDetailDTO.ViewDTO> detailList;

    }


}