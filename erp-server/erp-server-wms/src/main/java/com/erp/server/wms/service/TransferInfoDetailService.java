package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.TransferInfoDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 直接调拨单明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferInfoDetailService extends SuperService<TransferInfoDetailEntity> {
    /**
     * @description: 新增明细
     * @author Will
     * @date: 2023/5/15 11:39
     * @param detailList
     * @param mainId
     */
    void add(List<TransferInfoDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改明细
     * @author Will
     * @date: 2023/5/15 11:51
     * @param detailList
     * @param mainId
     */
    void update(List<TransferInfoDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/5/15 11:51
     * @param mainId
     * @return List<TransferInfoDetailEntity>
     */
    List<TransferInfoDetailEntity> listByMainId(String mainId);
    /**
     * @description: 根据主表ids删除
     * @author Will
     * @date: 2023/5/15 12:01
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/5/15 14:28
     * @param mainIds
     * @return List<TransferInfoDetailEntity>
     */
    List<TransferInfoDetailEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/5/15 16:19
     * @param sourceDetailIds
     * @return List<TransferInfoDetailEntity>
     */
    List<TransferInfoDetailEntity> listSourceDetailIds(List<String> sourceDetailIds);

    /**
     * 查询调拨明细信息包含审批需要信息 ： 仓管员
     * @param mainIds 主表数据id
     * @param isApprove 是否包含审核信息
     * @return
     */
    Map<String, List<TransferInfoDetailDTO.ApproveDTO>> listApproveByMainIds(List<String> mainIds, Boolean isApprove);
}
