package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 银行信息表
 * </p>
 *
 * @author wuht
 * @since 2025-07-31
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_bank_info")
public class DmpBankInfoEntity extends BaseEntity<DmpBankInfoEntity> {

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
    * 来源系统
    */
    @TableField("source_system")
    private String sourceSystem;
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
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 银行编号
    */
    @TableField("bank_no")
    private String bankNo;
    /**
    * 银行名称
    */
    @TableField("bank_name")
    private String bankName;
    /**
    * 银行类型
    */
    @TableField("bank_type")
    private String bankType;
    /**
    * 省份
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 区县
    */
    @TableField("district")
    private String district;
    /**
    * 创建组织ID
    */
    @TableField("create_org_id")
    private String createOrgId;
    /**
    * 使用组织ID
    */
    @TableField("use_org_id")
    private String useOrgId;
    /**
    * 银行地址
    */
    @TableField("bank_address")
    private String bankAddress;
    /**
    * 银行描述
    */
    @TableField("bank_description")
    private String bankDescription;
    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private String disabled;

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String CREATE_USER_NAME = "create_user_name";

    public static final String CREATE_USER_ID = "create_user_id";

    public static final String UPDATE_USER_NAME = "update_user_name";

    public static final String UPDATE_USER_ID = "update_user_id";

    public static final String VERSION = "version";

    public static final String IS_DELETED = "is_deleted";

    public static final String SOURCE_ID = "source_id";

    public static final String BANK_NO = "bank_no";

    public static final String BANK_NAME = "bank_name";

    public static final String BANK_TYPE = "bank_type";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT = "district";

    public static final String CREATE_ORG_ID = "create_org_id";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String BANK_ADDRESS = "bank_address";

    public static final String BANK_DESCRIPTION = "bank_description";

    public static final String COUNTRY = "country";

    public static final String STATUS = "status";

    public static final String DISABLED = "disabled";

    /**
     * 测试方法，用于验证类是否可以被正常加载
     */
    public static boolean isClassLoaded() {
        return true;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
