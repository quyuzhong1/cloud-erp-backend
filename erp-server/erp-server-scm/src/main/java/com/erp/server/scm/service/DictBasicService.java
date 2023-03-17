package com.erp.server.scm.service;


import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.DictBasicEntity;

import java.util.List;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {

    
    /**
     * 保存或者修改字典信息
     * @author yl
     * @date 2023-03-17 12:21
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateDict(List<DictBasicDTO> dto);

    /**
     * 根据key 获取字典数据
     * @author yl
     * @date 2023-03-17 14:16
     * @param key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     */
    List<DictBasicDTO> getByKey(String key);
}
