package com.erp.model.plm.dto;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 模具明细请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

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
        private String mouldHoles;

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
        private LocalDate enableDate;

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
         * 产品信息
         */
        private List<MouldProductDTO.ViewDTO> productList;

        /**
         * 模具价目信息
         */
        private MouldPurchasePriceDTO.ViewDTO purchasePrice;

        /**
         * 模具价目信息
         */
        private MouldRefundAgreementDTO.ViewDTO refundAgreement;

        /**
         * 关联产品
         */
        private List<MouldRefProductDTO.ViewDTO> refProductList;

        public static ViewDTO buildView(MouldDetailEntity entity, MouldPurchasePriceEntity purchasePrice, MouldRefundAgreementEntity refundAgreement, List<MouldRefProductEntity> refList, List<MouldProductEntity> productList) {
            ViewDTO dto = BeanMapperUtils.map(ViewDTO.class, entity);
            MouldPurchasePriceDTO.ViewDTO price = BeanMapperUtils.map(MouldPurchasePriceDTO.ViewDTO.class, purchasePrice);
            MouldRefundAgreementDTO.ViewDTO agreement = BeanMapperUtils.map(MouldRefundAgreementDTO.ViewDTO.class, refundAgreement);
            List<MouldProductDTO.ViewDTO> mouldProductList = new ArrayList<>();
            for (MouldProductEntity viewDTO : productList) {
                MouldProductDTO.ViewDTO productDto = new MouldProductDTO.ViewDTO();
                productDto.setMouldDetailId(viewDTO.getMouldDetailId());
                productDto.setProductName(viewDTO.getProductName());
                productDto.setImagesUrl(Arrays.asList(viewDTO.getImagesUrl().split(",")));
                mouldProductList.add(productDto);
            }
            List<MouldRefProductDTO.ViewDTO> mouldRefList = BeanMapperUtils.copyList(MouldRefProductDTO.ViewDTO.class, refList);
            dto.setPurchasePrice(price);
            dto.setRefundAgreement(agreement);
            dto.setProductList(mouldProductList);
            dto.setRefProductList(mouldRefList);
            return dto;
        }
    }

    /**
    * 新增
    */
    @Getter
    @Setter
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Getter
    @Setter
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

        /**
         * 产品信息
         */
        @Valid
        private List<MouldProductDTO.UpdateDTO> productList;

        /**
         * 模具价目信息
         */
        @Valid
        private MouldPurchasePriceDTO.UpdateDTO purchasePrice;

        /**
         * 模具价目信息
         */
        @Valid
        private MouldRefundAgreementDTO.UpdateDTO refundAgreement;

        /**
         * 关联产品
         */
        @Valid
        private List<MouldRefProductDTO.UpdateDTO> refProductList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

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
        @NotBlank(message = "模具类型不能为空")
        @Size(max = 19,message = "模具类型最大长度不能超过19位")
        private String typeId;

        /**
        * 模具穴数
        */
        @NotBlank(message = "模具穴数不能为空")
        @Size(max = 255,message = "模具穴数最大长度不能超过255位")
        private String mouldHoles;

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
        private LocalDate enableDate;

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

    }


}