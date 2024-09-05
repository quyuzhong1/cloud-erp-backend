package com.erp.server.mrp.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.entity.LabelInfoEntity;
import com.erp.model.mrp.vo.LabelVO;

import java.util.List;

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
    * 修改
    * @author will
    * @date: 2024-08-30
    * @param list
    * @return
    */
    Boolean update(List<LabelInfoDTO.UpdateDTO> list);


    /**
     * 根据补货建议主表id查询标签
     */
    List<LabelVO> listLabelByReplenishmentIds(List<String> ids);
    /**
     * 根据补货建议主表id查询标签
     */
    List<LabelVO> listLabelByReplenishmentId(String id);
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
    /**
     * 列表查询
     * @author will
     * @date 2024/9/4 16:36
     * @return List<LabelInfoEntity>
     */
    List<LabelInfoDTO.ListDTO> listLabelInfo();
}
