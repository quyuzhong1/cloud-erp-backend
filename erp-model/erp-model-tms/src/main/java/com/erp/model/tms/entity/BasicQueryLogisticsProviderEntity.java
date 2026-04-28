package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 查询物流商信息表
 * </p>
 *
 * @author jack
 * @since 2026-03-31
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("basic_query_logistics_provider")
public class BasicQueryLogisticsProviderEntity extends BaseEntity<BasicQueryLogisticsProviderEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 物流商名称（中文）
    */
    @TableField("logistics_name_cn")
    private String logisticsNameCn;
    /**
    * 物流商名称（英文）
    */
    @TableField("logistics_name_en")
    private String logisticsNameEn;
    /**
    * 公司编码
    */
    @TableField("company_code")
    private String companyCode;
    /**
    * 查询平台：TRACK123=Track123,KUAIDI100=快递100  枚举：TrackPlatformTypeEnum
    */
    @TableField("track_platform_type")
    private String trackPlatformType;
    /**
    * 是否注册手机号
    */
    @TableField("is_register_phone")
    private Boolean isRegisterPhone;


    public static final String REMARK = "remark";

    public static final String LOGISTICS_NAME_CN = "logistics_name_cn";

    public static final String LOGISTICS_NAME_EN = "logistics_name_en";

    public static final String COMPANY_CODE = "company_code";

    public static final String TRACK_PLATFORM_TYPE = "track_platform_type";

    public static final String IS_REGISTER_PHONE = "is_register_phone";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
