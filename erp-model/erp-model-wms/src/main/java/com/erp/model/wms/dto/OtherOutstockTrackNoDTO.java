package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 其他出库单跟踪号DTO
 *
 * @author system
 * @since 2025-12-16
 */
@Data
@NoArgsConstructor
public class OtherOutstockTrackNoDTO implements Serializable {

    /**
     * 批量更新跟踪号DTO
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateDTO {
        /**
         * 跟踪号信息列表
         */
        @NotEmpty(message = "跟踪号信息不能为空")
        private List<UpdateDTO> trackNoList;
    }

    /**
     * 更新跟踪号DTO
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 其他出库单ID
         */
        @NotBlank(message = "出库单ID不能为空")
        private String otherOutstockId;

        /**
         * 其他出库单编号
         */
        private String otherOutstockCode;

        /**
         * 跟踪号列表
         */
        @NotEmpty(message = "跟踪号不能为空")
        private List<String> trackNoList;
    }

    /**
     * 查看跟踪号DTO
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 其他出库单ID
         */
        private String otherOutstockId;

        /**
         * 其他出库单编号
         */
        private String otherOutstockCode;

        /**
         * 跟踪号列表
         */
        private List<String> trackNoList;
    }
}
