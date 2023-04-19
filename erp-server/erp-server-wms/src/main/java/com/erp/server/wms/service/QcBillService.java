package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.model.wms.entity.QcBillEntity;

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
}
