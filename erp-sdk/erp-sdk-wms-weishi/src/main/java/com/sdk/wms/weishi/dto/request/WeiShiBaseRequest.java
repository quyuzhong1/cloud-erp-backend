package com.sdk.wms.weishi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import java.util.Map;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiBaseRequest {

    private Map<String,Object> authMap;

    @Max(value = 100,message = "每页最大长度不能大于100")
    protected Integer limit;

    protected Integer page;
}
