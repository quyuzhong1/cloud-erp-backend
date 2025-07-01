package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportZipResultDTO {
    /**
     * 响应体
     */
    private StreamingResponseBody responseBody;
    /**
     * 下载文件名称
     */
    private String fileName;
}