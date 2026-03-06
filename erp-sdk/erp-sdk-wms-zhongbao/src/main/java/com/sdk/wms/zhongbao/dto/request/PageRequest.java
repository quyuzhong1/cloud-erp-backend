package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author zdy
 * @ClassName PageRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class PageRequest {
    //页码：默认=>1
    private String pageNum;
    //每页条数：默认=>10
    private String pageSize;
}
