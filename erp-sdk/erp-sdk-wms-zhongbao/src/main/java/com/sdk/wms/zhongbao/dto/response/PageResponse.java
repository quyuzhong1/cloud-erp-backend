package com.sdk.wms.zhongbao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zdy
 * @ClassName PageResponse
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse {
    private String pageNum;
    private String pageSize;
    private String totalCount;
    private String totalPage;
}
