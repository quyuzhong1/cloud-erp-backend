package com.erp.server.tms.service;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;

import java.util.List;

/**
 * <p>
 * 期初头程分摊明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
 */
public interface InitFirstMileAllocationDetailService extends SuperService<InitFirstMileAllocationDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InitFirstMileAllocationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-13
    * @param dto
    * @return
    */
    Boolean update(InitFirstMileAllocationDetailDTO.UpdateDTO dto);

    /**
     * 构建新增分摊明细数据
     * @param detailEntityList
     * @param id
     */
    void buildDetail(List<InitFirstMileAllocationDetailEntity> detailEntityList, String id);

    /**
     * 根据来源单获取明细记录
     * @param sourceIds
     * @return
     */
    List<InitFirstMileAllocationDetailEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 根据主表id获取明细记录
     * @param mainIds
     * @return
     */
    List<InitFirstMileAllocationDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表记录删除明细
     * @param id
     */
    void removeByMainId(String id);
}
