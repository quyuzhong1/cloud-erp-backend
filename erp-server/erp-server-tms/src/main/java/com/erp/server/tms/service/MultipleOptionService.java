package com.erp.server.tms.service;
import com.erp.model.tms.entity.MultipleOptionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.MultipleOptionDTO;

import java.util.List;

/**
 * <p>
 * 多选下拉存储表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
public interface MultipleOptionService extends SuperService<MultipleOptionEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-24
    * @param dto
    * @return
    */
    void add(MultipleOptionDTO.AddDTO dto);

    /**
     * 根据主表id删除
     * @Author Luo_WG
     * @Date 2024/1/24 17:25
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByMainIds(List<String> mainIds);

    /**
     * 根据主标Id查询配置信息
     * @Author Luo_WG
     * @Date 2024/1/24 17:33
     * @param mainIds
     * @return java.util.List<com.erp.model.tms.entity.MultipleOptionEntity>
     **/
    List<MultipleOptionEntity> listByMainIds(List<String> mainIds);
}
