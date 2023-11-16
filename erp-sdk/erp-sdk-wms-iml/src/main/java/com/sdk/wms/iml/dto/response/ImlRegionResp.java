package com.sdk.wms.iml.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlRegionResp implements Serializable {

    //区域ID
    @JSONField(name = "region_id")
    private Integer regionId;

    //父区域关联ID
    @JSONField(name = "parent_region_id")
    private Integer parentRegionId;

    //区域名称
    @JSONField(name = "region_name")
    private String regionName;

    //区域级别,0中国，1省，2市，3区/县
    @JSONField(name = "region_level")
    private Integer regionLevel;

}
