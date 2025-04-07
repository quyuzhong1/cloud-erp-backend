package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台物流单明细表
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_logistics_detail")
public class DmpSoLogisticsDetailEntity extends BaseEntity<DmpSoLogisticsDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 第三方物流明细id
    */
    @TableField("third_logistics_detail_id")
    private String thirdLogisticsDetailId;
    /**
    * 第三方明细创建时间
    */
    @TableField("third_detail_create_time")
    private LocalDateTime thirdDetailCreateTime;
    /**
    * 第三方明细更新时间
    */
    @TableField("third_detail_update_time")
    private LocalDateTime thirdDetailUpdateTime;
    /**
    * 轨迹状态
    */
    @TableField("track_status")
    private String trackStatus;
    /**
    * 数据状态(已创建，已更新，已删除等)
    */
    @TableField("data_status")
    private String dataStatus;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;


    public static final String MAIN_ID = "main_id";

    public static final String THIRD_LOGISTICS_DETAIL_ID = "third_logistics_detail_id";

    public static final String THIRD_DETAIL_CREATE_TIME = "third_detail_create_time";

    public static final String THIRD_DETAIL_UPDATE_TIME = "third_detail_update_time";

    public static final String TRACK_STATUS = "track_status";

    public static final String DATA_STATUS = "data_status";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}