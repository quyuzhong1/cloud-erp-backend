package com.sdk.oms.dht.dto.req;

import cn.hutool.core.annotation.Alias;
import com.sdk.oms.dht.dto.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DhtDownloadReq extends BaseReq {

    @Alias("mediaTypeDesc")
    private String mediaTypeDesc;

    @Alias("igonreMediaIdConvert")
    private Boolean igonreMediaIdConvert;

    @Alias("mediaId")
    private String mediaId;

}
