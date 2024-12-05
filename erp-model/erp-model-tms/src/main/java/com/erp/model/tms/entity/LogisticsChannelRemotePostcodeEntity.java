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
 * 渠道邮编组设置表
 * </p>
 *
 * @author jack
 * @since 2024-12-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_channel_remote_postcode")
public class LogisticsChannelRemotePostcodeEntity extends BaseEntity<LogisticsChannelRemotePostcodeEntity> {

    /**
    * 渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 邮编组id
    */
    @TableField("remote_postcode_id")
    private String remotePostcodeId;
    /**
    * 类型，all全部，part部分
    */
    @TableField("type")
    private String type;


    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String REMOTE_POSTCODE_ID = "remote_postcode_id";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}