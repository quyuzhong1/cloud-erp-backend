package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 上传记录请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
*/
@Data
@NoArgsConstructor
public class InvoiceInfoDTO implements Serializable {


    /**
     * 开具Cce详情回显
     */
    @Data
    @NoArgsConstructor
    public static class ViewCceDTO {
        /**
         * 发票号
         */
        private String code;
        /**
         * 修改次数
         */
        private Integer count;
    }
    /**
     * 带备注DTO
     */
    @Data
    @NoArgsConstructor
    public static class ReturnRemarkDTO extends  RemarkDTO{
        /**
         * 退货税务编码
         */
        @NotBlank(message = "退货税务编码不能为空")
        private String returnTaxCode;
    }


    /**
     * 带备注DTO
     */
    @Data
    @NoArgsConstructor
    public static class RemarkDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        private String remark;
    }

    /**
     * 销售订单备注DTO
     */
    @Data
    @NoArgsConstructor
    public static class SoRemarkDTO {
        /**
         * so主键id
         */
        @NotEmpty(message = "soId不能为空")
        private List<String> soIdList;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        private String remark;
    }


    /**
     * 销售订单无需开票DTO
     */
    @Data
    @NoArgsConstructor
    public static class NoNeedInvoiceDTO {
        /**
         * so主键id
         */
        @NotEmpty(message = "soId不能为空")
        private List<String> soIdList;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        private String remark;

        /**
         * 发票类型不能为空
         */
        @NotBlank(message = "发票类型不能为空")
        private String invoiceType;
    }

    /**
     * 开具Cce保存
     */
    @Data
    @NoArgsConstructor
    public static class UpdateCceDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 内容
         */
        @NotBlank(message = "内容不能为空")
        private String content;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 单据编码
        */
        private String code;

        /**
        * 配置id
        */
        private String cfgId;

        /**
        * 发票类型:vat=VAT发票
        */
        private String invoiceType;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售订单编码
        */
        private String soCode;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 模板类型:erp=ERP模板,official=官方模板
        */
        private String templateType;

        /**
        * 发票状态:invoicing=开票中,invoiceFailed=开票失败invoiceSuccess=开票成功
        */
        private String status;

        /**
        * 上传时间
        */
        private LocalDateTime uploadTime;

        /**
        * 备注
        */
        private String remark;

        /**
        * 文件地址
        */
        private String fileUrl;

        /**
        * 上传状态:waitUpload=待上传,uploadFailed=上传失败,uploadSuccess=上传成功
        */
        private String uploadStatus;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        //明细
        private List<InvoiceDetailDTO.AddDTO> detailList;

    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;
        //明细
        private List<InvoiceDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 配置id
        */
        @NotBlank(message = "配置id不能为空")
        @Size(max = 19,message = "配置id最大长度不能超过19位")
        private String cfgId;

        /**
        * 发票类型:vat=VAT发票
        */
        @NotBlank(message = "发票类型:vat=VAT发票不能为空")
        @Size(max = 30,message = "发票类型:vat=VAT发票最大长度不能超过30位")
        private String invoiceType;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售订单编码
        */
        @NotBlank(message = "销售订单编码不能为空")
        @Size(max = 32,message = "销售订单编码最大长度不能超过32位")
        private String soCode;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 模板类型:erp=ERP模板,official=官方模板
        */
        @NotBlank(message = "模板类型:erp=ERP模板,official=官方模板不能为空")
        @Size(max = 30,message = "模板类型:erp=ERP模板,official=官方模板最大长度不能超过30位")
        private String templateType;

        /**
        * 发票状态:invoicing=开票中,invoiceFailed=开票失败invoiceSuccess=开票成功
        */
        @NotBlank(message = "发票状态:invoicing=开票中,invoiceFailed=开票失败invoiceSuccess=开票成功不能为空")
        @Size(max = 30,message = "发票状态:invoicing=开票中,invoiceFailed=开票失败invoiceSuccess=开票成功最大长度不能超过30位")
        private String status;

        /**
        * 上传时间
        */
        private LocalDateTime uploadTime;

        /**
        * 备注
        */
        private String remark;

        /**
        * 文件地址
        */
        private String fileUrl;

        /**
        * 上传状态:waitUpload=待上传,uploadFailed=上传失败,uploadSuccess=上传成功
        */
        @NotBlank(message = "上传状态:waitUpload=待上传,uploadFailed=上传失败,uploadSuccess=上传成功不能为空")
        @Size(max = 255,message = "上传状态:waitUpload=待上传,uploadFailed=上传失败,uploadSuccess=上传成功最大长度不能超过255位")
        private String uploadStatus;
    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String  id;
        /**
         * 明细id
         */
        private String  detailId;

        /**
         * 单据编码【可排序】
         */
        private String code;

        /**
         * 配置id
         */
        private String cfgId;

        /**
         * 发票类型:vat=VAT发票【可排序】
         */
        private String invoiceType;
        /**
         * 发票类型名称
         */
        private String invoiceTypeName;

        /**
         * 店铺id【可排序】
         */
        private String shopId;

        private String shopName;
        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单编码【可排序】
         */
        private String soCode;

        /**
         * 平台订单号【可排序】
         */
        private String platformCode;

        /**
         * 模板类型:erp=ERP模板,official=官方模板【可排序】
         */
        private String templateType;
        /**
         * 模板类型名称
         */
        private String templateTypeName;

        /**
         * 发票状态:invoicing=开票中,invoiceFailed=开票失败invoiceSuccess=开票成功 【可排序】
         */
        private String status;
        private String statusName;
        /**
         * 序列号
         */
        private Integer no;
        /**
         * 起始编号
         */
        private String startCode;
        /**
         * 序列号/序号
         */
        private String startCodeStr;

        /**
         * 上传时间【可排序】
         */
        private LocalDateTime uploadTime;
        /**
         * 生成时间【可排序】
         */
        private LocalDateTime billCreateTime;

        /**
         * 备注【可排序】
         */
        private String remark;

        /**
         * 文件地址
         */
        private String fileUrl;

        /**
         * 上传状态:waitUpload=待上传,uploadFailed=上传失败,uploadSuccess=上传成功 【可排序】
         */
        private String uploadStatus;
        private String uploadStatusName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 产品sku编号【可排序】
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 发票性质
         */
        private String invoiceNature;
        /**
         * 发票性质名称
         */
        private String invoiceNatureName;
        /**
         * 平台发票号
         */
        private String platformInvoiceNo;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ExportAttachDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 文件url
         */
        private String attachUrl;
        /**
         * 文件名称
         */
        private String attachName;
        /**
         * 类型
         */
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class ExportResultDTO {
        /**
         * 响应体
         */
       private StreamingResponseBody responseBody;
       /**
        * 下载文件名称
        */
       private String fileName;
    }


    @Data
    @NoArgsConstructor
    public static class AttachDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 文件url
         */
        private String attachUrl;
        /**
         * 文件名称
         */
        private String attachName;
    }

    @Data
    @NoArgsConstructor
    public static class ProductAmountRuleResultDTO {
        /**
         * 是否匹配
         */
        private Boolean isMatch;
        /**
         * 描述
         */
        private String msg;

        private CfgInvoiceSettingDetailEntity invoiceSettingDetail;

        /**
         * 发票规则（Amount：全额，Custom：自定义，扣佣金：Deduct）
         * InvoiceRuleEnum
         */
        private String dictInvoiceRule;
        /**
         * 比例（x100）
         */
        private BigDecimal ratio;
    }
}