package com.erp.model.sys.dto;

import lombok.Data;

@Data
public class OpenApiInputDTO extends OpenApiReqDTO{
    // 业务参数
    private String requestIp;
    
    private String secretKey;
}
