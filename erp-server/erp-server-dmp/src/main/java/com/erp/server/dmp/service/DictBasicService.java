package com.erp.server.dmp.service;

import com.alibaba.fastjson.JSONObject;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.entity.DictBasicEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description:  字典表 服务类
 * @date 2023/10/16 15:30
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {

	boolean saveJsonObject(JSONObject jsonObject);
	
	boolean updateJsonObject(List<JSONObject> jsonObjects);
	
    /**
     * @description: 新增或修改
     * @author Will
     * @date: 2023/10/16 15:33
     * @param dto
     * @return Boolean
     */
    Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> dto);

    /**
     * @description: 查询字典
     * @author Will
     * @date: 2023/10/16 15:34
     * @param key
     * @return List<ViewDTO>
     */
    List<DictBasicDTO.ViewDTO> getByKey(String key);
}
