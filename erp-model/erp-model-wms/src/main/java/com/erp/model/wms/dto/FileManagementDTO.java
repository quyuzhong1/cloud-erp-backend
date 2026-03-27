package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文件管理请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Data
@NoArgsConstructor
public class FileManagementDTO implements Serializable {


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
         * sku引用id
         */
        private String skuRefId;
        /**
         * 文件id
         */
        private String fileId;

        /**
         * 单据编码(WDGL开头)【可排序】
         */
        private String code;

        /**
         * skuid
         */
        private String skuId;

        /**
         * sku编码【可排序】
         */
        private String skuNo;

        /**
         * 文件类型【可排序】
         */
        private String fileType;
        /**
         * 文件类型名称
         */
        private String fileTypeName;

        /**
         * 一级品类id
         */
        private String firstCategoryId;

        /**
         * 一级品类名称【可排序】
         */
        private String firstCategoryName;

        /**
         * 产品名称【可排序】
         */
        private String productName;

        /**
         * 文件名称【可排序】
         */
        private String attachName;
        /**
         * 文件版本【可排序】
         */
        private Integer attachVersion;

        /**
         * 文件链接
         */
        private String attachUrl;
        /**
         * 备注【可排序】
         */
        private String remark;

        /**
         * 上传日期
         */
        private LocalDateTime uploadTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;
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
         * 单据编码(WDGL开头)
         */
        private String code;

        /**
         * skuid
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 文件类型
         */
        private String fileType;
        /**
         * 文件类型名称
         */
        private String fileTypeName;

        /**
         * 一级品类id
         */
        private String firstCategoryId;

        /**
         * 一级品类名称
         */
        private String firstCategoryName;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 文件版本
         */
        private Integer attachVersion;

        /**
         * 文件链接
         */
        private String attachUrl;

        /**
         * 文件大小
         */
        private BigDecimal attachSize;

        /**
         * 文件名称
         */
        private String attachName;
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
         * skuid
         */
        @Size(max = 19, message = "skuid最大长度不能超过19位")
        private String skuId;

        /**
         * 文件类型
         * WmsFileTypeEnum 枚举类
         */
        @NotBlank(message = "文件类型不能为空")
        @Size(max = 32, message = "文件类型最大长度不能超过32位")
        private String fileType;

        /**
         * 一级品类id
         */
        @Size(max = 19, message = "一级品类id最大长度不能超过19位")
        private String firstCategoryId;

        /**
         * 一级品类名称
         */
        @Size(max = 32, message = "一级品类名称最大长度不能超过32位")
        private String firstCategoryName;

        /**
         * 产品名称
         */
        @Size(max = 255, message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
         * 文件版本
         */
        private Integer attachVersion;

        /**
         * 文件链接
         */
        @NotBlank(message = "附件上传或飞书文档链接上传二选一，飞书文档链接必须先下载再保存")
        @Size(max = 255, message = "文件链接最大长度不能超过255位")
        private String attachUrl;

        /**
         * 文件大小
         */
        @Digits(integer = 12, fraction = 4, message = "文件大小整数位不能超过12位，小数位不能超过4位")
        private BigDecimal attachSize;

        /**
         * 文件名称
         */
        @NotBlank(message = "文件名称不能为空")
        @Size(max = 255, message = "文件名称最大长度不能超过255位")
        private String attachName;
        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class VersionDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 业务id
         */
        private String businessId;

        /**
         * 文件版本
         */
        private Integer attachVersion;
        /**
         * 文件链接
         */
        private String attachUrl;
        /**
         * 文件名称
         */
        private String attachName;
        /**
         * 上传日期
         */
        private String createTime;
        /**
         * 文件大小
         */
        private BigDecimal attachSize;
        /**
         * 文件大小字符串
         */
        private String attachSizeStr;
        /**
         * 操作人
         */
        private String createUserName;
    }
}