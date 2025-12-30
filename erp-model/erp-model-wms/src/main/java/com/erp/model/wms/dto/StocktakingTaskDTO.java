package com.erp.model.wms.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.enums.SeparateRuleEnum;
import com.erp.model.wms.enums.StocktakingModeEnum;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 盘点任务
 *
 * @author Lambda
 * @Classname StocktakingDTO
 * @Description
 * @Date 2023-07-31 14:29
 * @Created by yl
 */

public class StocktakingTaskDTO implements Serializable {

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabDTO {
        /**
         * 标识
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
        /**
         * 标识
         */
        private String tabFlagName;

    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class CheckResultDTO {

        /**
         * 任务id
         */
        private String id;
        /**
         * 任务单号
         */
        private String code;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 数量
         */
        private Integer qty;

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
        private Map<String, String> sqlMap;
    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 盘点任务单号
         */
        private String code;

        /**
         * 盘点计划单号
         */
        private String sourceCode;

        /**
         * 盘点方式编码
         */
        private StocktakingModeEnum stocktakingMode;

        /**
         * 盘点方式名
         */
        private String stocktakingModeName;

        /**
         * 盘点类型
         */
        private StocktakingTypeEnum stocktakingType;

        /**
         * 盘点类型名
         */
        private String stocktakingTypeName;

        /**
         * 单据状态编码
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 分单规则编码
         */
        private SeparateRuleEnum separateRule;

        /**
         * 分单规则编码名
         */
        private String separateRuleName;

        /**
         * 盘点状态
         */
        private StocktakingStatusEnum stocktakingStatus;


        /**
         * 盘点状态名
         */
        private String stocktakingStatusName;



        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * sku 统计数
         */
        private Integer skuCount;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 盘点人
         */
        private String stocktakingUserName;

        /**
         * 待审核人
         */
        private String waitApproveUserName;

        /**
         * 最新审核人
         */
        private String approveUserName;


    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 盘点任务单号
         */
        private String code;

        /**
         * 盘点计划单号
         */
        private String sourceCode;

        /**
         * 盘点方式编码
         */
        private StocktakingModeEnum stocktakingMode;

        /**
         * 盘点方式名
         */
        private String stocktakingModeName;

        /**
         * 盘点类型
         */
        private StocktakingTypeEnum stocktakingType;

        /**
         * 盘点类型名
         */
        private String stocktakingTypeName;

        /**
         * 单据状态编码
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 分单规则编码
         */
        private SeparateRuleEnum separateRule;

        /**
         * 分单规则编码名
         */
        private String separateRuleName;

        /**
         * 盘点状态
         */
        private StocktakingStatusEnum stocktakingStatus;


        /**
         * 盘点状态名
         */
        private String stocktakingStatusName;


        /**
         * 盘点人
         */
        private String stocktakingUserName;


        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 详情列表
         */
        private List<StocktakingTaskDetailDTO.ViewDTO> detailList;


    }


    /**
     * 导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }

    /**
     * 分配盘点人
     */
    @Data
    @NoArgsConstructor
    public static class AssignUserDTO {
        /**
         * id 集合
         */
        @NotNull(message ="盘点任务不能为空")
        @Size(min = 1, message = "至少需要选择一个盘点任务")
        private List<String> ids;


        /**
         * 分配用户集合
         */
        @NotNull(message ="分配用户不能为空")
        @Size(min = 1, message = "至少需要选择一个分配用户")
        private List<String> userIdList;
    }
    /**
     * 盘点任务
     */
    @Data
    @NoArgsConstructor
    public static class CreateDTO{
        /**
         * 单号
         */
        private String code;

        /**
         * 盘点状态
         */
        private StocktakingStatusEnum status;

        /**
         * 来源id 来源盘点计划
         */
        private String sourceId;

        /**
         * 来源code 来源盘点计划code
         */
        private String sourceCode;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;
    }

    @Data
    @NoArgsConstructor
    public static class CreateDetailDTO{

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库位编码
         */
        private String warehouseLocation;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 可用库存
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;
    }

    @Data
    @AllArgsConstructor
    public static class BaseIdDTO extends PagingParamDTO {

//        @NotBlank(message = "id不能为空")
        private String id;

        private String name;

        public BaseIdDTO() {
        }

        public String checkAndGetMainId() {
            String mainId = this.getId();
            // 兼容前端高级查询传参
            if (StringUtils.isBlank(mainId)){
                Object idsObj = super.getAdvanceQueryDTOList()
                        .stream()
                        .filter(e -> "st.id".equalsIgnoreCase(e.getField()))
                        .map(AdvanceQueryDTO::getValue)
                        .findFirst().orElse(null);
                if (null == idsObj){
                    throw new ServiceException("盘点任务ID为空");
                }
                JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(idsObj));
                if (CollectionUtils.isEmpty(jsonArray)){
                    throw new ServiceException("盘点任务IDS为空");
                }
                if (jsonArray.size() > 1){
                    throw new ServiceException("盘点任务明细导出IDS数量不能超过1");
                }
                mainId = jsonArray.get(0).toString();
            }
            if (StringUtils.isBlank(mainId)){
                throw new ServiceException("盘点任务ID不能为空");
            }
            return mainId;
        }

    }
}
