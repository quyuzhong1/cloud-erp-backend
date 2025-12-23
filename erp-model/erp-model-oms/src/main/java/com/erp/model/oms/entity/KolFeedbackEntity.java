package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * KOL回片列表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_feedback")
public class KolFeedbackEntity extends BaseEntity<KolFeedbackEntity> {

    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源明细ID
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 达人昵称
    */
    @TableField("partner_nickname")
    private String partnerNickname;
    /**
    * 达人ID
    */
    @TableField("partner_id")
    private String partnerId;
    /**
    * 回片链接（完整链接）
    */
    @TableField("url")
    private String url;
    /**
    * 回片链接哈希值（MD5或SHA256，用于唯一键）
    */
    @TableField("url_hash")
    private String urlHash;
    /**
    * 发布形式
    */
    @TableField("publish_type")
    private String publishType;
    /**
    * 发布日期
    */
    @TableField("publish_date")
    private LocalDate publishDate;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 回片状态 com.erp.model.oms.enums.FeedbackStatusEnum
     */
    @TableField("feedback_status")
    private String feedbackStatus;


    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String QTY = "qty";

    public static final String PARTNER_NIKENAME = "partner_nickname";

    public static final String PARTNER_ID = "partner_id";

    public static final String URL = "url";

    public static final String URL_HASH = "url_hash";

    public static final String PUBLISH_TYPE = "publish_type";

    public static final String PUBLISH_DATE = "publish_date";

    public static final String REMARK = "remark";

    public static final String FEEDBACK_STATUS = "feedback_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}