package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.sdk.oms.amz.spapi.client.JSON;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeAssistantDataDetailConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeAssistantDataDetailConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 18:36
 * @Created by yl
 */
@Slf4j
@Service
public class KingdeeAssistantDataDetailConsumerServiceImpl implements KingdeeAssistantDataDetailConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi
    public void executeAssistantDataDetailConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = (Integer) map.get("moduleType");
        //操作项
        String operate = (String) map.get("operate");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());

        String syncKingdeeId = (String) map.get("syncKingdeeId");
        String code = (String) map.get("code");
        if(StringUtils.isBlank(syncKingdeeId) && StringUtils.isBlank(code)) {
        	throw new ServiceException("金蝶id和code不能同时为空：" + JSON.toJsonStr(map));
        }
        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            //map中设置父级id
            setPid(apiUtils, map , platformEntity);
            operateApprove(apiUtils,platformEntity, map,type);
        }

        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity, map,operate);
        }
    }

    /**
     * 设置父级id
     */
    public void setPid(KingdeeApiUtils apiUtils, Map<String, Object> map , PlatformEntity platformEntity) {
        //判断是否存在上级
        Boolean isExistParent = (Boolean) map.get("isExistParent");
        String pid = map.getOrDefault("pid", "").toString();
        if (isExistParent && StringUtils.isBlank(pid)) {
        	//根据上级编码查询上级id
            String parentCode = (String) map.get("parentCode");
            map.put("pid", this.getKingdeeId(apiUtils, parentCode, map, platformEntity));
        }

    }
    /**
     * @description:
     * @author Will
     * @date: 2023/9/26 10:38
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), ApiModuleTypeEnum.ASSISTANT_DATA.getCode());

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }
        //判断金蝶系统是否已存在该数据
        JSONObject model = null;
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        if(StringUtils.isNotBlank(syncKingdeeId)) {
        	try {
                model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
            } catch (Exception e) {
                //更新数据
                kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
                return;
            }
        }else {
        	String code = (String) map.get("code");
            JSONObject fId = json.getJSONObject("FId");
        	if(fId != null) {
        		String fNumber = fId.getStr("FNumber");
        		model = this.queryByTypeAndCode(apiUtils, fNumber, code);
        	}
        	if(model == null) {
        		//更新数据
                kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
                return;
        	}
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = (String) model.get("Id");
        Boolean flag = Boolean.FALSE;

        //操作项
        String operate = (String) map.get("operate");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //删除
            if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
                String code = (String) map.get("code");
                kingdeeCommonService.delete(apiUtils, platformEntity, map, type, code);
                return;
            }
            //主单据id
            KingdeeUtils.makeFieldJson(json, "FEntryId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
        }
    }

    /**
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 10:44
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param operate
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,String operate) {
    	String syncKingdeeId = (String) map.get("syncKingdeeId");
        if(StringUtils.isBlank(syncKingdeeId)) {
        	String code = (String) map.get("code");
        	map.put("syncKingdeeId", this.getKingdeeId(apiUtils, code, map, platformEntity));
        }
    	//删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.ASSISTANT_DATA.getCode(),operate);
    }
    
    private String getKingdeeId(KingdeeApiUtils apiUtils , String code , Map<String, Object> map , PlatformEntity platformEntity) {
    	String id = "";
    	//根据上级编码查询上级id
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), ApiModuleTypeEnum.ASSISTANT_DATA.getCode());
    	JSONObject fId = json.getJSONObject("FId");
    	if(fId != null) {
    		String fNumber = fId.getStr("FNumber");
    		JSONObject model = this.queryByTypeAndCode(apiUtils, fNumber, code);
    		if(model != null) {
    			id = (String) model.get("Id");
    		}
    	}
        if(StringUtils.isBlank(id)) {
        	throw new ServiceException(ApiError.ERROR_NOT_EXIST_PARENT_ASSISTANT_DATA);
        }
        return id;
    }
    
    private JSONObject queryByTypeAndCode(KingdeeApiUtils apiUtils , String fNumber, String code) {
    	JSONObject model = null; 
    	LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId.FNumber = '%s' and FNumber = '%s'", fNumber , code
        		));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FEntryId,FDocumentStatus";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 1);
        if(CollUtil.isNotEmpty(queryList)) {
        	if(queryList.size() != 1) {
        		throw new ServiceException("类型【" + fNumber + "】代码【" + code + "】辅助资料存在多个：" + JSON.toJsonStr(queryList));
        	}else {
        		model = new JSONObject();
        		String id = "";
        		Object fEntryId = queryList.get(0).get("FEntryId");
        		if(fEntryId != null) {
        			id = fEntryId.toString();
        		}
        		String documentStatus = "";
        		Object fDocumentStatus = queryList.get(0).get("FDocumentStatus");
        		if(fDocumentStatus != null) {
        			documentStatus = fDocumentStatus.toString();
        		}
        		model.set("Id", id);
        		model.set("DocumentStatus", documentStatus);
        	}
        }
        return model;
    }
}
