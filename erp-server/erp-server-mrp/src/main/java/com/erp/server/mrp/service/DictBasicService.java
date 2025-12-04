package com.erp.server.mrp.service;

import com.alibaba.fastjson.JSONObject;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.DictBasicDTO;
import com.erp.model.mrp.entity.DictBasicEntity;

import java.util.List;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {

	boolean saveJsonObject(JSONObject jsonObject);
	
	boolean updateJsonObject(List<JSONObject> jsonObjects);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(DictBasicDTO.UpdateDTO dto);

    /**
     * 保存或者修改字典信息
     * @author yl
     * @date 2023-03-17 12:21
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> dto);

    /**
     * 根据key 获取字典数据
     * @author yl
     * @date 2023-03-17 14:16
     * @param key
     * @return java.util.List<com.erp.model.tms.dto.DictBasicDTO>
     */
    List<DictBasicDTO.ViewDTO> getByKey(String key);


    /**
     * 根据key list 获取对应数据
     * @author yl
     * @date 2023-03-20 14:24
     * @param keyList
     * @return java.util.List<com.erp.model.tms.entity.DictBasicEntity>
     */
    List<DictBasicEntity> getByKeyList(List<String> keyList);

    /**
     * 根据类型和值获取到对应信息
     * @author yl
     * @date 2023-06-28 16:25
     * @param type
     * @param value
     * @return com.erp.model.tms.entity.DictBasicEntity
     */
    DictBasicEntity getByTypeAndValue(String type, String value);

    /**
     * 树结构
     * @param type 类型
     */
    List<DictBasicDTO.TreeDTO> treeByType(String type);
}
