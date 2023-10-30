package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.core.utils.MathUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.push.service.business.KingdeeStockInConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeStockInConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-02 9:30
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeStockInConsumerServiceImpl implements KingdeeStockInConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public void executeConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode();

        //业务编码
        String code = (String) map.get("code");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_INSTOCK.getCode());

        //操作项
        String operate = (String) map.get("operate");

        /**
         * 作废
         */
        if (SyncOperateEnum.OPERATE_INVALID.getCode().equals(operate)) {
            operateInvalid(apiUtils,platformEntity,map,type,code,operate);
        }
        /**
         * 反审核
         */
        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            operateDisapprove(apiUtils,platformEntity, map,type);
        }
        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils,platformEntity, map,type);
        }
        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity,map,operate);
        }

    }

    /**
     * 作废
     */
    public void operateInvalid(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type, String code,String operate) {
        //判断金蝶系统是否已存在该数据
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            //更新业务表中的金蝶id
            kingdeeCommonService.updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), SyncStatusEnum.NO_NEED_SYNC.getCode(), "");
            return;
        }
        String documentStatus = (String)model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id")) ;
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //反审核
            Boolean unAudit = kingdeeCommonService.unAudit(apiUtils, id);
            if (!unAudit) {
                return;
            }
        }
        //作废
        kingdeeCommonService.excuteOperation(apiUtils, map, code, operate);
        return;
    }

    /**
     * 反审核
     */
    public void operateDisapprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //反审核
        kingdeeCommonService.handleUnAudit(platformEntity, map, apiUtils, type);
        return;
    }

    /**
     * 审核
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (isAdd) {
                //给明细id赋值
                JSONArray jsonArray = setDetailIdForJSONObject(apiUtils,platformEntity, map, type);
                //更新明细id
                updateKingdeeDetailId(jsonArray);
            }
            return;
        }
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
            KingdeeUtils.makeFieldJson(json,"FId",".", id);
            //更新数据不能传入库组织
            json.remove("FStockOrgId.FNumber");
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            if (isAdd) {
                //给明细id赋值
                JSONArray jsonArray = setDetailIdForJSONObject(apiUtils,platformEntity, map, type);
                //更新明细id
                updateKingdeeDetailId(jsonArray);
            }
        }
    }

    /**
     * 删除
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,String operate) {
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),operate);
        return;
    }

    /**
     * 给明细id赋值
     * @Author Luo_WG
     * @Date 2023/7/12 10:18
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @return cn.hutool.json.JSONArray
     **/
    public JSONArray setDetailIdForJSONObject (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        JSONArray list = JSONUtil.parseArray(map.get("list"));
        String id = (String)map.get("syncKingdeeId");

        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FInStockEntry_FEntryID";
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
                newJson.set("kingdeeDetailId",queryList.get(i).get("FInStockEntry_FEntryID"));
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
        params.put("code",ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode().toString());
        params.put("details",jsonArray);
        wmsTaskFeign.updateBusinessSyncKingdeeStatus(params);
    }
}
