package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.TransferOutDetailDTO;
import com.erp.model.wms.entity.TransferOutDetailEntity;

import java.util.List;

/**
 * <p>
 * 分步式调出单明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferOutDetailService extends SuperService<TransferOutDetailEntity> {
    /**
     * @description: 根据来源ids查询
     * @author Will
     * @date: 2023/5/18 9:44
     * @param sourceDetailIds
     * @return List<TransferOutDetailEntity>
     */
    List<TransferOutDetailEntity> listSourceDetailIds(List<String> sourceDetailIds);

    /**
     * 分步式调出单明细新增
     * @param detailList
     * @param mainId
     */
    void add(List<TransferOutDetailDTO.AddDTO> detailList, String mainId);

    /**
     * 分步式调出单明细修改
     * @param detailList
     * @param mainId
     */
    void update(List<TransferOutDetailDTO.UpdateDTO> detailList, String mainId);

    /**
     * 根据主表id查询
     * @param mainId
     * @return
     */
    List<TransferOutDetailEntity> listByMainId(String mainId);

    /**
     * 根据主表id集合查询
     * @param mainIds
     * @return
     */
    List<TransferOutDetailEntity> listByMainIds(List<String> mainIds);
}
