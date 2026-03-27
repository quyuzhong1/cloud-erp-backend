package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ChannelRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class ChannelRequest implements Serializable {
    //仓库代码
    private String warehouseCode;
    //渠道代码
    private String code;
    //渠道类型:1=>物流渠道,2=>面单渠道
    private String methodType;
    //通用请求参数
    private CommonRequest commonParam;
}
