package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 第三方仓退货入库
 * </p>
 *
 * @author Jim
 * @since 2024-10-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_return_inbound_detail")
public class DmpThirdReturnInboundDetailEntity extends BaseEntity<DmpThirdReturnInboundDetailEntity> {

    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 商品SKU(第三方)
    */
    @TableField("product_sku")
    private String productSku;
    /**
    * 应退数量
    */
    @TableField("must_qty")
    private Integer mustQty;
    /**
    * 签收数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 实退数量
    */
    @TableField("real_qty")
    private Integer realQty;
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
     * 第三方明细唯一ID
     */
    @TableField("third_detail_id")
    private String thirdDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String PRODUCT_SKU = "product_sku";

    public static final String MUST_QTY = "must_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String REAL_QTY = "real_qty";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}