package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author zdy
 * @ClassName CommonRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class CommonRequest {
    //分页参数
    private PageRequest pageParam;
}
