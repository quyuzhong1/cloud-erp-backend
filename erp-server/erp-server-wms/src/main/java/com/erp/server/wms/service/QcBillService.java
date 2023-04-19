package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.model.wms.entity.QcBillEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 质检单表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcBillService extends SuperService<QcBillEntity> {


    /**
     * 暂存质检单
     * @param dto
     * @return
     */
    Boolean draft(QcBillDTO.SaveOrUpdateDTO dto);
    /**
     * 根据采购id查询
     */
    List<QcBillEntity> listByPoIds(List<String> poIds);

    /**
     * 质检单详情
     * @author yl
     * @date 2023-04-19 11:53
     * @param id
     * @return com.erp.model.wms.dto.QcBillDTO.ViewDTO
     */
    QcBillDTO.ViewDTO view(String id);

    /**
     * 质检单分页信息
     * @author yl
     * @date 2023-04-19 15:25
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.QcBillDTO.PagingViewDTO>
     */
    PagingVO<QcBillDTO.PagingViewDTO> paging(PagingDTO<QcBillDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @author yl
     * @date 2023-04-19 17:39
     * @param dto
     * @param response
     * @return void
     */
    void exportQcBill(QcBillDTO.ExportDTO dto, HttpServletResponse response);
}
