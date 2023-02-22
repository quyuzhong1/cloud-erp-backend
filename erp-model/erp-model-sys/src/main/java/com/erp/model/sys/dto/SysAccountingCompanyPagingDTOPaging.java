package com.erp.model.sys.dto;



import com.common.business.dto.base.BasePagingSearchDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Classname SysAccountingCompanyPaingDTO
 * @Description TODO
 * @Date 2022-07-12 11:29
 * @Created by yl
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class SysAccountingCompanyPagingDTOPaging extends BasePagingSearchDTO {

    //状态
    private Integer state;
}
