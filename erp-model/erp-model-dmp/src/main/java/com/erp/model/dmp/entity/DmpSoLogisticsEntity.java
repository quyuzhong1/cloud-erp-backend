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
 * 中台物流单主表
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_logistics")
public class DmpSoLogisticsEntity extends BaseEntity<DmpSoLogisticsEntity> {

    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 第三方物流id
    */
    @TableField("third_logistics_id")
    private String thirdLogisticsId;
    /**
    * 第三方物流单据编号
    */
    @TableField("third_logistics_code")
    private String thirdLogisticsCode;
    /**
    * 第三方创建时间
    */
    @TableField("third_create_time")
    private LocalDateTime thirdCreateTime;
    /**
    * 第三方更新时间
    */
    @TableField("third_update_time")
    private LocalDateTime thirdUpdateTime;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 签收时间
    */
    @TableField("sign_time")
    private LocalDateTime signTime;
    /**
    * 出库单号
    */
    @TableField("outstock_code")
    private String outstockCode;
    /**
    * 物流商编码
    */
    @TableField("logistic_company_code")
    private String logisticCompanyCode;
    /**
    * 物流商名称
    */
    @TableField("logistic_company_name")
    private String logisticCompanyName;
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

    /**
     * 店铺对应客户编号
     */
    @TableField("shop_no")
    private String shopNo;

    /**
     * 店铺对应客户名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 销售平台单号
     */
    @TableField("order_platform_code")
    private String orderPlatformCode;

    /**
     * 平台类型
     */
    @TableField("platform_type")
    private String platformType;


    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_LOGISTICS_ID = "third_logistics_id";

    public static final String THIRD_LOGISTICS_CODE = "third_logistics_code";

    public static final String THIRD_CREATE_TIME = "third_create_time";

    public static final String THIRD_UPDATE_TIME = "third_update_time";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String SIGN_TIME = "sign_time";

    public static final String OUTSTOCK_CODE = "outstock_code";

    public static final String LOGISTIC_COMPANY_CODE = "logistic_company_code";

    public static final String LOGISTIC_COMPANY_NAME = "logistic_company_name";

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