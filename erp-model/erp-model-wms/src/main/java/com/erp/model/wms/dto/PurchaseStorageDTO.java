package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 11:11
 */
@Data
@NoArgsConstructor
public class PurchaseStorageDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

    }
}
