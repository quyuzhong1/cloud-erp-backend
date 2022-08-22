package com.erp.model.sys.dto;



import com.erp.common.dto.base.BasePagingSearchDTO;
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
