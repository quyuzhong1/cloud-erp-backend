package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import com.common.business.dto.base.BaseDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 图片分类附件关联表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
*/
@Data
@NoArgsConstructor
public class RefProductImgAttachmentDTO implements Serializable {



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
        * 分类ID
        */
        private String categoryId;

        /**
        * 产品明细ID
        */
        private String productDetailId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * 附件ID
        */
        private String attachmentId;

        /**
         * 附件名称
         */
        private String attachName;

        /**
         * 附件URL
         */
        private String attachUrl;

        /**
         * 附件大小
         */
        private BigDecimal attachSize;

        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

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
        * 分类ID
        */
        private String categoryId;

        /**
        * 产品明细ID
        */
        private String productDetailId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * 附件ID
        */
        private String attachmentId;


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
        * 分类ID
        */
        @NotBlank(message = "分类ID不能为空")
        @Size(max = 19,message = "分类ID最大长度不能超过19位")
        private String categoryId;

        /**
        * 产品明细ID
        */
        private String productDetailId;

        /**
        * 附件ID
        */
        @NotBlank(message = "附件ID不能为空")
        @Size(max = 19,message = "附件ID最大长度不能超过19位")
        private String attachmentId;


    }

    /**
     * 批量上传图片参数
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class BatchUploadDTO extends BaseDTO.ImportDTO {
        /**
         * ZIP文件URL（从/plm/common/upload获取，前端传递，会赋值到fileUrl）
         */
        @NotBlank(message = "ZIP文件URL不能为空")
        private String zipUrl;

        /**
         * 分类ID
         */
        @NotBlank(message = "分类ID不能为空")
        private String categoryId;
    }

    /**
     * 批量上传错误信息DTO（用于导出Excel）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchUploadErrorDTO {
        /**
         * 文件名
         */
        private String fileName;

        /**
         * SKU编码（从文件名提取）
         */
        private String skuNo;

        /**
         * 错误原因
         */
        private String errorReason;
    }

    /**
     * 移动分类DTO
     */
    @Data
    @NoArgsConstructor
    public static class MoveCategoryDTO {
        /**
         * 关联记录ID列表
         */
        @NotEmpty(message = "关联记录ID列表不能为空")
        private List<String> ids;

        /**
         * 目标分类ID
         */
        @NotBlank(message = "目标分类ID不能为空")
        private String categoryId;
    }

    /**
     * 批量下载DTO
     */
    @Data
    @NoArgsConstructor
    public static class BatchDownloadDTO {
        /**
         * 关联记录ID列表
         */
        @NotEmpty(message = "关联记录ID列表不能为空")
        private List<String> ids;
    }

    /**
     * 批量下载任务参数DTO（用于异步任务）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchDownloadTaskDTO {
        /**
         * ZIP文件URL
         */
        private String zipUrl;
    }

    /**
     * 上传产品主图DTO
     */
    @Data
    @NoArgsConstructor
    public static class UploadProductMainImageDTO {
        /**
         * 产品sku明细表id
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 图片地址列表
         */
        private List<String> imagesUrls;
    }

}