package com.erp.model.wms.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * wms虚拟仓明细同步表
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "wms_virtual_detail_msg",autoResultMap = true)
public class WmsVirtualDetailMsgEntity extends BaseEntity<WmsVirtualDetailMsgEntity> {

    /**
    * json数据
    */
    @TableField(value = "data_json", typeHandler = JacksonTypeHandler.class)
    private JSONObject dataJson;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 操作时间
    */
    @TableField("trade_time")
    private LocalDateTime tradeTime;
    /**
    * waitHandle待处理，success成功，fail失败，doing进行中
    */
    @TableField("status")
    private String status;


    public static final String DATA_JSON = "data_json";

    public static final String REMARK = "remark";

    public static final String TRADE_TIME = "trade_time";

    public static final String STATUS = "status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}