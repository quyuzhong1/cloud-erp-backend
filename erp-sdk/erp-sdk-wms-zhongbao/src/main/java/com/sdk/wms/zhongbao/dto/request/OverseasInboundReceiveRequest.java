package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCancelRequest
 * @description: 入库单签收
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class OverseasInboundReceiveRequest implements Serializable {
    //产品SKU
    private String productSku;
    //产品SKU列表
    private List<String> productSkus;
    //仓库代码
    private String warehouseCode;
    //仓库代码列表
    private List<String> warehouseCodes;
    //开始创建时间
    private String startCreateTime;
    //结束创建时间
    private String endCreateTime;
    //通用请求参数
    private CommonRequest commonParam;
}
