package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.enums.SeparateRuleEnum;
import com.erp.model.wms.enums.StocktakingModeEnum;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 盘点计划表
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
*/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("stocktaking_plan")
public class StocktakingPlanEntity extends BaseEntity<StocktakingPlanEntity> {


    /**
    * 盘点计划单号
    */
    @TableField("code")
    private String code;

    /**
    * 盘点计划名称
    */
    @TableField("name")
    private String name;

    /**
    * 单据审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
    * 盘点状态
    */
    @TableField("status")
    private StocktakingStatusEnum status;

    /**
    * 盘点方式
    */
    @TableField("mode")
    private StocktakingModeEnum mode;

    /**
    * 分单规则
    */
    @TableField("separate_rule")
    private SeparateRuleEnum separateRule;

    /**
    * 盘点类型
    */
    @TableField("type")
    private StocktakingTypeEnum type;

    /**
    * 提交审核时间
    */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
    * 提交审核人id
    */
    @TableField("submit_user_id")
    private String submitUserId;

    /**
    * 提交审核人名称
    */
    @TableField("submit_user_name")
    private String submitUserName;

    /**
    * 最后审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
    * 最新审核人名称
    */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
    * 最新审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
    * 动销开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
    * 动销结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;


    

    

    public static final String APPROVE_STATUS = "approve_status";

    

    

    public static final String SEPARATE_RULE = "separate_rule";

    

    public static final String SUBMIT_TIME = "submit_time";

    public static final String SUBMIT_USER_ID = "submit_user_id";

    public static final String SUBMIT_USER_NAME = "submit_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public StocktakingPlanEntity(StocktakingPlanDTO.AddDTO addDTO, String code) {
        this.code = code;
        this.name = addDTO.getName();
        if (addDTO.getStartTime() != null && addDTO.getEndTime() != null){
            this.startTime = addDTO.getStartTime();
            this.endTime = addDTO.getEndTime();
        }
        this.mode = addDTO.getMode();
        this.type = addDTO.getType();
        this.separateRule = addDTO.getSeparateRule();
    }

    public StocktakingPlanEntity(StocktakingPlanDTO.UpdateDTO updateDTO) {
        super(updateDTO.getId());
        this.name = updateDTO.getName();
        if (updateDTO.getStartTime() != null && updateDTO.getEndTime() != null){
            this.startTime = updateDTO.getStartTime();
            this.endTime = updateDTO.getEndTime();
        }
        this.mode = updateDTO.getMode();
        this.type = updateDTO.getType();
        this.separateRule = updateDTO.getSeparateRule();
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}