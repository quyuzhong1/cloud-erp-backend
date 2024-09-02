package com.erp.model.plm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Classname ProjectTaskDTO

 * @Date 2022-09-22 15:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectTaskDTO  implements Serializable {


    /**
     * 任务id
     */
    private String id;

    /**
     * 项目id
     */
    private String projectId;


    /**
     * 父级id  保存子任务需要传
     */
    private String pid;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;


    /**
     * 任务名
     */
    @NotBlank(message = "任务名不能为空")
    @Size(max = 50,message = "任务名最大50字符")
    private String name;



    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    @StateEnumValue(intValues = {0, 1}, message = "任务类型只能是0或者1")
    private Integer type;

    /**
     * 负责人id
     */
    @NotNull(message = "任务负责人集合不能为空")
    @Size(min = 1,message = "负责人至少有一个")
    private List<String> chargeIds;

    /**
     * 前置任务id
     */
    private List<String> preTaskIdList;

    /**
     * 计划开始时间
     */

    private LocalDate planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDate planEndTime;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private Integer priority;

    /**
     * 任务阶段id
     */
    private String phaseId;

    /**
     * 任务阶段名
     */
    private String phaseName;


    /**
     * 任务描述
     */
    private String description;

    /**
     *流程id 审核任务用到
     */
    private String processId;

    /**
     * 业务流程表id
     */
    private String businessProcessId;

    /**
     * 业务流程名
     */
    private String businessName="";
    
    /**
     * 状态
     */
    private Integer status;


    /**
     * 排期状态
     */
    private String scheduleStatus;

    /**
     * 交付文档审核分配人集合
     */
    private List<TaskChargeDistributionDTO> approvalList;

    /**
     * 交付文档
     *
     */
    @Valid
    private List<DocsDTO>  deliveryDocsList;

    /**
     * 设置里程碑(0否，1是)
     */
    private Integer isMilepost;


    /**
     * 关联sku 表id集合
     */
    private List<String> refSkuIdList;

    /**
     * 关联sku 表sku 名字集合
     */
    private List<String> refSkuNoList;


    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    private String fieldConfigType;



    /**
     * 勾选字段后的json 字段
     */
    private String fieldJson;


    /**
     * sku 完成信息
     */
    private List<Map<String,Object>> refSkuFinishList;


    /**
     * 交付文档名称（逗号分隔，用于操作日志）
     */
    private String  deliveryDocsNames;
    /**
     * 前置任务名称（逗号分隔，用于操作 日志）
     */
    private String preTaskNames;

    /**
     * 分配类型，由模板生成时带过来（0角色，1人员）
     */
    private Integer distributionType;

    /**
     * 是否是固定任务 1是  0  不是
     */
    private Integer isFixed;

    /**
     * 关联sku类型,RelatedSkuTypeEnum枚举(1，自动关联，2选择关联，3不关联)
     */
    private String relatedSkuType;

    /**
     * 工期
     */
    private Integer workPeriod;

    /**
     * 辅助字段：project导入任务ID
     */
    private String projectTaskId;


    /**
     * 任务关注人集合
     */
    private List<String> concernUserIdList;

    @Data
    public static class PagingParamDTO{
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 只能查询已添加sku关联SPU下的任务
         */
        @NotEmpty(message = "请选择SKU后才能添加任务")
        private List<String> skuIdList;
    }

    @Data
    public static class SimpleViewDTO{
        /**
         * 任务ID
         */
        private String id;
        /**
         * 任务名称
         */
        private String name;
        /**
         * 任务负责人ID
         */
        private String chargeId;
        /**
         * 任务负责人名称
         */
        private String chargeName;
        /**
         * 阶段ID
         */
        private String phaseId;
        /**
         * 阶段名称
         */
        private String phaseName;
        /**
         * 产品ID
         */
        private String productId;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 任务状态
         */
        private int status;
        /**
         * 任务状态名称
         */
        private String statusName;
        /**
         * spu编码
         */
        private String spuNo;

    }
}
