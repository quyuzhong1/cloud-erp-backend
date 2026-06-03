package com.erp.model.plm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

import com.common.business.dto.AdvanceQueryDTO;

import java.util.Map;

/**
 * <p>
 * sku标准成本表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Data
@NoArgsConstructor
public class SkuStdCostDTO implements Serializable {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO extends SortDTO {


        @NotNull(message = "SKU IDS不能为空")
        private List<String> skuIds;

        @NotNull(message = "billDate不能为空")
        private LocalDate billDate;
    }

    @Data
    @NoArgsConstructor
    public static class AutoFetchDTO implements Serializable {

        @NotBlank(message = "SKU标准成本ID不能为空")
        private String id;

        @NotBlank(message = "组织ID不能为空")
        private String orgId;

        @NotBlank(message = "仓库ID不能为空")
        private String warehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class AutoFetchBatchDTO implements Serializable {

        @NotEmpty(message = "SKU标准成本IDS不能为空")
        private List<String> ids;

        @NotBlank(message = "组织ID不能为空")
        private String orgId;

        @NotBlank(message = "仓库ID不能为空")
        private String warehouseId;
    }

}
