package com.erp.server.workflow.service;

import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.DictBasicEntity;
import com.alibaba.fastjson.JSONObject;
import com.common.business.service.SuperService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {
	
	boolean saveJsonObject(JSONObject jsonObject);
	
	boolean updateJsonObject(List<JSONObject> jsonObjects);
    /**
     * 下拉列表
     * @param type
     * @param remark
     * @return
     */
    List<DictBasicDTO.DropDownDTO> listByType(String type, String remark);
    /**
     * 根据类型和值查询
     * @author will
     * @date 2025/5/12 16:35
     * @param value
     * @param key
     * @return DictBasicEntity
     */
    DictBasicEntity getByTypeAndValue(String value, String key);

    List<DictBasicEntity> getByType(String type);

    Map<String,DictBasicEntity> getMapByType(String type);


}
