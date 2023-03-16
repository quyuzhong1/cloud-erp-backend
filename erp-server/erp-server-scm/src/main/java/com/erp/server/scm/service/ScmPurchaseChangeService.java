package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.ScmPurchaseChangeDTO;
import com.erp.model.scm.dto.ScmPurchaseChangePagingParamDTO;
import com.erp.model.scm.dto.ScmPurchaseChangePagingViewDTO;
import com.erp.model.scm.entity.ScmPurchaseChangeEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求明细表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface ScmPurchaseChangeService extends SuperService<ScmPurchaseChangeEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/16 12:06
     * @param dto
     * @return PagingVO<List<ScmPurchaseChangePagingViewDTO>>
     */
    PagingVO<List<ScmPurchaseChangePagingViewDTO>> paging(PagingDTO<ScmPurchaseChangePagingParamDTO> dto);
    /**
     * @description: 获取变更单号
     * @author Will
     * @date: 2023/3/16 12:08
     * @return String
     */
    String getCode();
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/16 12:08
     * @param scmPurchaseChangeDTO
     * @return Boolean
     */
    Boolean add(ScmPurchaseChangeDTO scmPurchaseChangeDTO);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 12:08
     * @param scmPurchaseChangeDTO
     * @return Boolean
     */
    Boolean update(ScmPurchaseChangeDTO scmPurchaseChangeDTO);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/3/16 12:09
     * @param id
     * @return ScmPurchaseChangeDTO
     */
    ScmPurchaseChangeDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 12:09
     * @param id
     * @return Boolean
     */
    Boolean delete(String id);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/3/16 12:09
     * @param ids
     * @return Boolean
     */
    Boolean invalid(List<String> ids);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/3/16 12:09
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/3/16 12:09
     * @param ids
     * @return Boolean
     */
    Boolean unApprove(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/16 12:13
     * @param scmPurchaseChangePagingParamDTO
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(ScmPurchaseChangePagingParamDTO scmPurchaseChangePagingParamDTO, HttpServletResponse response);
}
