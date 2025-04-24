package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 盘点任务表
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@TableName("stocktaking_task")
public class StocktakingTaskEntity extends BaseEntity<StocktakingTaskEntity> {

    /**
     * 单号
     */
    @TableField("code")
    private String code;

    /**
     * 盘点状态
     */
    @TableField("status")
    private StocktakingStatusEnum status;

    /**
     * 来源id 来源盘点计划
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源code 来源盘点计划code
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;


    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核人名
     */
    @TableField("approve_user_name")
    private String approveUserName;


    

    

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String APPROVE_STATUS = "approve_status";

    public StocktakingTaskEntity(StocktakingPlanEntity entity, String code) {
        this.code = code;
        this.status = StocktakingStatusEnum.NOT_STARTED;
        this.sourceId = entity.getId();
        this.sourceCode = entity.getCode();
        this.approveStatus = ApproveStatusEnum.WAIT_SUBMIT;
    }

    public StocktakingTaskEntity(StocktakingPlanEntity entity, String code, String uid, String username) {
        this(entity, code);
        super.setCreateUserId(uid);
        super.setCreateUserName(username);
        super.setUpdateUserId(uid);
        super.setUpdateUserName(username);
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
