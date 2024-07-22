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
 * 产品spu信息
 * </p>
 *
 * @author shukai
 * @since 2024-07-22
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_product_info")
public class DmpProductInfoEntity extends BaseEntity<DmpProductInfoEntity> {

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
    * 来源系统：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 产品id
    */
    @TableField("spu_id")
    private String spuId;
    /**
    * 产品编码
    */
    @TableField("spu_no")
    private String spuNo;
    /**
    * 产品名称
    */
    @TableField("spu_name")
    private String spuName;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SPU_ID = "spu_id";

    public static final String SPU_NO = "spu_no";

    public static final String SPU_NAME = "spu_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}