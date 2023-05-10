package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 14:11
 */
@Data
@NoArgsConstructor
public class TransferApplicationDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 入库单号
         */
        private String code;



    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 主键ids
         */
        private List<String> ids;



    }

    @Data
    @NoArgsConstructor
    public static class ListStatusCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String type;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {


    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends OtherOutstockDTO.CommonDTO {

        /**
         * 来源主键id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * 来源 purchaseOrder采购订单
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<PoInstockDetailDTO.AddDTO> details;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends OtherOutstockDTO.CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<PoInstockDetailDTO.UpdateDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class SupplierDTO {
        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 供应商地址
         */
        private String supplierAddress;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends OtherOutstockDTO.CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 入库单号
         */
        private String  code;

        /**
         * 审核状态
         */
        private String  approveStatus;


        /**
         * 明细
         */
        private List<PoInstockDetailDTO.ViewDTO> details;
    }

}
