package com.erp.model.dmp.dto;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.PlatformEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/9 16:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiSyncLogMsgDTO implements Serializable {
    /**
     * 业务id
     */
    private String bussinessId;

    /**
     * 平台
     */
    private PlatformEntity platformEntity;

    /**
     * 业务传过来数据
     */
    private Map<String, Object> map;

    /**
     * 传到金蝶的json
     */
    private JSONObject json;

    /**
     * 错误信息
     */
    private String msg;

}
