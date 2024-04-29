package com.cloud.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileTaskDTO {
    /**
     * 文件来源，确定执行地方（添加为字典或枚举）
     */
    private String event;
    /**
     * 文件名字
     */
    private String fileName;
    /**
     * 查询数据的请求参数
     */
    private Object metaInfo;

}
