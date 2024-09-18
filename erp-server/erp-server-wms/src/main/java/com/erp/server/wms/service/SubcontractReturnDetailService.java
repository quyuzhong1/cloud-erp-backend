package com.erp.server.wms.service;
import com.erp.model.wms.entity.SubcontractReturnDetailEntity;
import com.erp.model.wms.entity.SubcontractReturnDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SubcontractReturnDetailDTO;

import java.util.List;

/**
 * <p>
 * 委外退料明细单 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
public interface SubcontractReturnDetailService extends SuperService<SubcontractReturnDetailEntity> {

//    /**
//    * 新增
//    * @author zdy
//    * @date: 2024-09-15
//    * @param dto
//    * @return
//    */
//    BaseResultDTO.AddDTO add(SubcontractReturnDetailDTO.AddDTO dto);
//
//    /**
//    * 修改
//    * @author zdy
//    * @date: 2024-09-15
//    * @param dto
//    * @return
//    */
//    Boolean update(SubcontractReturnDetailDTO.UpdateDTO dto);

    /**
     * 根据委外订单明细id获取委外退料明细
     * @param subcontractOrderDetailIdList
     * @return
     */
    List<SubcontractReturnDetailEntity> listBySubcontractOrderDetailIdList(List<String> subcontractOrderDetailIdList);

    /**
     * 新增明细
     * @param detailList
     * @param id
     */
    void add(List<SubcontractReturnDetailDTO.AddDTO> detailList, String id);

    /**
     * 更新明细
     * @param detailList
     * @param id
     */
    Boolean update(List<SubcontractReturnDetailDTO.UpdateDTO> detailList, String id);

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2024/1/9 14:43
     * @param mainIdList
     * @return List<SubcontractReturnDetailEntity>
     */
    List<SubcontractReturnDetailEntity> listByMainIds(List<String> mainIdList);

    /**
     * @description: 根据mainId删除
     * @author Will
     * @date: 2024/1/9 15:46
     * @param mainId
     */
    void deleteByMainId(String mainId);

    /**
     * @description: 根据来源明细id集合查询
     * @author Will
     * @date: 2024/1/9 16:56
     * @param sourceDetailIdList
     * @return List<SubcontractReturnDetailEntity>
     */
    List<SubcontractReturnDetailEntity> listBySourceDetailIdList(List<String> sourceDetailIdList);

}
