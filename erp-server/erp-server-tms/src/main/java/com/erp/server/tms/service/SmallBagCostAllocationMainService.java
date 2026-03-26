package com.erp.server.tms.service;
import com.erp.model.tms.entity.SmallBagCostAllocationMainEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.SmallBagCostAllocationMainDTO;

import java.util.List;

/**
 * <p>
 * 小包费用分摊主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-05
 */
public interface SmallBagCostAllocationMainService extends SuperService<SmallBagCostAllocationMainEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-05
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SmallBagCostAllocationMainDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-05
    * @param dto
    * @return
    */
    Boolean update(SmallBagCostAllocationMainDTO.UpdateDTO dto);

    /**
     * 更新大表生成状态
     * @param id
     * @param bigTableStatus
     * @return
     */
    Boolean updateBigTableStatus(String id, String bigTableStatus);
    /**
     * 根据费用id列表查询小包费用分摊主表信息
     * @author will
     * @date 2026/1/30 18:23
     * @param costIdList
     * @return List<SmallBagCostAllocationMainEntity>
     */
    List<SmallBagCostAllocationMainEntity> listByCostIdList(List<String> costIdList);
}
