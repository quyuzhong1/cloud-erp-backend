package com.erp.server.plm.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.plm.entity.BasicDictEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * plm 字典表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface BasicDictService extends IService<BasicDictEntity> {
	
	boolean saveJsonObject(JSONObject jsonObject);
	
	boolean updateJsonObject(List<JSONObject> jsonObjects);

    Boolean saveOrUpdateDict(List<BasicDictDTO> dtos);

    List<BasicDictEntity>  listByType(String type);

    /**
     * 批量获取字典
     * @param typeList
     * @return
     */
    List<BasicDictEntity>  listByTypeList(List<String> typeList);

    /**
     * key = value,value = name
     * @param type
     * @return
     */
    Map<String,String>  mapByType(String type);

    /**
     * 根据类型和值查询字典信息
     */
    BasicDictEntity  listByTypeAndValue(String type,String value);

    /**
     * 根据id集合批量查询字典信息
     * @Author Luo_WG
     * @Date 2022/10/22 19:50
     * @param list id集合
     * @return java.util.List<com.erp.model.plm.entity.BasicDictEntity>
     **/
    List<BasicDictEntity> listByIds(List<String> list);

    /**
     * @Description 根据名称查询字段是否存在
     * @Author Luo_WG
     * @Date 2022/9/29 11:02
     * @param type:字典类型
     * @param value:字典值
     * @return com.erp.model.plm.entity.BasicDictEntity
     **/
    BasicDictEntity checkBasicDict(String type, String value);

    /**
     * 字典下拉框
     * @param code
     * @return
     */
    List<DictControllerDTO.DictDropDownDTO> listDictDropDown(String code);
}
