package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 新逻辑开放API文件上传请求DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
public class OpenApiReqNewDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 服务名称
     */
    private String method;

    /**
     * 业务参数
     */
    private String data;

}
