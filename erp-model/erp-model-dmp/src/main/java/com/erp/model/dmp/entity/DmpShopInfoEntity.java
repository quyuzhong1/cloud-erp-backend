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
 * 中台店铺表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_shop_info")
public class DmpShopInfoEntity extends BaseEntity<DmpShopInfoEntity> {

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
    * 来源类型：管易，金蝶，马帮
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 单据编号（唯一）
    */
    @TableField("bill_code")
    private String billCode;
    /**
    * 销售平台原始单号
    */
    @TableField("platform_original_code")
    private String platformOriginalCode;
    /**
    * 平台店铺账户
    */
    @TableField("account_user_name")
    private String accountUserName;
    /**
    * 店铺名称
    */
    @TableField("name")
    private String name;
    /**
    * site
站点
    */
    @TableField("site")
    private String site;
    /**
    * 店铺状态:true 禁用 false 启用
    */
    @TableField("disabled")
    private String disabled;
    /**
    * 企业id
    */
    @TableField("company_id")
    private String companyId;
    /**
    * 企业名称
    */
    @TableField("company_name")
    private String companyName;
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


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String BILL_CODE = "bill_code";

    public static final String PLATFORM_ORIGINAL_CODE = "platform_original_code";

    public static final String ACCOUNT_USER_NAME = "account_user_name";

    public static final String NAME = "name";

    public static final String SITE = "site";

    public static final String DISABLED = "disabled";

    public static final String COMPANY_ID = "company_id";

    public static final String COMPANY_NAME = "company_name";

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