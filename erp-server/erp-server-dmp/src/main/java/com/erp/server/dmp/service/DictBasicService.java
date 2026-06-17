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
    List<DictBasicEntity> getByKey(String key);

    /**
     * 根据key list 获取对应数据
     * @author zdy
     * @date 2023-03-20 14:24
     * @param keyList
     * @return java.util.List<DictBasicEntity>
     */
    List<DictBasicEntity> getByKeyList(List<String> keyList);
}
