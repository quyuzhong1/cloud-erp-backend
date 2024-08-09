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
 * 销售预入库主表
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_prestock_info")
public class DmpSoPrestockInfoEntity extends BaseEntity<DmpSoPrestockInfoEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 审核时间
    */
    @TableField("check_time")
    private LocalDateTime checkTime;
    /**
    * 来源系统
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 第三方单号
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 物流单号
    */
    @TableField("logistics_no")
    private String logisticsNo;
    /**
    * 物流名称
    */
    @TableField("logistics_name")
    private String logisticsName;
    /**
    * 货品数量
    */
    @TableField("goods_count")
    private Integer goodsCount;
    /**
    * 货品种类数
    */
    @TableField("goods_type_count")
    private Integer goodsTypeCount;
    /**
    * 入库人姓名
    */
    @TableField("operator_name")
    private String operatorName;
    /**
    * 审核员姓名
    */
    @TableField("checker_name")
    private String checkerName;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库编号
    */
    @TableField("warehouse_no")
    private String warehouseNo;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
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


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String CHECK_TIME = "check_time";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String LOGISTICS_NO = "logistics_no";

    public static final String LOGISTICS_NAME = "logistics_name";

    public static final String GOODS_COUNT = "goods_count";

    public static final String GOODS_TYPE_COUNT = "goods_type_count";

    public static final String OPERATOR_NAME = "operator_name";

    public static final String CHECKER_NAME = "checker_name";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NO = "warehouse_no";

    public static final String REMARK = "remark";

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