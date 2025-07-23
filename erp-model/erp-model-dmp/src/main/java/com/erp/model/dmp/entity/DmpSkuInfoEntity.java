package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台产品表
 * </p>
 *
 * @author shukai
 * @since 2024-07-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_sku_info")
public class DmpSkuInfoEntity extends BaseEntity<DmpSkuInfoEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 产品id
    */
    @TableField("spu_id")
    private String spuId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 中文名
    */
    @TableField("name")
    private String name;
    /**
    * 统一成本价
    */
    @TableField("default_cost")
    private BigDecimal defaultCost;
    /**
    * 状态：1 在售 2 赠品 3 已下架 4 卖完下架 5 未上架 6 清仓 7 定制
    */
    @TableField("status")
    private String status;
    /**
    * 品牌名称
    */
    @TableField("brand_name")
    private String brandName;
    /**
    * 商品目录(一级)
    */
    @TableField("parent_category_name")
    private String parentCategoryName;
    /**
    * 商品目录(二级)
    */
    @TableField("category_name")
    private String categoryName;
    /**
    * 售价
    */
    @TableField("sell_price")
    private BigDecimal sellPrice;
    /**
    * 申报价格
    */
    @TableField("declare_price")
    private BigDecimal declarePrice;
    /**
    * 负责人id
    */
    @TableField("charge_id")
    private String chargeId;
    /**
    * 负责人名称
    */
    @TableField("charge_name")
    private String chargeName;
    /**
    * 企业id
    */
    @TableField("company_id")
    private String companyId;
    /**
    * 企业名称
    */
    @TableField("company_name")
    private String companyName;
    /**
    * 物料属性 1.外购 2.自制 3.委外 4.服务
    */
    @TableField("prodcut_property")
    private String prodcutProperty;
    /**
    * sku重量
    */
    @TableField("gross_weight")
    private BigDecimal grossWeight;
    /**
    * sku物流尺寸-高
    */
    @TableField("package_height")
    private BigDecimal packageHeight;
    /**
    * sku物流尺寸-宽
    */
    @TableField("package_width")
    private BigDecimal packageWidth;
    /**
    * sku物流尺寸-长
    */
    @TableField("package_length")
    private BigDecimal packageLength;
    
    /**
     * 上线时间
     */
    @TableField("listing_time")
    private LocalDateTime listingTime;
    
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 图片url
    */
    @TableField("image_urls")
    private String imageUrls;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 包装尺寸单位
    */
    @TableField("package_unit")
    private String packageUnit;
    /**
    * 重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
     * 第三方ID
     */
    @TableField("third_id")
    private String thirdId;

    /**
     * 父平台产品ID（父ASIN）
     */
    @TableField("platform_parent_spu_no")
    private String platformParentSpuNo;



    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SPU_ID = "spu_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String NAME = "name";

    public static final String DEFAULT_COST = "default_cost";

    public static final String STATUS = "status";

    public static final String BRAND_NAME = "brand_name";

    public static final String PARENT_CATEGORY_NAME = "parent_category_name";

    public static final String CATEGORY_NAME = "category_name";

    public static final String SELL_PRICE = "sell_price";

    public static final String DECLARE_PRICE = "declare_price";

    public static final String CHARGE_ID = "charge_id";

    public static final String CHARGE_NAME = "charge_name";

    public static final String COMPANY_ID = "company_id";

    public static final String COMPANY_NAME = "company_name";

    public static final String PRODCUT_PROPERTY = "prodcut_property";

    public static final String GROSS_WEIGHT = "gross_weight";

    public static final String PACKAGE_HEIGHT = "package_height";

    public static final String PACKAGE_WIDTH = "package_width";

    public static final String PACKAGE_LENGTH = "package_length";

    public static final String MAIN_ID = "main_id";

    public static final String IMAGE_URLS = "image_urls";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String PACKAGE_UNIT = "package_unit";

    public static final String WEIGHT_UNIT = "weight_unit";

    @Override
    public Serializable pkVal() {
        return null;
    }

}