package com.erp.model.plm.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 模具主表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldInfoDTO implements Serializable {


    /**
     * 分页
     */
    @Getter
    @Setter
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 项目编号
         */
        private String projectNo;

        /**
         * 项目名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 明细id
         */
        private String  detailId;

        /**
         * 模具编号
         */
        private String mouldNo;

        /**
         * 外部模具编号(供应商)
         */
        private String thirdMouldNo;

        /**
         * 模具类型
         */
        @Dict(tableName = "cfg_mould_setting", queryFieldName = "id")
        private String typeId;

        /**
         * 模具穴数
         */
        private String moldHoles;

        /**
         * 模具长
         */
        private BigDecimal length;

        /**
         * 模具宽
         */
        private BigDecimal width;

        /**
         * 模具高
         */
        private BigDecimal height;

        /**
         * 模具材质
         */
        private String material;

        /**
         * 模具寿命(万)(啤)
         */
        private Integer lifeCycle;

        /**
         * 开模周期(自然日)
         */
        private Integer developCycle;

        /**
         * 启用时间
         */
        private LocalDateTime enableDate;

        /**
         * 供应商id
         */
        @Dict(tableName = "supplier", serviceCode = ServiceCodeNameEnum.SCM, queryFieldName = "id")
        private String supplierId;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人名称
         */
        private String createUserName;
        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 修改时间
         */
        private LocalDateTime updateTime;

        /**
         * 产品明细
         */
        private List<MouldProductDTO.ViewDTO> productList;

        /**
         * 存放位置
         */
        private MouldStoreLocationDTO.ViewDTO storeLocation;

    }

    /**
     * 参数
     */
    @Getter
    @Setter
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

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
        * 项目编号
        */
        private String projectNo;

        /**
        * 项目名称
        */
        private String name;

        /**
        * 状态
        */
        private String status;

        /**
        * 产品经理
        */
        private String productManagerId;

        /**
        * 备注
        */
        private String remark;

        /**
        * 分类id
        */
        private String categoryId;

        /**
        * 模具分类编码
        */
        private String moldCategoryCode;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细数据
         */
        @Valid
        private List<DetailDTO> detailList;

        /**
         * 文档数据
         */
        private List<DocDTO> docList;

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

        /**
         * 明细数据
         */
        @Valid
        private List<DetailDTO> detailList;

        /**
         * 文档数据
         */
        private List<DocDTO> docList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 项目编号
        */
        @Size(max = 50,message = "项目编号最大长度不能超过50位")
        private String projectNo;

        /**
        * 项目名称
        */
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 255,message = "项目名称最大长度不能超过255位")
        private String name;

        /**
        * 产品经理
        */
        @NotBlank(message = "产品经理不能为空")
        @Size(max = 19,message = "产品经理最大长度不能超过19位")
        private String productManagerId;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过100位")
        private String remark;

        /**
        * 分类id
        */
        @NotBlank(message = "分类id不能为空")
        @Size(max = 19,message = "分类id最大长度不能超过19位")
        private String categoryId;


    }

    @Getter
    @Setter
    public static class DetailDTO {

        /**
         * 外部模具编号(供应商)
         */
        private String thirdMouldNo;

        /**
         * 模具类型
         */
        @NotBlank(message = "模具类型不能为空")
        @Size(max = 19,message = "模具类型最大长度不能超过19位")
        private String typeId;

        /**
         * 模具穴数
         */
        @NotBlank(message = "模具穴数不能为空")
        @Size(max = 255,message = "模具穴数最大长度不能超过255位")
        private String moldHoles;

        /**
         * 产品信息
         */
        @NotNull(message = "产品信息不能为空")
        @Valid
        private List<ProductDTO> productList;

        /**
         * 模具长
         */
        private BigDecimal length;

        /**
         * 模具宽
         */
        private BigDecimal width;

        /**
         * 模具高
         */
        private BigDecimal height;

        /**
         * 模具材质
         */
        @NotBlank(message = "模具材质不能为空")
        @Size(max = 255,message = "模具材质最大长度不能超过255位")
        private String material;

        /**
         * 模具寿命(万)(啤)
         */
        @NotNull(message = "模具寿命(万)(啤)不能为空")
        private Integer lifeCycle;

        /**
         * 开模周期(自然日)
         */
        @NotNull(message = "开模周期(自然日)不能为空")
        private Integer developCycle;

        /**
         * 启用时间
         */
        @NotNull(message = "启用时间不能为空")
        private LocalDateTime enableDate;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
         * 备注
         */
        private String remark;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
         * 结算方式
         */
        @NotBlank(message = "结算方式不能为空")
        @Size(max = 19,message = "结算方式最大长度不能超过19位")
        private String payMethodId;

        /**
         * 付款条件
         */
        @NotBlank(message = "付款条件不能为空")
        @Size(max = 255,message = "付款条件最大长度不能超过255位")
        private String paymentCondition;


        /**
         * 是否费用返还
         */
        private Boolean isNeedRefund;

        /**
         * 返还标准
         */
        private String refundStandard;

        /**
         * 退款单量
         */
        private Integer refundOrderQty;

        /**
         * 返还金额
         */
        private BigDecimal refundAmount;

        /**
         * 费用返还状态
         */
        private String refundStatus;

        /**
         * 关联产品
         */
        private List<MouldRefProductDTO.CommonDTO> refProductList;
    }




    @Getter
    @Setter
    public static class ProductDTO {
        /**
         * 产品名称
         */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
         * 图片地址
         */
        @NotBlank(message = "图片地址不能为空")
        @Size(max = 255,message = "图片地址最大长度不能超过255位")
        private String imagesUrl;
    }


    @Getter
    @Setter
    public static class DocDTO {
        /**
         * 文档类型id
         */
        private String typeId;

        /**
         * 版本号
         */
        private String docVersion;

        /**
         * 文件地址
         */
        private String docUrl;

        /**
         * 文档名字
         */
        private String docName;

        /**
         * 外部链接
         */
        private String extLink;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 存放位置
     */
    @Getter
    @Setter
    public static class StoreLocationDTO {
        /**
         * 明细id
         */
        @Size(min = 1, message = "明细不能为空")
        private List<String> detailId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id")
        private String warehouseId;
        /**
         * 库位
         */
        @NotBlank(message = "库位")
        private String warehouseLocation;
        /**
         * 明细地址
         */
        private String address;
    }

    @Getter
    @Setter
    public static class EnableTimeDTO {
        /**
         * 明细id
         */
        @Size(min = 1, message = "明细不能为空")
        private List<String> detailIdList;

        /**
         * 启用时间
         */
        @NotNull(message = "启用时间")
        private LocalDate enableTime;
    }
}