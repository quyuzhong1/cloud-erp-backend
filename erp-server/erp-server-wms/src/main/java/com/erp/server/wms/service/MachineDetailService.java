package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.entity.MachineDetailEntity;

import java.util.List;

/**
 * <p>
 * 加工单明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface MachineDetailService extends SuperService<MachineDetailEntity> {
    /**
     * @description: 新增明细
     * @author Will
     * @date: 2023/5/16 14:24
     * @param detailList
     * @param mainId
     */
    void add(List<MachineDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/16 14:41
     * @param detailList
     * @param mainId
     */
    void update(List<MachineDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 根据mainId查询
     * @author Will
     * @date: 2023/5/16 14:50
     * @param mainId
     * @return List<MachineDetailEntity>
     */
    List<MachineDetailEntity> listByMainId(String mainId);
    /**
     * @description: 根据mainIds查询
     * @author Will
     * @date: 2023/5/16 18:31
     * @param mainIds
     * @return List<MachineDetailEntity>
     */
    List<MachineDetailEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/5/16 15:00
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);

}
