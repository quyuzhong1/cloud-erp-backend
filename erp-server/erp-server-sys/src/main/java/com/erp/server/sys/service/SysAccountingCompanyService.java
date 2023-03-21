package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BatchStateDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CompanyPagingSearchDTO;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;

import java.util.List;


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

    boolean updateCompanyState(UpdateStateDTO dto);


    PagingVO paging(PagingDTO<CompanyPagingSearchDTO> dto);

    boolean batchUpdateCompanyState(BatchStateDTO dto);

    /**
     * 获取核算组织
     * @author yl
     * @date 2023-03-21 17:44
     * @param
     * @return java.util.List<com.erp.model.sys.dto.SysAccountingCompanyDTO.ListDTO>
     */
    List<SysAccountingCompanyDTO.ListDTO> getList();
}
