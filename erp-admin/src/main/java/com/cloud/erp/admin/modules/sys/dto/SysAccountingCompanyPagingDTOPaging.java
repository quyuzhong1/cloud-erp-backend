package com.cloud.erp.admin.modules.sys.dto;

import com.cloud.erp.common.common.dto.BasePagingSearchDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname SysAccountingCompanyPaingDTO
 * @Description TODO
 * @Date 2022-07-12 11:29
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysAccountingCompanyPagingDTOPaging extends BasePagingSearchDTO {

    //状态
    private Integer state;
}
