package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
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
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeSupplierConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeSupplierConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-02 9:43
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeSupplierConsumerServiceImpl implements KingdeeSupplierConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.SUPPLIER.getCode();
        //操作项
        String operate = (String) map.get("operate");
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_SUPPLIER.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }

        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils,platformEntity, map,type,json);
        }

        /**
         * 反审核
         */
        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            operateDisapprove(apiUtils,platformEntity, map,type);
        }

        /**
         * 禁用/反禁用
         */
        if (SyncOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            operateEnable(apiUtils,platformEntity, map,type,json);
        }

        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity,map,type,operate);
        }

    }

    /**
     * @description: 审核
     * @author Will
     * @date: 2023/9/26 9:42
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        Boolean erpForbidStatus = ObjectUtil.isNotEmpty(map.get("disabled")) ? (Boolean) map.get("disabled") : Boolean.FALSE;
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {

            //更新数据
            Boolean saveOrUpdateResult = kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
            if (saveOrUpdateResult && erpForbidStatus) {
                //启用、禁用
                excuteOperation(apiUtils, platformEntity, map, type);
            }
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        Boolean flag = Boolean.FALSE;
        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            setQueryJSONObject(id, apiUtils, json);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
            //启用、禁用
            excuteOperation(apiUtils, platformEntity, map, type);
        }

    }

    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/9/26 9:40
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateDisapprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //反审核
        kingdeeCommonService.handleUnAudit(platformEntity, map, apiUtils, type);
        return;
    }

    /**
     * @description: 启用禁用
     * @author Will
     * @date: 2023/9/26 9:37
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param json
     */
    public void operateEnable(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {

        //业务编码
        String code = (String) map.get("code");
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        Boolean erpForbidStatus = ObjectUtil.isNotEmpty(map.get("disabled")) ? (Boolean) map.get("disabled") : Boolean.FALSE;
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {

            //更新数据
            Boolean saveOrUpdateResult = kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
            if (saveOrUpdateResult && erpForbidStatus) {
                //启用、禁用
                excuteOperation(apiUtils, platformEntity, map, type);
            }
            return;
        }
        //查找到数据后，判断其审核状态
        String forbidStatus = String.valueOf(model.get("ForbidStatus"));
        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
        //操作项
        String operate = (String) map.get("operate");
        if (SyncOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            // 判断禁用状态是否与金蝶系统一致 A启用 B禁用
            if (erpForbidStatus.equals(kingdeeForbidStatus)) {
                log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), operate, operate);
                return;
            }
            //启用、禁用
            kingdeeCommonService.excuteOperation(apiUtils, map, code, operate);
            return;
        } else if (kingdeeForbidStatus && erpForbidStatus) {
            // 判断禁用状态是否与金蝶系统一致
            log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}]，跳过{}操作", forbidStatus, map.get("disabled"), operate);
            return;
        }
    }


    /**
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 9:48
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,String operate) {
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,type,operate);
        return;

    }


    /**
     * 给修改json对象赋值ID
     */
    public void setQueryJSONObject(String id, KingdeeApiUtils apiUtils, JSONObject json) {
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FSupplierId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FFinanceInfo_FEntryID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DETAIL_ID);
        }
        //主单据id
        KingdeeUtils.makeFieldJson(json, "FSupplierId", ".", id);
        //比较
        for (Map<String, Object> queryMap : queryList) {
            //财务信息
            JSONObject finance = (JSONObject) json.get("FFinanceInfo");
            finance.set("FEntryId", queryMap.get("FFinanceInfo_FEntryID"));

            //商务信息
            JSONObject business = (JSONObject) json.get("FBusinessInfo");
            business.set("FEntryId", queryMap.get("FBusinessInfo_FEntryID"));
        }

    }

    /**
     * 启用、禁用
     */
    public void excuteOperation(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        //仓库状态 true禁用,false启用
        Object disabled = map.get("disabled");
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        if (ObjectUtils.isEmpty(disabled)) {
            return;
        }
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FSupplierId = '%s'", syncKingdeeId));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FForbiderId";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 1);
        if (CollectionUtils.isEmpty(queryList)) {
            return;
        }
        Map<String, Object> queryMap = queryList.get(0);
        //禁用人
        String disablerId = (String) queryMap.get("FForbiderId");

        String code = (String) map.get("code");
        String operate = null;
        //启用
        if (!(Boolean) disabled && !StringUtils.equals("0", disablerId)) {
            operate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if ((Boolean) disabled && StringUtils.equals("0", disablerId)) {
            operate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        if (StringUtils.isNotBlank(operate)) {
            kingdeeCommonService.excuteOperation(apiUtils, map, code, operate);
        }
    }
}
