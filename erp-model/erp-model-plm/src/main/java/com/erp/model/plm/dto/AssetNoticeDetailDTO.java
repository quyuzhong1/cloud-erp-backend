package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@NoArgsConstructor
public class AssetNoticeDetailDTO implements Serializable {




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
        private String mainId;

        /**
        * 资产id
        */
        private String assetId;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 资产名称
        */
        private String assetName;

        /**
        * 标识(首套模、复制模)
        */
        private String tag;

        /**
        * 是否加急
        */
        private Boolean isUrgent;

        /**
        * 计划交期
        */
        private LocalDate planDeliverDate;

        /**
        * 申请数量
        */
        private BigDecimal applyQty;

        /**
        * 采购组织id
        */
        private String purchaseOrgId;

        /**
        * 备注
        */
        private String remark;


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
        * 项目编号
        */
        @NotBlank(message = "资产通知单头id不能为空")
        private String mainId;

        /**
        * 资产id
        */
        @NotBlank(message = "资产id不能为空")
        private String assetId;

        /**
        * 资产编码
        */
        @NotBlank(message = "资产编码不能为空")
        private String assetCode;

        /**
        * 资产名称
        */
        @NotBlank(message = "资产名称不能为空")
        private String assetName;

        /**
        * 标识(首套模、复制模)
        */
        private String tag;

        /**
        * 是否加急
        */
        @NotNull(message = "是否加急不能为空")
        private Boolean isUrgent;

        /**
        * 计划交期
        */
        private LocalDate planDeliverDate;

        /**
        * 申请数量
        */
        @NotNull(message = "申请数量不能为空")
        @DecimalMin(value = "0.0", inclusive = true, message = "申请数量不能小于0")
        private BigDecimal applyQty;

        /**
        * 采购组织id
        */
        @NotBlank(message = "采购组织id不能为空")
        private String purchaseOrgId;

        /**
        * 备注
        */
        @Size(max = 255,message = "采购组织id最大长度不能超过200位")
        private String remark;


    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<AssetNoticeDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }
}