package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @description: 产品证书表
 * @author Will
 * @date: 2024/2/19 10:41
 */
@Data
@NoArgsConstructor
public class ProductCertificateDTO implements Serializable {

    /**
     * 列表DTO
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id【可排序】
         */
        private String id;

        /**
         * 产品sku表id【可排序】
         */
        private String skuId;

        /**
         * 产品sku编码【可排序】
         */
        private String skuNo;

        /**
         * 产品sku名称（品名）【可排序】
         */
        private String skuName;

        /**
         * 证书类型【可排序】
         */
        private String type;

        /**
         * 证书类型名称
         */
        private String typeName;

        /**
         * 证书项目【可排序】
         */
        private String dictProject;

        /**
         * 证书项目名称
         */
        private String dictProjectName;

        /**
         * 证书有效期【可排序】
         */
        private LocalDate certificateValidTime;

        /**
         * 文件名称【可排序】
         */
        private String attachName;

        /**
         * 文件路径【可排序】
         */
        private String attachUrl;

        /**
         * 文件全路径
         */
        private String fullAttachUrl;

        /**
         * 备注【可排序】
         */
        private String remark;

        /**
         * 更新人【可排序】
         */
        private String updateUserName;

        /**
         * 更新时间【可排序】
         */
        private LocalDateTime updateTime;
    }

    /**
     * 参数DTO
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

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
     * 新增DTO
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId集合
         */
        @NotEmpty(message = "SKU不能为空")
        private List<String> skuIdList;

        /**
         * 证书类型
         */
        @NotBlank(message = "证书类型不能为空")
        private String type;

        /**
         * 证书文件
         */
        @NotEmpty(message = "证书文件不能为空")
        @Valid
        private List<FileDTO> fileList;

        /**
         * 有效期
         */
        private LocalDate certificateValidTime;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 附件DTO
     */
    @Data
    @NoArgsConstructor
    public static class FileDTO {

        /**
         * 证书项目
         */
        @NotBlank(message = "证书项目不能为空")
        private String dictProject;

        /**
         * 证书文件
         */
        @NotNull(message = "证书文件不能为空")
        private MultipartFile multipartFile;

    }

    /**
     * 修改DTO
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 有效期
         */
        private LocalDate certificateValidTime;

        /**
         * 证书文件
         */
        private MultipartFile multipartFile;

        /**
         * 备注
         */
        private String remark;

        /**
         * 需要上传的附件id集合
         */
        private List<String> removeFileIdList;
    }

    /**
     * 查看详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 证书类型
         */
        private String type;

        /**
         * 证书类型
         */
        private String typeName;

        /**
         * 证书项目
         */
        private String dictProject;

        /**
         * 证书项目名称
         */
        private String dictProjectName;

        /**
         * 有效时间
         */
        private LocalDate certificateValidTime;

        /**
         * 备注
         */
        private String remark;

        /**
         * 历史附件
         */
        private List<AttachmentDTO.ListDTO> historyFileList;

    }
}