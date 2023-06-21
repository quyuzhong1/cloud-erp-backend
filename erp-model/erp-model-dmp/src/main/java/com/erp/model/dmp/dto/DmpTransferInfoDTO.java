package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 直接调拨单请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
*/
@Data
@NoArgsConstructor
public class DmpTransferInfoDTO implements Serializable {


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
         private String searchType;

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
         * 搜索类型
         */
         private String  searchType;

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
        * 单据编号
        */
        private String code;

        /**
        * 单据类型
        */
        private String type;

        /**
        * 调拨类型
        */
        private String transferType;

        /**
        * 调拨类型编码
        */
        private String transferTypeCode;

        /**
        * 调入组织id
        */
        private String inOrgId;

        /**
        * 调入组织名称
        */
        private String inOrgName;

        /**
        * 调出库存组织id
        */
        private String outOrgId;

        /**
        * 调出库存组织名称
        */
        private String outOrgName;

        /**
        * 单据日期
        */
        private LocalDateTime billDate;

        /**
        * 单据状态
        */
        private String approveStatus;

        /**
        * 调拨方向
        */
        private String transferDirection;

        /**
        * 平台创建人
        */
        private String platformCreateUserName;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 审核人
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废人
        */
        private String invalidUserName;

        /**
        * 最后修改时间
        */
        private LocalDateTime lastUpdatedTime;

        /**
        * 最后修改人
        */
        private String lastUpdatedUserName;

        /**
        * 第三方单据id
        */
        private String sourceId;

        /**
        * 来源平台 马帮，管易，金蝶等
        */
        private String plaformSign;

        /**
        * 同步马帮状态
        */
        private String syncMbStatus;

        /**
        * 最新同步时间
        */
        private LocalDateTime lastSyncMbTime;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

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
        * 单据编号
        */
        private String code;

        /**
        * 单据类型
        */
        private String type;

        /**
        * 调拨类型
        */
        private String transferType;

        /**
        * 调拨类型编码
        */
        private String transferTypeCode;

        /**
        * 调入组织id
        */
        private String inOrgId;

        /**
        * 调入组织名称
        */
        private String inOrgName;

        /**
        * 调出库存组织id
        */
        private String outOrgId;

        /**
        * 调出库存组织名称
        */
        private String outOrgName;

        /**
        * 单据日期
        */
        private LocalDateTime billDate;

        /**
        * 单据状态
        */
        private String approveStatus;

        /**
        * 调拨方向
        */
        private String transferDirection;

        /**
        * 平台创建人
        */
        private String platformCreateUserName;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 审核人
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废人
        */
        private String invalidUserName;

        /**
        * 最后修改时间
        */
        private LocalDateTime lastUpdatedTime;

        /**
        * 最后修改人
        */
        private String lastUpdatedUserName;

        /**
        * 第三方单据id
        */
        private String sourceId;

        /**
        * 来源平台 马帮，管易，金蝶等
        */
        private String plaformSign;

        /**
        * 同步马帮状态
        */
        private String syncMbStatus;

        /**
        * 最新同步时间
        */
        private LocalDateTime lastSyncMbTime;


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
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 32,message = "单据类型最大长度不能超过32位")
        private String type;

        /**
        * 调拨类型
        */
        @NotBlank(message = "调拨类型不能为空")
        @Size(max = 32,message = "调拨类型最大长度不能超过32位")
        private String transferType;

        /**
        * 调拨类型编码
        */
        @NotBlank(message = "调拨类型编码不能为空")
        @Size(max = 32,message = "调拨类型编码最大长度不能超过32位")
        private String transferTypeCode;

        /**
        * 调入组织id
        */
        @NotBlank(message = "调入组织id不能为空")
        @Size(max = 32,message = "调入组织id最大长度不能超过32位")
        private String inOrgId;

        /**
        * 调入组织名称
        */
        @NotBlank(message = "调入组织名称不能为空")
        @Size(max = 128,message = "调入组织名称最大长度不能超过128位")
        private String inOrgName;

        /**
        * 调出库存组织id
        */
        @NotBlank(message = "调出库存组织id不能为空")
        @Size(max = 32,message = "调出库存组织id最大长度不能超过32位")
        private String outOrgId;

        /**
        * 调出库存组织名称
        */
        @NotBlank(message = "调出库存组织名称不能为空")
        @Size(max = 128,message = "调出库存组织名称最大长度不能超过128位")
        private String outOrgName;

        /**
        * 单据日期
        */
        private LocalDateTime billDate;

        /**
        * 调拨方向
        */
        @NotBlank(message = "调拨方向不能为空")
        @Size(max = 32,message = "调拨方向最大长度不能超过32位")
        private String transferDirection;

        /**
        * 平台创建人
        */
        @NotBlank(message = "平台创建人不能为空")
        @Size(max = 32,message = "平台创建人最大长度不能超过32位")
        private String platformCreateUserName;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废人
        */
        @NotBlank(message = "作废人不能为空")
        @Size(max = 32,message = "作废人最大长度不能超过32位")
        private String invalidUserName;

        /**
        * 最后修改时间
        */
        @NotNull(message = "最后修改时间不能为空")
        private LocalDateTime lastUpdatedTime;

        /**
        * 最后修改人
        */
        @NotBlank(message = "最后修改人不能为空")
        @Size(max = 32,message = "最后修改人最大长度不能超过32位")
        private String lastUpdatedUserName;

        /**
        * 第三方单据id
        */
        @NotBlank(message = "第三方单据id不能为空")
        @Size(max = 32,message = "第三方单据id最大长度不能超过32位")
        private String sourceId;

        /**
        * 来源平台 马帮，管易，金蝶等
        */
        @NotBlank(message = "来源平台 马帮，管易，金蝶等不能为空")
        @Size(max = 64,message = "来源平台 马帮，管易，金蝶等最大长度不能超过64位")
        private String plaformSign;

        /**
        * 同步马帮状态
        */
        @NotBlank(message = "同步马帮状态不能为空")
        @Size(max = 16,message = "同步马帮状态最大长度不能超过16位")
        private String syncMbStatus;

        /**
        * 最新同步时间
        */
        @NotNull(message = "最新同步时间不能为空")
        private LocalDateTime lastSyncMbTime;


    }


}