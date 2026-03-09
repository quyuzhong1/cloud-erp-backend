package com.sdk.wms.zhongbao.dto.response;

import lombok.Data;

/**
 * @author zdy
 * @ClassName PageResponse
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
public class PageResponse {
    private String pageNum;
    private String pageSize;
    private String totalCount;
    private String totalPage;
}
