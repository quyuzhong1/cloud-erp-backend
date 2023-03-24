package com.erp.server.wms.service;


import com.common.business.service.SuperService;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.DictBasicEntity;

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

    
    /**
     * 根据key list 获取对应数据
     * @author yl
     * @date 2023-03-20 14:24
     * @param keyList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
     */
    List<DictBasicEntity> getByKeyList(List<String> keyList);
}
