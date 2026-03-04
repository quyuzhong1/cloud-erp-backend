package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName WarehouseRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class ProductRequest implements Serializable {
    //产品分类Code
    private String productCategoryCode;
    //产品SKU列表
    private List<String> productSkus;
    //产品SKU
    private String productSku;
    //产品类型:1=>普通,2=>小件,3=>大件,4=>超大件,5=>超重件
    private String productType;
    //产品状态:-1=>废弃,1=>草稿,2=>待审核,3=>已审核,4=>驳回
    private Integer status;
    //通用请求参数
    private CommonRequest commonParam;
}
