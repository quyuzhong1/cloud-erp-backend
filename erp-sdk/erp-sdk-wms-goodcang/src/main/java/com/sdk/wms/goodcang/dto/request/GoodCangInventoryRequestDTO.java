package com.sdk.wms.goodcang.dto.request;

import lombok.Data;
import java.util.List;

/**
 * 谷仓库存流水请求 DTO
 */
@Data
public class GoodCangInventoryRequestDTO {

    /**
     * 操作类型
     * 枚举: enum\Enum\OpenApi\Inventory\ApplicationCodeEnum
     * 示例: 3
     */
    private Integer application_code;

    /**
     * 仓库编码
     * 示例: USEA
     */
    private String warehouse_code;

    /**
     * 商品品质
     * 枚举: enum\Enum\OpenApi\Inventory\ProductQualityEnum
     * 默认值: 1
     * 示例: 1
     */
    private Integer product_quanlity;

    /**
     * 商品编码列表
     * 多个SKU，数组格式，精确匹配，最多支持200个
     * 示例: ALI_AND_NABC_INO
     */
    private List<String> product_sku_list;

    /**
     * 操作单号列表
     * 对应该次库存涉及的业务单号，例如出库单号、入库单号、退货单号等等，最多支持200个
     * 示例: RVG296-210518-0016
     */
    private List<String> reference_no_list;

    /**
     * 必填 - 开始时间
     * 格式: YYYY-MM-DD
     * 示例: 2021-05-07
     */
    private String create_date_from;

    /**
     * 必填 - 结束时间
     * 格式: YYYY-MM-DD，最大时间跨度为31天
     * 示例: 2021-05-10
     */
    private String create_date_end;

    /**
     * 必填 - 每页数据长度
     * 最大值: 200
     * 默认值: 20
     * 示例: 20
     */
    private Integer pageSize;

    /**
     * 必填 - 当前页
     * 默认值: 1
     * 示例: 1
     */
    private Integer page;
}
