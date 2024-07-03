package com.erp.server.dmp.inout.dto.base;

import java.io.Serializable;

import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 拉取任务文件存储请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpOutputTaskDto implements Serializable {
	/**
     * 响应代码
     */
    private Integer code;
    /**
     * 请求消息
     */
    private String requestData;
    /**
     * 响应消息
     */
    private String responseData;
	/**
	 * 内容形式
	 */
	private DmpInputTaskFileContentTypeEnum contentType;
}