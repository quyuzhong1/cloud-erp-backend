package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.enums.SeparateRuleEnum;
import com.erp.model.wms.enums.StocktakingModeEnum;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 盘点计划表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
*/
@Data
@NoArgsConstructor
public class StocktakingPlanDTO implements Serializable {


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

         /**
          * 类型
          */
         private String tabFlagName;

     }
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
          * 盘点计划单号
          */
         private String code;
         /**
          * 盘点计划名称
          */
         private String name;

        /**
         * 单据审核状态
        */
         private List<ApproveStatusEnum> approveStatusList;
         /**
          * 盘点方式
          */
         private List<StocktakingModeEnum> modeList;
         /**
          * 盘点状态
          */
         private List<StocktakingStatusEnum> statusList;
         /**
          * 盘点类型
          */
         private List<StocktakingTypeEnum> typeList;
         /**
          * 分单规则
          */
        private List<SeparateRuleEnum> separateRuleList;
         /**
          * 提交人id列表
          */
         private List<String> submitUserIdList;
         /**
          * 提交审核时间列表
          */
         private List<LocalDateTime> submitTimeList;
         /**
          * tab标识
          */
         private String tabFlag;
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
        private String  id;

        /**
        * 盘点计划单号
        */
        private String code;

        /**
        * 盘点计划名称
        */
        private String name;

        /**
        * 单据审核状态
        */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据审核状态名称
         */
        private String approveStatusName;

        /**
        * 盘点状态
        */
        private StocktakingStatusEnum status;

        /**
         * 盘点状态名称
         */
        private String statusName;

        /**
        * 盘点方式
        */
        private StocktakingModeEnum mode;

        /**
         * 盘点方式名称
         */
        private String modeName;

        /**
        * 分单规则
        */
        private SeparateRuleEnum separateRule;

        /**
         * 分单规则名称
         */
        private String separateRuleName;

        /**
        * 盘点类型
        */
        private StocktakingTypeEnum type;

        /**
         * 盘点日期
         */
        private LocalDate stocktakingDate;

        /**
         * 盘点类型名称
         */
        private String typeName;

        /**
        * 提交审核时间
        */
        private LocalDateTime submitTime;

        /**
        * 提交审核人id
        */
        private String submitUserId;

        /**
        * 提交审核人名称
        */
        private String submitUserName;
        /**
         * 待审核人
         */
        private String waitApproveUserName;
        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 动销开始时间
        */
        private LocalDateTime startTime;

        /**
        * 动销结束时间
        */
        private LocalDateTime endTime;

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
        * 盘点计划单号
        */
        private String code;

        /**
        * 盘点计划名称
        */
        private String name;

        /**
        * 单据审核状态
        */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据审核状态名称
         */
        private String approveStatusName;

        /**
        * 盘点状态
        */
        private StocktakingStatusEnum status;

        /**
         * 盘点状态名称
         */
        private String statusName;

        /**
        * 盘点方式
        */
        private StocktakingModeEnum mode;

        /**
         * 盘点方式名称
         */
        private String modeName;

        /**
        * 分单规则
        */
        private SeparateRuleEnum separateRule;

        /**
         * 分单规则名称
         */
        private String separateRuleName;

        /**
         * 盘点日期
         */
        private LocalDate stocktakingDate;

        /**
        * 盘点类型
        */
        private StocktakingTypeEnum type;

        /**
         * 盘点类型名称
         */
        private String typeName;

        /**
        * 提交审核时间
        */
        private LocalDateTime submitTime;

        /**
        * 提交审核人id
        */
        private String submitUserId;

        /**
        * 提交审核人名称
        */
        private String submitUserName;

        /**
        * 最后审核人id
        */
        private String approveUserId;

        /**
        * 最后审核人名称
        */
        private String approveUserName;

        /**
        * 最后审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 动销开始时间
        */
        private LocalDateTime startTime;

        /**
        * 动销结束时间
        */
        private LocalDateTime endTime;

        /**
         * 明细列表
         */
        private List<StocktakingPlanDetailDTO.ViewDTO> detailList;

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
         * 盘点日期
         */
        @NotNull(message = "盘点日期不能为空")
        private LocalDate stocktakingDate;

        /**
        * 盘点计划名称
        */
        @NotBlank(message = "盘点计划名称不能为空")
        @Size(max = 80,message = "盘点计划名称最大长度不能超过80位")
        private String name;

        /**
        * 盘点方式
        */
        @NotNull(message = "盘点方式不能为空")
        private StocktakingModeEnum mode;

        /**
        * 分单规则
        */
        @NotNull(message = "分单规则不能为空")
        private SeparateRuleEnum separateRule;

        /**
        * 盘点类型
        */
        @NotNull(message = "盘点类型不能为空")
        private StocktakingTypeEnum type;

        /**
         * 动销开始时间
         */
        private LocalDateTime startTime;

        /**
         * 动销结束时间
         */
        private LocalDateTime endTime;

        /**
         * 盘点计划明细
         */
        @Valid
        @NotEmpty(message = "盘点计划明细不能为空")
        private List<DetailDTO> detailList;


    }

    @Data
    public static class DetailDTO{
        private String id;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;
        /**
         * 库区
         */
        private String warehouseArea;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 仓位id
         */
        private String warehouseLocationId;

        /**
         * 库存id
         */
        private String inventoryId;
    }

    @Data
    public static class AllowPushDTO{
        /**
         * id
         */
        private String id;
    }
}