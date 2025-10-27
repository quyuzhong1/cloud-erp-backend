package com.erp.server.wms.service;


import com.common.business.dto.base.BaseDropDownDTO;
import com.alibaba.fastjson.JSONObject;
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
	
	boolean saveJsonObject(JSONObject jsonObject);
	
	boolean updateJsonObject(List<JSONObject> jsonObjects);
    
    /**
     * 保存或者修改字典信息
     * @author yl
     * @date 2023-03-17 12:21
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateDict(List<DictBasicDTO.ListDTO> dto);

    /**
     * 根据key 获取字典数据
     * @author yl
     * @date 2023-03-17 14:16
     * @param key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     */
    List<DictBasicDTO.ListDTO> getByKey(String key);

    
    /**
     * 根据key list 获取对应数据
     * @author yl
     * @date 2023-03-20 14:24
     * @param keyList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
     */
    List<DictBasicEntity> getByKeyList(List<String> keyList);
    /**
     * @description: 下拉列表
     * @author Will
     * @date: 2023/5/11 11:52
     * @param type
     * @param remark
     * @return List<DropDownDTO>
     */
    List<DictBasicDTO.DropDownDTO> listByType(String type, String remark);

    /**
     * 根据类型和值获取到对应信息
     * @author yl
     * @date 2023-06-28 16:25
     * @param type
     * @param value
     * @return com.erp.model.oms.entity.DictBasicEntity
     */
    DictBasicEntity getByTypeAndValue(String type, String value);
}
