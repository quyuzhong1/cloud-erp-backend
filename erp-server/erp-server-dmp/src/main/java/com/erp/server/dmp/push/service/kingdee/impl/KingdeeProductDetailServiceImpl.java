package com.erp.server.dmp.push.service.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import com.erp.server.dmp.service.PlatformService;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 18:03
 */
@Service
public class KingdeeProductDetailServiceImpl implements KingdeeProductDetailService {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;


    @Override
    public void pushProductDetail(Map<String, Object> map) {
        //传入map数据不能为空
        if (ObjectUtils.isEmpty(map) || map.size() == 0) {
            throw new ServiceException(ApiError.Default);
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getName());
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(platformEntity.getId());
        dto.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        if (CollectionUtils.isNotEmpty(mapList)) {
            throw new ServiceException(ApiError.ERROR_97025);
        }
        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());

        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);

        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();
        //用于记录结果
        StringBuilder info = new StringBuilder();
        //业务对象标识
        String formId = "BD_MATERIAL";

        Map<String,Object> resultMap = new LinkedHashMap<>();

        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mapList) {
            //第三方系统逗号分割多层结构
            String apiField = cfgApiFieldMapDTO.getApiField();
            List<String> apiFields = Arrays.stream(apiField.split(",")).collect(Collectors.toList());
            for (int i = 0; i < apiFields.size() ; i++ ) {
                //给不同结构的外部字段赋值
                handleResultMap(cfgApiFieldMapDTO,cfgApiFieldMapValueList,map,resultMap,apiFields,i);
            }
        }


        //调用接口
        String resultJson = null;
        try {
            resultJson = client.save(formId,"jsonData");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 处理结果集Map
     * @author Will
     * @date: 2023/1/12 12:05
     * @param cfgApiFieldMapDTO
     * @param cfgApiFieldMapValueList
     * @param map
     * @param resultMap
     * @param apiFields
     * @param i
     */
    private void handleResultMap(CfgApiFieldMapDTO cfgApiFieldMapDTO,List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList,Map<String,Object> map,Map<String,Object> resultMap,List<String> apiFields,int i) {
        if (i == 0) {
            //第一层结构时
            if (apiFields.size() == 1 ) {
                //如果只有一层结构则直接插入resultMap
                putValueResultMap(cfgApiFieldMapDTO,cfgApiFieldMapValueList,map,resultMap,apiFields,i);
            } else {
                Object obj = resultMap.get(apiFields.get(i));
                if (ObjectUtils.isNull(obj)) {
                    resultMap.put(apiFields.get(i),new LinkedHashMap<>());
                }
            }
        } else if (i == apiFields.size() - 1){
            //获取上一级Map对象
            Map<String, Object> parentMap = getParentMap(resultMap, apiFields, i);
            //如果时最后一层结构则插入值到上一层Map中
            putValueResultMap(cfgApiFieldMapDTO,cfgApiFieldMapValueList,map,parentMap,apiFields,i);
        } else {
            //给非底层结构添加Map
            Object obj = resultMap.get(apiFields.get(i));
            if (ObjectUtils.isNull(obj)) {
                resultMap.put(apiFields.get(i),new LinkedHashMap<>());
            }
        }
    }

    /**
     * @description: 获取上一级Map对象
     * @author Will
     * @date: 2023/1/12 12:03
     * @param map
     * @param apiFields
     * @param i
     * @return Map<Object>
     */
    private Map<String,Object> getParentMap (Map<String,Object> map,List<String> apiFields,int i) {
        Map<String ,Object> resultMap = map;
        for (int j = 0; j < apiFields.size() ; j++ ) {
            //当传入i和j相等时返回map
            if (j == i) {
                return resultMap;
            } else {
                resultMap = (LinkedHashMap) resultMap.get(apiFields.get(j));
            }
        }
        return resultMap;
    }

    /**
     * @description: 给最底层字段赋值
     * @author Will
     * @date: 2023/1/12 12:03
     * @param cfgApiFieldMapDTO
     * @param cfgApiFieldMapValueList
     * @param map
     * @param resultMap
     * @param apiFields
     * @param i
     */
    private void putValueResultMap (CfgApiFieldMapDTO cfgApiFieldMapDTO,List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList,Map<String,Object> map,Map<String,Object> resultMap,List<String> apiFields,int i) {

        if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            resultMap.put(apiFields.get(i),map.get(apiFields.get(i)));
        } else {
            if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                throw new ServiceException(ApiError.ERROR_97025);
            }
            //根据值映射转换
            String apiValue = cfgApiFieldMapValueList.stream()
                    .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(map.get(apiFields.get(i))))
                    .map(CfgApiFieldMapValueEntity::getApiValue)
                    .findFirst()
                    .orElse(null);
            if (StringUtils.isBlank(apiValue)) {
                throw new ServiceException(ApiError.ERROR_97025);
            }
            resultMap.put(apiFields.get(i),apiValue);
        }
    }

}
