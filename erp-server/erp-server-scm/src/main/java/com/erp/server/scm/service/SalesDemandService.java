package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandPagingViewDTO;
import com.erp.model.scm.dto.SalesDemandPagingParamDTO;
import com.erp.model.scm.entity.SalesDemandEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求主表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
public interface SalesDemandService extends SuperService<SalesDemandEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/15 16:48
     * @param dto
     * @return PagingVO<List<ScmSalesDemandDTO>>
     */
    PagingVO<List<SalesDemandPagingViewDTO>> paging(PagingDTO<SalesDemandPagingParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/15 17:35
     * @param salesDemandDTO
     * @return Boolean
     */
    Boolean add(SalesDemandDTO salesDemandDTO);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/15 17:35
     * @param salesDemandDTO
     * @return Boolean
     */
    Boolean update(SalesDemandDTO salesDemandDTO);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ScmSalesDemandDTO
     */
    SalesDemandDTO view(String id);
    /**
     * @description: 批量作废
     * @author Will
     * @date: 2023/3/15 17:51
     * @param ids
     * @return Boolean
     */
    Boolean invalid(List<String> ids);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/3/15 17:54
    * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return Boolean
     */
    Boolean cancelProcess(String id);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param salesDemandPagingParamDTO
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(SalesDemandPagingParamDTO salesDemandPagingParamDTO, HttpServletResponse response);
    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/15 18:19
     * @param ids
     * @return Boolean
     */
    Boolean unApprove(List<String> ids);
    /**
     * @description: 获取备货单号
     * @author Will
     * @date: 2023/3/16 10:53
     * @return String
     */
    String getCode();

    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:21
     * @param id
     * @return Boolean
     */
    Boolean delete(String id);
}
