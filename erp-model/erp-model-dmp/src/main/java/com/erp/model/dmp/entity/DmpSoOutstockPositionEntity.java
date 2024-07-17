package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台销售订单出库详情
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_outstock_position")
public class DmpSoOutstockPositionEntity extends BaseEntity<DmpSoOutstockPositionEntity> {

    /**
    * 主id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 批次号
    */
    @TableField("batch_no")
    private String batchNo;
    /**
    * 有效期
    */
    @TableField("expire_date")
    private String expireDate;
    /**
    * 销售出库单详情id
    */
    @TableField("stockout_detail_id")
    private String stockoutDetailId;
    /**
    * 货位号
    */
    @TableField("position_no")
    private String positionNo;
    /**
    * 当前货位出库货品总量
    */
    @TableField("position_goods_count")
    private Integer positionGoodsCount;
    /**
    * 货位明细id
    */
    @TableField("rec_id")
    private String recId;
    /**
    * 货位id
    */
    @TableField("position_id")
    private String positionId;
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


    public static final String MAIN_ID = "main_id";

    public static final String BATCH_NO = "batch_no";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String STOCKOUT_DETAIL_ID = "stockout_detail_id";

    public static final String POSITION_NO = "position_no";

    public static final String POSITION_GOODS_COUNT = "position_goods_count";

    public static final String REC_ID = "rec_id";

    public static final String POSITION_ID = "position_id";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}