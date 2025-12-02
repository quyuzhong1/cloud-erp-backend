package com.erp.model.oms.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * KOL回片列表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolFeedbackDTO implements Serializable {

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
    public static class ParamDTO extends SortDTO {

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
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 来源明细ID
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 达人昵称
         */
        private String partnerNickname;

        /**
         * 达人ID
         */
        private String partnerId;

        /**
         * 回片链接（完整链接）
         */
        private String url;

        /**
         * 回片链接哈希值（MD5或SHA256，用于唯一键）
         */
        private String urlHash;

        /**
         * 发布形式
         */
        private String publishType;

        /**
         * 发布形式名称
         */
        private String publishTypeName;

        /**
         * 发布日期
         */
        private LocalDate publishDate;

        /**
         * 备注
         */
        private String remark;

        /**
         * 回片状态 com.erp.model.oms.enums.FeedbackStatusEnum
         */
        private String feedbackStatus;

        /**
         * 回片状态名称
         */
        private String feedbackStatusName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人ID
         */
        private String createUserId;

        /**
         * 创建人
         */
        private String createUserName;

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
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源明细ID
        */
        private String sourceDetailId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 达人昵称
        */
        private String partnerNickname;

        /**
        * 达人ID
        */
        private String partnerId;

        /**
        * 回片链接（完整链接）
        */
        private String url;

        /**
        * 回片链接哈希值（MD5或SHA256，用于唯一键）
        */
        private String urlHash;

        /**
        * 发布形式
        */
        private String publishType;

        /**
        * 发布日期
        */
        private LocalDate publishDate;

        /**
         * 备注
         */
        private String remark;

        /**
         * 回片状态 com.erp.model.oms.enums.FeedbackStatusEnum
         */
        private String feedbackStatus;


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
        * 来源单号
        */
        @Size(max = 100,message = "来源单号最大长度不能超过100位")
        private String sourceCode;

        /**
        * 来源ID
        */
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源明细ID
        */
        @Size(max = 19,message = "来源明细ID最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 来源类型
        */
        @Size(max = 200,message = "来源类型最大长度不能超过200位")
        private String sourceType;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 达人昵称
        */
        @Size(max = 100,message = "达人昵称最大长度不能超过100位")
        private String partnerNickname;

        /**
        * 达人ID
        */
        @NotBlank(message = "达人ID不能为空")
        @Size(max = 19,message = "达人ID最大长度不能超过19位")
        private String partnerId;

        /**
        * 回片链接（完整链接）
        */
        @NotBlank(message = "回片链接不能为空")
        private String url;

        /**
        * 回片链接哈希值（MD5或SHA256，用于唯一键）
        */
        @Size(max = 64,message = "回片链接哈希值（MD5或SHA256，用于唯一键）最大长度不能超过64位")
        private String urlHash;

        /**
        * 发布形式
        */
        @Size(max = 100,message = "发布形式最大长度不能超过100位")
        private String publishType;

        /**
        * 发布日期
        */
        private LocalDate publishDate;

        /**
         * 备注
         */
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;

        /**
         * 回片状态 com.erp.model.oms.enums.FeedbackStatusEnum
         */
        @Size(max = 50,message = "回片状态最大长度不能超过50位")
        private String feedbackStatus;


    }


}