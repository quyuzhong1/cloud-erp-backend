package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineSubComponentsEntity;

import java.util.List;

/**
 * <p>
 * 加工单子件明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface MachineSubComponentsService extends SuperService<MachineSubComponentsEntity> {
    /**
     * @description: 新增子件信息
     * @author Will
     * @date: 2023/5/17 14:38
     * @param addList
     * @param detailId
     */
    void add(List<MachineSubComponentsDTO.AddDTO> addList, String detailId);
    /**
     * @description: 修改子件信息
     * @author Will
     * @date: 2023/5/17 14:52
     * @param addList
     * @param id
     */
    void update(List<MachineSubComponentsDTO.UpdateDTO> addList, String id);
    /**
     * @description: 根据明细id查询子件明细
     * @author Will
     * @date: 2023/5/17 15:10
     * @param detailId
     * @return List<MachineSubComponentsEntity>
     */
    List<MachineSubComponentsEntity> listByDetailId(String detailId);
    /**
     * @description: 根据明细ids查询子件明细
     * @author Will
     * @date: 2023/5/17 15:10
     * @param detailIds
     * @return List<MachineSubComponentsEntity>
     */
    List<MachineSubComponentsEntity> listByDetailIds(List<String> detailIds);
    /**
     * @description: 根据加工单ids删除子件
     * @author Will
     * @date: 2023/5/18 12:25
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
}
