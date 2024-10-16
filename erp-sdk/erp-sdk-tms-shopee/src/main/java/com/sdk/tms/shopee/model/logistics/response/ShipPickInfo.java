package com.sdk.tms.shopee.model.logistics.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ShipInfo
 * @description: TODO
 * @date 2024年10月09日
 * @version: 1.0
 */
@Data
public class ShipPickInfo implements Serializable {
    @Alias( "address_list")
    private List<AddressInfo> addressInfoList;
}
