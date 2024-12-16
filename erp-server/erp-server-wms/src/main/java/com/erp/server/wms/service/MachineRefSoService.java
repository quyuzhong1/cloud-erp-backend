package com.erp.server.wms.service;
import com.erp.model.wms.entity.MachineRefSoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.MachineRefSoDTO;

import java.util.List;

/**
 * <p>
 * 加工单和销售订单关联表 服务类
 * </p>
 *
 * @author will
 * @since 2023-12-06
 */
public interface MachineRefSoService extends SuperService<MachineRefSoEntity> {

    /**
    * 新增
    * @author will
    * @date: 2023-12-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(MachineRefSoDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2023-12-06
    * @param dto
    * @return
    */
    Boolean update(MachineRefSoDTO.UpdateDTO dto);

    /**
     * @description: 根据加工单明细id集合删除
     * @author Will
     * @date: 2023/12/6 14:55
     * @param machineDetailIdList
     * @return Boolean
     */
    Boolean removeByMachineDetailIdList(List<String> machineDetailIdList);
    /**
     * @description: 根据加工单id集合删除
     * @author Will
     * @date: 2023/12/6 14:58
     * @param machineIdList
     * @return Boolean
     */
    Boolean removeByMachineIdList(List<String> machineIdList);
    /**
     * @description: 根据销售订单明细id集合查询
     * @author Will
     * @date: 2023/12/6 15:05
     * @param refDetailIdList
     * @return List<MachineRefSoEntity>
     */
    List<MachineRefSoEntity> listBySoDetailIdList(List<String> refDetailIdList);
    /**
     * @description: 根据销售订单id集合查询关联订单数据
     * @author Will
     * @date: 2023/12/6 16:06
     * @param soIds
     * @return List<MachineRefSoEntity>
     */
    List<MachineRefSoEntity> listBySoIdList(List<String> soIds);
    /**
     * 根据明细id查询
     * @author will
     * @date 2024/11/18 15:25
     * @param detailIdList
     * @return List<MachineRefSoEntity>
     */
    List<MachineRefSoEntity> listByMachineDetailIdList(List<String> detailIdList);
}
