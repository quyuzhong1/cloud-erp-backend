package com.erp.model.sys.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;


/**
 * <p>
 * 通知配置明细表
 * </p>
 *
 * @author will
 * @since 2025-02-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_notice_detail")
public class CfgNoticeDetailEntity extends BaseEntity<CfgNoticeDetailEntity> {

    /**
    * 通知类型，noticeUser通知人员,noticeGroup通知群,noticeDay按天,noticeWeek按周
    */
    @TableField("notice_type")
    private String noticeType;
    /**
    * 通知json
    */
    @TableField(value = "notice_value_json", jdbcType = JdbcType.OTHER)
    private JSONObject noticeValueJson;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;


    public static final String NOTICE_TYPE = "notice_type";

    public static final String NOTICE_VALUE_JSON = "notice_value_json";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}