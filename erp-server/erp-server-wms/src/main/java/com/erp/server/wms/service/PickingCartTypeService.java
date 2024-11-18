package com.erp.server.wms.service;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.model.wms.entity.PickingCartTypeEntity;

import java.util.List;

/**
 * <p>
 * 拣货车类型 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
public interface PickingCartTypeService extends SuperService<PickingCartTypeEntity> {


    /**
     * 批量更新
     * @author will
     * @date 2024/6/20 17:11
     * @param list
     * @return Boolean
     */
    Boolean batchUpdate(List<PickingCartTypeDTO.BatchUpdateDTO> list);
    /**
     * 拣货车类型查询
     * @author will
     * @date 2024/6/20 18:22
     * @param selectDTO
     * @return List<ListDTO>
     */
    List<PickingCartTypeDTO.ListDTO> select(PickingCartTypeDTO.SelectDTO selectDTO);
    /**
     * 删除拣货类型
     * @author will
     * @date 2024/6/24 10:43
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * 验证是否被用
     * @author will
     * @date 2024/6/25 16:39
     * @param id
     * @return Boolean
     */
    Boolean checkIsUsed(String id);
}
