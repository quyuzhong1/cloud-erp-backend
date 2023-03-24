package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.entity.PurchaseChangeEntity;
import com.erp.server.scm.mapper.PurchaseChangeMapper;
import com.erp.server.scm.service.PurchaseChangeService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseChangeServiceImpl extends SuperServiceImpl<PurchaseChangeMapper, PurchaseChangeEntity> implements PurchaseChangeService {

    @Override
    public PagingVO<PurchaseChangeDTO.ListDTO> paging(PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(PurchaseChangeDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseChangeDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public PurchaseChangeDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean exportExcel(PurchaseChangeDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseChangeDTO.AddDTO dto) {
        return null;
    }


}
