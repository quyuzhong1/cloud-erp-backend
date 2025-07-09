package com.sdk.wms.weishi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiBaseRequest {

    @Max(value = 200,message = "每页最大长度不能大于200")
    protected Integer pageSize;

    protected Integer page;
}
