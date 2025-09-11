package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
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
import com.common.core.utils.MathUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.K3CloudApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeePoReceiveConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KingdeePoReceiveConsumerServiceImpl implements KingdeePoReceiveConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private WmsTaskFeign scmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.PO_RECEIVE.getCode();

        //业务id
        String  businessId = String.valueOf(map.get("id"));

        //业务编码
        String code = (String) map.get("code");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_RECEIVEBILL.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }

        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            //更新数据
            saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            return;
        }
        //执行操作
        operate (platformEntity,map,apiUtils,model,json,type);
    }

    /**
     * 采购订单操作
     */
    public void operate (PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject model, JSONObject json, Integer type) {
        //查找到数据后的审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        //操作项
        String operate = (String) map.get("operate");
        if (SyncOperateEnum.OPERATE_INVALID.getCode().equals(operate)) {
            operateInvalid(apiUtils, map);
        }
        //反审核
        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            //审核中或已审核则要先反审
            if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
                //反审核
                operateDisapprove(apiUtils, map);
            }
        }
        //审核
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils, platformEntity, map, model, json, type);
        }
        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity,map,operate);
        }
    }

    public void operateInvalid(KingdeeApiUtils apiUtils,Map<String, Object> map) {
        //业务编码
        String code = (String) map.get("code");
        //操作项
        String operate = (String) map.get("operate");
        //作废
        kingdeeCommonService.excuteOperation(apiUtils,map,code,operate);
        return;
    }

    public void operateDisapprove(KingdeeApiUtils apiUtils,Map<String, Object> map) {
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        if (StringUtils.isBlank(syncKingdeeId)) {
            return;
        }
        //反审核
        kingdeeCommonService.unAudit(apiUtils, syncKingdeeId);
        return;
    }

    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map, JSONObject model, JSONObject json, Integer type) {
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id")) ;
        Boolean flag = Boolean.FALSE;

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            KingdeeUtils.makeFieldJson(json,"FID",".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        }
    }

    /**
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 11:49
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param operate
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,String operate) {
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.PO_RECEIVE.getCode(),operate);
        return;
    }

    /**
     * 新增
     */
    public Boolean saveOrUpdate (PlatformEntity platformEntity,Map<String, Object> map ,KingdeeApiUtils apiUtils, JSONObject json,KingdeeParamDTO.SaveParamDTO param, Integer type) {

        Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        if (isAdd) {
            //给明细id赋值
            JSONArray jsonArray = setDetailIdForJSONObject(apiUtils, map);
            //更新明细id
            updateKingdeeDetailId(jsonArray);
        }
        return  isAdd;
    }

    /**
     * 给明细id赋值
     * @Author Luo_WG
     * @Date 2023/7/12 10:18
     * @param apiUtils
     * @param map
     * @return cn.hutool.json.JSONArray
     **/
    public JSONArray setDetailIdForJSONObject (KingdeeApiUtils apiUtils,Map<String, Object> map) {

        JSONArray list = JSONUtil.parseArray(map.get("list"));
        String id = (String)map.get("syncKingdeeId");

        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FDetailEntity_FEntryID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DETAIL_ID);
        }
        JSONArray removeObj = new JSONArray();
        JSONArray addObj = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            JSONObject newJson = new JSONObject(new LinkedHashMap<>());
            if (list.size() >= queryList.size()) {
                //金蝶明细id赋值
                newJson.set("kingdeeDetailId",queryList.get(i).get("FDetailEntity_FEntryID"));
            }
            newJson.putAll(jsonObject);
            removeObj.set(obj);
            addObj.set(newJson);
        }
        list.removeAll(removeObj);
        list.addAll(addObj);
        return list;
    }


    /**
     * 更新明细id
     */
    public void updateKingdeeDetailId (JSONArray jsonArray) {
        //更新业务单据状态
        Map<String,Object> params = new HashMap<>(MathUtil.THREE);
        params.put("code",ApiModuleTypeEnum.PO_RECEIVE.getCode().toString());
        params.put("details",jsonArray);
        scmTaskFeign.updateBusinessSyncKingdeeStatus(params);
    }
}
