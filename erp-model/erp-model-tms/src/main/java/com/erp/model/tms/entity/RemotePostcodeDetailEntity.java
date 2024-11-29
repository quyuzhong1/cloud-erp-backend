package com.erp.model.tms.entity;

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
 * 偏远邮编明细表
 * </p>
 *
 * @author jack
 * @since 2024-11-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("remote_postcode_detail")
public class RemotePostcodeDetailEntity extends BaseEntity<RemotePostcodeDetailEntity> {

    /**
    * 主表id 
    */
    @TableField("main_id")
    private String mainId;
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * code 
    */
    @TableField("code")
    private String code;
    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 匹配类型dict_basic表matchType: preciseMatch=精准匹配, prefixMatch=匹配前缀, suffixMatch=匹配后缀, fuzzyMatch=模糊匹配  枚举：RemotePostcodeDetailMatchTypeEnum
    */
    @TableField("match_type")
    private String matchType;
    /**
    * 邮编
    */
    @TableField("post_code")
    private String postCode;


    public static final String MAIN_ID = "main_id";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String COUNTRY = "country";

    public static final String CITY = "city";

    public static final String MATCH_TYPE = "match_type";

    public static final String POST_CODE = "post_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
