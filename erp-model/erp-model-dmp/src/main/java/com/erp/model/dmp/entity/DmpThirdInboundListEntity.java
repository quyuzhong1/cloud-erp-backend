package com.erp.model.dmp.entity;

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
 * 第三方仓库存
 * </p>
 *
 * @author lrp
 * @since 2025-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_inbound_list")
public class DmpThirdInboundListEntity extends BaseEntity<DmpThirdInboundListEntity> {

    /**
    * 仓库平台类型
    */
    @TableField("warehouse_platform_type")
    private String warehousePlatformType;
    /**
    * 来源平台（编码）：goodcang、iml
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 入库单号
    */
    @TableField("receiving_code")
    private String receivingCode;
    /**
    * 入库单状态
    */
    @TableField("receiving_status")
    private String receivingStatus;
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
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;


    public static final String WAREHOUSE_PLATFORM_TYPE = "warehouse_platform_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String RECEIVING_CODE = "receiving_code";

    public static final String RECEIVING_STATUS = "receiving_status";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}