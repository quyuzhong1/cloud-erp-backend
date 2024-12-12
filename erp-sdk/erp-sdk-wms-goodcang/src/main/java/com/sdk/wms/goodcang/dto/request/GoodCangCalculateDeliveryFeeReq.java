package com.sdk.wms.goodcang.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zdy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GoodCangCalculateDeliveryFeeReq {
    /**
     * 仓库代码
     * 必填
     */
    @NotNull(message = "仓库代码不能为空")
    @JSONField(name = "warehouse_code")
    protected String warehouseCode;
    /**
     * 物流产品代码
     */
    @JSONField(name = "sm_code")
    protected String smCode;

    /**
     * 目的国家/地区代码
     * 必填
     */
    @NotNull(message = "目的国家不能为空")
    @JSONField(name = "country_code")
    protected String countryCode;

    /**
     * 邮政编码
     * 必填
     */
    @NotNull(message = "邮政编码不能为空")
    @JSONField(name = "postcode")
    protected String postcode;

    /**
     * 包裹重量
     * 单位KG
     * 有sku参数时，以sku数据优先，下文参数length、width、height同理。
     * 最多保留2位小数
     */
    @JSONField(name = "weight")
    protected Float weight;
    /**
     * 包裹长
     * 单位CM
     * 有sku参数时，以sku数据优先，下文参数length、width、height同理。
     * 最多保留2位小数
     */
    @JSONField(name = "length")
    protected Float length;
    /**
     * 包裹宽
     * 单位CM
     * 有sku参数时，以sku数据优先，下文参数length、width、height同理。
     * 最多保留2位小数
     */
    @JSONField(name = "width")
    protected Float width;

    /**
     * 包裹高
     * 单位CM
     * 有sku参数时，以sku数据优先，下文参数length、width、height同理。
     * 最多保留2位小数
     */
    @JSONField(name = "height")
    protected Float height;

    /**
     * 商品编码
     * 数组元素的字符串长度限制24
     *
     * [商品编码与包裹重量必须填写一个,商品编码与包裹重量都填写时,以商品编码为主]
     * 格式如:["sku1","sku2:1","sku3:1"],多个商品隔开,冒号后接商品数量,不填商品数量,默认1件,最多不超过100种sku
     */
    @JSONField(name = "sku")
    protected List<String> sku;

    /**
     * 是否住宅地址
     * BoolIntEnum
     */
    @JSONField(name = "is_residential")
    protected Integer isResidential;

    /**
     * 是否签名服务
     * IsSignServerEnum
     */
    @JSONField(name = "is_sign_server")
    protected Integer isSignServer;

    /**
     * 是否保险服务
     * IsInsuranceService
     */
    @JSONField(name = "is_insurance_service")
    protected Integer isInsuranceService;

    /**
     * 保险金额
     * 单位USD 当is_insurance_service等于2时生效
     * 最多保留3位小数
     */
    @JSONField(name = "insurance_amount")
    protected Float insuranceAmount;

    /**
     * 省/州
     * 示例 : NY
     */
    @JSONField(name = "state")
    protected String state;

    /**
     * 城市
     * 当国家/地区为[澳大利亚]时，如果不传城市，可能导致费用结果与实际偏差较大。
     */
    @JSONField(name = "city")
    protected String city;

    /**
     * 平台模式
     * PlatformModelEnum   SFP
     */
    @JSONField(name = "property_label")
    protected String propertyLabel;
}
