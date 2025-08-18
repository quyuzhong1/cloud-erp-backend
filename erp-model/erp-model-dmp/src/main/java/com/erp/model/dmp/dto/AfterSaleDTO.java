package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.dmp.entity.AfterSaleDetailEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 售后申请表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-06
*/
@Data
@NoArgsConstructor
public class AfterSaleDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;
         /**
         * 类型
         */
         private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 页面高级查询
         */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
            * sqlMap 默认key default
        */
        private Map<String,String> sqlMap;

         /**
          * 主键id
          */
         private List<String> ids;

     }
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 工单号
        */
        private String code;

        /**
        * 单据状态
        */
        private String status;

        private String statusName;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 最新审核人ID
        */
        private String approveUserId;

        /**
        * 最新审核人
        */
        private String approveUserName;

        /**
        * thrid_user_info主键id
        */
        private String thridUserId;

        /**
        * 第三方平台类型
        */
        private String thridType;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 销售平台
        */
        private String dictPlatform;

        /**
        * 购买日期
        */
        private LocalDate buyDate;

        /**
        * 邮寄地址
        */
        private String address;

        /**
        *  故障描述
        */
        private String faultDesc;

        /**
        * 旺店通绑定维修收费单号
        */
        private String repairInvoiceCode;

        /**
        * 货值
        */
        private BigDecimal totalPrice;

        /**
        * 维修金额
        */
        private BigDecimal totalRepairAmount;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 备注
        */
        private String remark;

        /**
         * 客服备注
         */
        private String csrRemark;

        /**
         * 维修备注
         */
        private String rmaRemark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        // after_sale_detail 表字段
        private String detailId;
        private Long mainId;
        private BigDecimal price;
        private BigDecimal repairAmount;
        private String skuId;
        private String skuNo;
        private String productName;
        private Integer skuQty;
        private String detailDesc;
        /**
         * 仓库寄的快递单号
         */
        private String outboundTrackNo;
        /**
         * 客户寄的快递单号
         */
        private String returnTrackNo;
        /**
         * 客户名
         */
        private String thridUserName;
        /**
         * 用户昵称
         */
        private String nickName;
        /**
         * 手机号码
         */
        private String phoneNumber;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 工单号
        */
        private String code;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 审核状态
        */
        private ApproveStatusEnum approveStatus;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 最新审核人ID
        */
        private String approveUserId;

        /**
        * 最新审核人
        */
        private String approveUserName;

        /**
        * thrid_user_info主键id
        */
        private String thridUserId;

        /**
        * 第三方平台类型
        */
        private String thridType;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 销售平台
        */
        private String dictPlatform;
        private String dictPlatformName;

        /**
        * 购买日期
        */
        private LocalDate buyDate;

        /**
        * 邮寄地址
        */
        private String address;

        /**
        *  故障描述
        */
        private String faultDesc;

        /**
        * 旺店通绑定维修收费单号
        */
        private String repairInvoiceCode;

        /**
        * 货值
        */
        private BigDecimal totalPrice;

        /**
        * 维修金额
        */
        private BigDecimal totalRepairAmount;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 备注
        */
        private String remark;

        /**
         * 客服备注
         */
        private String csrRemark;

        /**
         * 维修备注
         */
        private String rmaRemark;


        private String statusName;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 仓库寄的快递单号
         */
        private String outboundTrackNo;
        /**
         * 客户寄的快递单号
         */
        private String returnTrackNo;
        /**
         * 客户名
         */
        private String thridUserName;
        /**
         * 用户昵称
         */
        private String nickName;
        /**
         * 手机号码
         */
        private String phoneNumber;

        private List<AfterSaleDetailEntity> detailList;
        private List<String> attachNameList;
        private List<String> attachUrlList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 单据状态
         */
        private String status;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * thrid_user_info主键id*
         * 客户ID
         */
        private String thridUserId;

        @Size(max = 32,message = "客户名称最大长度不能超过32位")
        private String thridUserName;

        @Size(max = 11,message = "手机号码最大长度不能超过11位")
        private String phoneNumber;
        //用户类型 wx , selfAdd
        private String type;

        /**
         * 第三方平台类型
         */
        private String thridType;

        /**
         * 平台订单号
         * 数字：0-9
         * 大小写字母：a-z, A-Z
         * 常用特殊字符：!@#$%^&*()_+-=[]{};':"|,.<>/?
         */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 32,message = "平台订单号最大长度不能超过32位")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$",
                message = "字段只能包含数字、大小写字母和常用特殊字符")
        private String platformCode;

        /**
         * 销售平台
         */
        @NotBlank(message = "销售平台不能为空")
        @Size(max = 32,message = "销售平台最大长度不能超过32位")
        private String dictPlatform;

        /**
         * 购买日期
         */
        private LocalDate buyDate;

        /**
         * 邮寄地址
         */
        @NotBlank(message = "邮寄地址不能为空")
        @Size(max = 500,message = "邮寄地址最大长度不能超过500位")
        private String address;

        /**
         *  故障描述
         */
        @NotBlank(message = " 故障描述不能为空")
        @Size(max = 500,message = " 故障描述最大长度不能超过500位")
        private String faultDesc;

        /**
         * 旺店通绑定维修收费单号
         */
        @Size(max = 32,message = "旺店通绑定维修收费单号最大长度不能超过32位")
        private String repairInvoiceCode;

        /**
         * 货值
         */
        @Digits(integer = 14, fraction = 2, message = "货值整数位不能超过14位，小数位不能超过2位")
        @Min(value = 0, message = "货值金额不能小于0")
        private BigDecimal totalPrice;

        /**
         * 维修金额
         */
        @Digits(integer = 14, fraction = 2, message = "维修金额整数位不能超过14位，小数位不能超过2位")
        @Min(value = 0, message = "维修金额不能小于0")
        private BigDecimal totalRepairAmount;

        /**
         * 备注
         */
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;

        /**
         * 客服备注
         */
        @Size(max = 255,message = "客服备注最大长度不能超过255位")
        private String csrRemark;

        /**
         * 维修备注
         */
        @Size(max = 255,message = "维修备注最大长度不能超过255位")
        private String rmaRemark;

        /**
         * 小程序端附件
         */
        private List<String> attachmentList;
        /**
         * web附件
         */
        private List<String> attachNameList;
        private List<String> attachUrlList;

        /**
         * 商家寄出快递单号
         */
        @Size(max = 64,message = "商家寄出快递单号最大长度不能超过64位")
        private String outboundTrackNo;
        /**
         * 买家寄出快递单号
         */
        @Size(max = 64,message = "买家寄出快递单号最大长度不能超过64位")
        private String returnTrackNo;

        /**
         * 明细
         */
//        @NotEmpty(message = "售后明细不能为空")
        private List<AfterSaleDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttachmentDTO implements Serializable{

        private String tempFilePath;
        private BigDecimal size;
        private String fileType;
        private String type;
        /**
         * 文件url
         */
        private String url;
        private String thumb;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NodeDTO implements Serializable{
        /**
         *
         */
        private Integer index;
        /**
         *
         */
        private String node;

        /**
         *
         */
        private String nodeName;

        /**
         *
         */
        private String remark;
        /**
         * 客服备注
         */
        private String csrRemark;

        /**
         * 维修备注
         */
        private String rmaRemark;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ProgressDTO extends ThridUserDTO{
        /**
         *
         */
        @NotBlank(message = "工单号不能为空")
        private String code;

    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ThridUserDTO {

        /**
         *
         */
        @NotBlank(message = "用户唯一标记不能为空")
        private String thridUserId;
    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class IdsDTO extends NodeDTO{
        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        private String comment;

        private String trackNo;

    }

    /**
     * 微信小程序输出结果
     */
    @Data
    @NoArgsConstructor
    public static class DropDownDTO{
//
//        /**
//         *dmp_so_info / dmp_so_original_info 主键
//         */
//        private String id;
//        /**
//         *dmp_so_detail/dmp_so_original_detail 主键
//         */
//        private String detailId;

        private String shopId;
        /**
         *
         */
        private String skuId;

        private String skuNo;

        /**
         *
         */
        private String productName;
        /**
         *
         */
        private Integer skuQty;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         *
         */
        private String thirdType;

    }



    /**
     * 微信小程序输出结果
     */
    @Data
    @NoArgsConstructor
    public static class OpenApiCommonDTO{

        /**
         */
        private String id;

        /**
         */
        private String key;


        /**
         */
        private String platformCode;


        /**
         */
        private String code;


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class UpdateTrackNoDTO {
        /**
         *
         */
        @NotBlank(message = "工单号不能为空")
        private String code;

        /**
         *
         */
        @NotBlank(message = "快递单号不能为空")
        private String trackNo;


    }

}