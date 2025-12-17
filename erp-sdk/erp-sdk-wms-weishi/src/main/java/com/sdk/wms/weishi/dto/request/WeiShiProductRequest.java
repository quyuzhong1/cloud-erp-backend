package com.sdk.wms.weishi.dto.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiProductRequest extends WeiShiBaseRequest{

    private String startTime;

    private String endTime;
}
