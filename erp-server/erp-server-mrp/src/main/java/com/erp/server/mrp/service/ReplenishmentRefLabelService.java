package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.dto.ReplenishmentRefLabelDTO;
import com.erp.model.mrp.entity.ReplenishmentRefLabelEntity;

import java.util.List;

/**
 * <p>
 * 补货建议标签关系表 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
public interface ReplenishmentRefLabelService extends SuperService<ReplenishmentRefLabelEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-30
    * @param dto
    * @return
    */
    Boolean update(ReplenishmentRefLabelDTO.UpdateDTO dto,String refId);

    /**
     * 取消标签
     * @author will
     * @date 2024/8/30 16:19
     * @param labelIdList
     * @param id
     */
    void deleteLabel(List<String> labelIdList, String id);
    /**
     * 根据关联id查询
     * @author will
     * @date 2024/9/4 16:44
     * @param refId
     * @return List<ViewDTO>
     */
    List<LabelInfoDTO.ViewDTO> listLabelInfoByRefId(String refId);
}
