package com.erp.model.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Data
@NoArgsConstructor
public class DeclarationGenerationDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputDetailDTO {
        private String id;
        private String shipmentOrderId;
        private String destinationCountry;

        // 合并特征键
        private String hsCode;
        private String customsName;
        private String declarationElements;
        private String declarationUnit;
        private String declarationCurrency;
        private String model;

        // 基础数据
        private String sku;
        private BigDecimal price;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutputDeclarationDTO {
        private String id; // 系统生成单号
        private String destinationCountry;
        private boolean isLocked; // 可合并锁定状态标志
        private List<OutputDeclarationDetailDTO> details;

        public OutputDeclarationDTO(String destinationCountry) {
            this.id = UUID.randomUUID().toString().replace("-", "");
            this.destinationCountry = destinationCountry;
            this.details = new ArrayList<>();
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutputDeclarationDetailDTO {
        private BigDecimal unitPrice;
        private Integer totalQuantity;
        private Set<String> skus;
        private List<String> linkedDetailIds;

    }

    @Data
    @NoArgsConstructor
    public static class MergeKey {
        private  String hsCode;
        private  String customsName;
        private  String declarationElements;
        private  String declarationUnit;
        private  String declarationCurrency;
        private  String model;

        private MergeKey(String hsCode, String customsName, String elements, String unit, String currency, String model) {
            this.hsCode = hsCode == null ? "" : hsCode;
            this.customsName = customsName == null ? "" : customsName;
            this.declarationElements = elements == null ? "" : elements;
            this.declarationUnit = unit == null ? "" : unit;
            this.declarationCurrency = currency == null ? "" : currency;
            this.model = model == null ? "" : model;
        }

        public static MergeKey from(DeclarationGenerationDTO.InputDetailDTO dto) {
            return new MergeKey(
                    dto.getHsCode(),
                    dto.getCustomsName(),
                    dto.getDeclarationElements(),
                    dto.getDeclarationUnit(),
                    dto.getDeclarationCurrency(),
                    dto.getModel()
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentBlock {
        private  String shipmentId;
        private  List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> details;

        public int getSize() {
            return details.size();
        }
    }
}
