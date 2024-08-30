package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.LabelInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.LabelInfoDTO;

/**
 * <p>
 * 标签信息表 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
public interface LabelInfoService extends SuperService<LabelInfoEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LabelInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-30
    * @param dto
    * @return
    */
    Boolean update(LabelInfoDTO.UpdateDTO dto);

    /**
     * 批量删除
     * @author will
     * @date 2024/8/30 14:25
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * 变更启禁用
     * @author will
     * @date 2024/8/30 14:35
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO updateDisabled(String id,Boolean disabled);
}
