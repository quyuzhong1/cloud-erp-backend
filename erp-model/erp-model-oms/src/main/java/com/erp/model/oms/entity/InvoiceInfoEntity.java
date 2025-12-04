package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 上传记录
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("invoice_info")
public class InvoiceInfoEntity extends BaseEntity<InvoiceInfoEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 配置id
    */
    @TableField("cfg_id")
    private String cfgId;
    /**
    * 发票类型:vat=VAT发票  枚举：InvoiceInfoInvoiceTypeEnum
    */
    @TableField("invoice_type")
    private String invoiceType;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售订单编码
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 平台订单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 模板类型:erp=ERP模板,official=官方模板  枚举：InvoiceInfoTemplateTypeEnum
    */
    @TableField("template_type")
    private String templateType;
    /**
    * 发票状态:invoicing=开票中,invoiceFailed=开票失败invoiceSuccess=开票成功  枚举：InvoiceInfoStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 上传时间
    */
    @TableField("upload_time")
    private LocalDateTime uploadTime;
    /**
     * 发票生成时间
     */
    @TableField("bill_create_time")
    private LocalDateTime billCreateTime;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 文件地址
    */
    @TableField("file_url")
    private String fileUrl;
    /**
    * 上传状态:waitUpload=待上传,uploadFailed=上传失败,uploadSuccess=上传成功  枚举：InvoiceInfoUploadStatusEnum
    */
    @TableField("upload_status")
    private String uploadStatus;

    /**
     * 查询id，用来查询发票结果的Id
     */
    @TableField("query_id")
    private String queryId;
    /**
     * 查询key，用来查询发票结果的Key
     */
    @TableField("query_key")
    private String queryKey;

    /**
     * 查询结果，当第三方失败时有值
     */
    @TableField("query_result")
    private String queryResult;
    /**
     * 发票性质
     * InvoiceNatureEnum
     */
    @TableField("invoice_nature")
    private String invoiceNature;
    /**
     * 平台发票编号
     */
    @TableField("platform_invoice_no")
    private String platformInvoiceNo;
    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;
    /**
     * 退货税务编码
     */
    @TableField("return_tax_code")
    private String returnTaxCode;
    /**
     * 退票原因
     */
    @TableField("return_reason")
    private String returnReason;

    /**
     * 开票备注
     */
    @TableField("invoice_remark")
    private String invoiceRemark;
    /**
     * 序列号
     */
    @TableField("no")
    private Integer no;
    /**
     * 起始编号
     */
    @TableField("start_code")
    private String startCode;
    /**
     * 公司名称
     */
    @TableField("company_name")
    private String companyName;
    /**
     *
     * 卖家税号
     */
    @TableField("seller_tax_no")
    private String sellerTaxNo;

    /**
     * 发票地址
     */
    @TableField(exist = false)
    private String invoiceAddress;

    public static final String CODE = "code";

    public static final String CFG_ID = "cfg_id";

    public static final String INVOICE_TYPE = "invoice_type";

    public static final String SHOP_ID = "shop_id";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String TEMPLATE_TYPE = "template_type";

    public static final String STATUS = "status";

    public static final String UPLOAD_TIME = "upload_time";

    public static final String REMARK = "remark";

    public static final String FILE_URL = "file_url";

    public static final String UPLOAD_STATUS = "upload_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
