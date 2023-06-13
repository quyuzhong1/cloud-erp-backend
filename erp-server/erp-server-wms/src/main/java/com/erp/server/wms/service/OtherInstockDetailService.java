package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.OtherInstockDetailDTO;
import com.erp.model.wms.entity.OtherInstockDetailEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface OtherInstockDetailService extends SuperService<OtherInstockDetailEntity> {
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/17 15:32
     * @param detailList
     * @param mainId
     */
    void add(List<OtherInstockDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/17 15:49
     * @param detailList
     * @param mainId
     */
    void update(List<OtherInstockDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/5/17 15:55
     * @param mainId
     * @return List<OtherInstockDetailEntity>
     */
    List<OtherInstockDetailEntity> listByMainId(String mainId);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/5/19 14:11
     * @param mainIds
     * @return List<OtherInstockDetailEntity>
     */
    List<OtherInstockDetailEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表ids删除
     * @author Will
     * @date: 2023/5/17 15:56
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
}
