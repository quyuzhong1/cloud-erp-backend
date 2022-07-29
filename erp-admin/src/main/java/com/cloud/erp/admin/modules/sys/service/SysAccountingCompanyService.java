package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.CompanyPagingSearchDTO;
import com.cloud.erp.admin.modules.sys.dto.SysAccountingCompanyDTO;
import com.cloud.erp.admin.modules.sys.entity.SysAccountingCompanyEntity;
import com.erp.common.dto.BasePagingSearchDTO;
import com.erp.common.dto.BatchStateDTO;
import com.erp.common.dto.PagingDTO;
import com.erp.common.dto.StateDTO;
import com.erp.common.vo.PagingVO;


/**
 * @Classname SysAccountingCompanyService
 * @Description TODO
 * @Date 2022-07-12 9:52
 * @Created by yl
 */
public interface SysAccountingCompanyService extends IService<SysAccountingCompanyEntity> {

    /**
     * 保存公司信息
     * @author yl
     * @date 2022-07-12 10:12
     * @param dto
     * @return boolean
     */

    boolean saveCompany(SysAccountingCompanyDTO dto);


    boolean updateCompany(SysAccountingCompanyDTO dto);

    boolean updateCompanyState(StateDTO dto);


    PagingVO paging(PagingDTO<CompanyPagingSearchDTO> dto);

    boolean batchUpdateCompanyState(BatchStateDTO dto);
}
