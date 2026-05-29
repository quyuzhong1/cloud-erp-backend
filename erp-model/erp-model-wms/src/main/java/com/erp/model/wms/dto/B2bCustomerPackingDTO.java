package com.erp.model.wms.dto;

import com.common.business.dto.AttachDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * B2B客户装箱
 */
@Data
@NoArgsConstructor
public class B2bCustomerPackingDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;
        private String mainId;
        private Integer boxSeq;
        private String boxMarkNo;
        private String boxMarkRefNo;
        private String labelSize;
        private String labelingRequirement;
        private String skuId;
        private String skuNo;
        private String productName;
        private Integer saleQty;
        private Integer packingQty;
        private String warehousePlatformSku;
        private Integer sort;
        private List<WmsAttachmentDTO.UpdateDTO> attachList;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        private String id;

        @NotNull(message = "序号不能为空")
        private Integer boxSeq;

        @Size(max = 50, message = "箱唛号最大长度不能超过50位")
        private String boxMarkNo;

        @Size(max = 50, message = "箱唛参考号最大长度不能超过50位")
        private String boxMarkRefNo;

        @Size(max = 20, message = "标签尺寸最大长度不能超过20位")
        private String labelSize;

        @Size(max = 200, message = "贴标要求最大长度不能超过200位")
        private String labelingRequirement;

        @NotBlank(message = "SKU不能为空")
        @Size(max = 255, message = "SKU最大长度不能超过255位")
        private String skuNo;

        private String skuId;
        private String productName;
        private Integer saleQty;

        @NotNull(message = "装箱数量不能为空")
        private Integer packingQty;

        private String warehousePlatformSku;

        private Integer sort;

        private List<AttachDTO> attachList;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        private List<ViewDTO> successList;
        private String errorUrl;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {

        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;
        private String packingType;
        private List<B2bThirdDeliveryDetailDTO.AddDTO> detailList;
    }
}
