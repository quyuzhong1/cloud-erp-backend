package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName WarehouseRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class WarehouseRequest implements Serializable {
    //仓库代码
    private String warehouseCode;
    //通用请求参数
    private CommonRequest commonParam;
}
