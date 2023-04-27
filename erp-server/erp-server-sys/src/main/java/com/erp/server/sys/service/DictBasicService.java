package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.DictBasicEntity;

import java.util.List;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {

    
    /**
     * 保存或者修改字典信息
     * @author yl
     * @date 2023-04-26 15:16
     * @param list
     * @return java.lang.Boolean
     */
    Boolean addOrUpdate(List<DictBasicDTO.AddOrUpdateDTO> list);

    
    /**
     * 根据类型获取字典值
     * @author yl
     * @date 2023-04-26 15:33
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.DictBasicDTO.ViewDTO>
     */
    List<DictBasicDTO.ViewDTO> listByType(String type);

    /**
     * 根据值获取字典信息
     * @author yl
     * @date 2023-04-27 14:58
     * @param itemRoleValueList
     * @return java.util.List<com.erp.model.sys.entity.DictBasicEntity>
     */
    List<DictBasicEntity> listByValues(List<String> itemRoleValueList);
}
