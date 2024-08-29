package com.erp.server.wms.service;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.entity.PackingTaskDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackingTaskDetailDTO;
import com.erp.model.wms.entity.WmsCartonSpecEntity;

import java.util.List;

/**
 * <p>
 * 装箱任务明细表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
public interface PackingTaskDetailService extends SuperService<PackingTaskDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackingTaskDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    Boolean update(PackingTaskDetailDTO.UpdateDTO dto);

    /**
     * 根据主表获取装箱任务明细
     * @param mainIds
     * @return
     */
    List<PackingTaskDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id删除明细
     * @param mainId
     */
    void removeByMainId(String mainId);

    /**
     * 根据主表id进行sku分组统计
     * @param mainIds
     * @return
     */
    List<PackingTaskDTO.DetailDTO> listDetailByMainIds(List<String> mainIds);

    /**
     * 统计发货数量总和
     * @param id
     * @return
     */
    Integer countDeliveryQty(String id);

    /**
     * 模糊搜索装箱任务明细
     * @param taskId
     * @param searchKey
     * @return
     */
    List<PackingTaskDetailDTO.ViewDTO> searchProductBySearchKey(String taskId,String searchKey);

    List<PackingTaskDetailEntity> listBySourceIds(List<String> sourceDetailIds);
}
