package com.erp.server.file.dto;

import com.common.business.enums.FileTaskEventEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileTaskDTO {
    /**
     * 文件来源，确定执行地方（添加为字典或枚举）
     * @see FileTaskEventEnum
     */
    private String event;
    /**
     * 文件名字
     */
    private String fileName;
    /**
     * 查询数据的请求参数 对象
     */
    private Object metaInfo;

}
