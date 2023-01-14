package com.erp.server.dmp.push.service.kingdee.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.MathUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.*;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.OperatorResult;
import com.kingdee.bos.webapi.entity.RepoError;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 18:03
 */
@Slf4j
@Service
public class KingdeeProductDetailServiceImpl implements KingdeeProductDetailService {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;

    @Resource
    private ApiPlmSyncLogService apiPlmSyncLogService;

    @Resource
    private ApiSyncTaskService apiSyncTaskService;

    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();

        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.taskName);
        SaveParam param = new SaveParam(resultMap);
        SaveResult saveResult = apiUtils.save(param);
        System.out.println(saveResult);
    }

    @Override
    @Transactional
    public void pushProductDetail(Map<String, Object> map) {
        //传入map数据不能为空
        if (ObjectUtils.isEmpty(map) || map.size() == 0) {
            throw new ServiceException(ApiError.Default);
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(platformEntity.getId());
        dto.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        if (CollectionUtils.isNotEmpty(mapList)) {
            log.info(ApiError.ERROR_97025.msg);
            //新增定时同步任务
            insertApiSyncTask(platformEntity, map);
            return;
        }
        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());

        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);

        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.taskName);

        Map<String, Object> resultMap = new LinkedHashMap<>();

        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mapList) {
            //第三方系统逗号分割多层结构
            String apiField = cfgApiFieldMapDTO.getApiField();
            List<String> apiFields = Arrays.stream(apiField.split("_")).collect(Collectors.toList());
            for (int i = 0; i < apiFields.size(); i++) {
                //给不同结构的外部字段赋值
                handleResultMap(cfgApiFieldMapDTO, cfgApiFieldMapValueList, map, resultMap, apiFields, i);
            }
        }
        //判断金蝶系统是否已存在该数据
        String skuNo = (String)resultMap.get("Model_FNumber");
        OperatorResult operatorResult = apiUtils.viewByNumber(skuNo);
        //如果未查询到数据则直接新增
        if (ObjectUtils.isEmpty(operatorResult.getResult())) {
            insert(platformEntity,map,resultMap,apiUtils);
        } else {

        }
    }

    /**
     * 新增数据到金蝶
     */
    private void insert(PlatformEntity platformEntity,Map<String, Object> map,Map<String,Object> resultMap,KingdeeApiUtils apiUtils) {
        //结果集转json字符串
        String jsonData = JSONObject.toJSONString(resultMap);
        //调用接口
        SaveParam param = new SaveParam(jsonData);
        SaveResult resultJson = apiUtils.save(param);
        boolean successfully = resultJson.isSuccessfully();
        if (successfully) {
            //新增日志信息
            ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
            apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
            apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
            apiPlmSyncLogDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
            apiPlmSyncLogDTO.setBusinessId(String.valueOf(map.get("id")));
            apiPlmSyncLogDTO.setStatus(ApiSendStatusEnum.SUCCESS.getCode());
            apiPlmSyncLogDTO.setMsg("发送成功");
            apiPlmSyncLogDTO.setRequestParamJson(jsonData);
            apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
            //发送成功后删除任务表数据
            Map<String, Object> removeMap = new HashMap<>();
            removeMap.put("apiPlatformId", platformEntity.getId());
            removeMap.put("moduleType", ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
            removeMap.put("businessId", String.valueOf(map.get("id")));
            apiPlmSyncLogService.removeByMap(removeMap);
        } else {
            ArrayList<RepoError> errors = resultJson.getResult().getResponseStatus().getErrors();
            //新增日志信息
            ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
            apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
            apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
            apiPlmSyncLogDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
            apiPlmSyncLogDTO.setBusinessId(String.valueOf(map.get("id")));
            apiPlmSyncLogDTO.setStatus(ApiSendStatusEnum.FAILURE.getCode());
            apiPlmSyncLogDTO.setMsg(JSONArray.toJSONString(errors));
            apiPlmSyncLogDTO.setRequestParamJson(jsonData);
            apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
            //新增定时同步任务
            insertApiSyncTask(platformEntity, map);
        }
    }

    /**
     * @description: 新增定时同步任务
     * @author Will
     * @date: 2023/1/12 16:41
     * @param platformEntity
     * @param map
     */
    private void insertApiSyncTask(PlatformEntity platformEntity,Map<String, Object> map) {
        //新增或更新定时任务数据重新发送
        ApiSyncTaskDTO apiSyncTaskDTO = new ApiSyncTaskDTO();
        apiSyncTaskDTO.setApiPlatformId(platformEntity.getId());
        apiSyncTaskDTO.setApiPlatform(platformEntity.getName());
        apiSyncTaskDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        apiSyncTaskDTO.setBusinessId(String.valueOf(map.get("id")));
        apiSyncTaskDTO.setRetryCount(MathUtil.ZERO);
        //传入参数转json字符串
        String jsonParam = JSONObject.toJSONString(map);
        apiSyncTaskDTO.setRequestParamJson(jsonParam);
        ApiSyncTaskEntity apiSyncTask = apiSyncTaskService.getByApiSyncTask(apiSyncTaskDTO);
        if (ObjectUtils.isEmpty(apiSyncTask)) {
            apiSyncTaskService.insert(apiSyncTaskDTO);
        } else {
            apiSyncTaskDTO.setId(apiSyncTask.getId());
            apiSyncTaskDTO.setRetryCount(MathUtil.add(apiSyncTask.getRetryCount(),1));
            apiSyncTaskService.update(apiSyncTaskDTO);
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
