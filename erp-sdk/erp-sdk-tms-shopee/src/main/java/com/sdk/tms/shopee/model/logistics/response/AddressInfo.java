package com.sdk.tms.shopee.model.logistics.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName AddressInfo
 * @description: TODO
 * @date 2024年10月09日
 * @version: 1.0
 */
@Data
public class AddressInfo  implements Serializable {
    @Alias( "address_id")
    private Integer addressId;
    @Alias( "region")
    private String region;
    @Alias( "state")
    private String state;
    @Alias( "city")
    private String city;
    @Alias( "district")
    private String district;
    @Alias( "town")
    private String town;
    @Alias( "address")
    private String address;
    @Alias( "zipcode")
    private String zipcode;
    @Alias( "address_flag")
    private String addressFlag;
}
