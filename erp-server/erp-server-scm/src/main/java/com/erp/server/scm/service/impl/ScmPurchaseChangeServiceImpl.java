package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.ScmPurchaseChangeDTO;
import com.erp.model.scm.dto.ScmPurchaseChangePagingParamDTO;
import com.erp.model.scm.dto.ScmPurchaseChangePagingViewDTO;
import com.erp.model.scm.entity.ScmPurchaseChangeEntity;
import com.erp.server.scm.mapper.ScmPurchaseChangeMapper;
import com.erp.server.scm.service.ScmPurchaseChangeService;
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
public class ScmPurchaseChangeServiceImpl extends SuperServiceImpl<ScmPurchaseChangeMapper, ScmPurchaseChangeEntity> implements ScmPurchaseChangeService {

    @Override
    public PagingVO<List<ScmPurchaseChangePagingViewDTO>> paging(PagingDTO<ScmPurchaseChangePagingParamDTO> dto) {
        return null;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public Boolean add(ScmPurchaseChangeDTO scmPurchaseChangeDTO) {
        return null;
    }

    @Override
    public Boolean update(ScmPurchaseChangeDTO scmPurchaseChangeDTO) {
        return null;
    }

    @Override
    public ScmPurchaseChangeDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(String id) {
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
    public Boolean unApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(ScmPurchaseChangePagingParamDTO scmPurchaseChangePagingParamDTO, HttpServletResponse response) {
        return null;
    }


}
