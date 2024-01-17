package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName PurchaseOrderSrmDTO
 * @description: TODO
 * @date 2024年01月16日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class PurchaseOrderSrmDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class RequestDTO {
        @NotNull(message = "供应商id不能为空")
        private String supplierId;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 全部  all
         * 待确认  toBeConfirm
         * 已确认  confirm
         * 已拒绝  reject
         * 送货中  delivery
         * 已完成  finish
         * 已关闭  closed
         *
         */
        private String executionStatus;
        /**
         * 审核状态
         */
        private String approveStatus;
    }
}
