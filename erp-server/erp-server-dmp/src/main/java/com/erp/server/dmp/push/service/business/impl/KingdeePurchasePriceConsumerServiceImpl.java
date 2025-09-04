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
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeePurchasePriceConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeePurchasePriceConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 21:08
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeePurchasePriceConsumerServiceImpl implements KingdeePurchasePriceConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi(KingdeePushModuleEnum.PUR_PRICECATEGORY)
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_PRICE.getCode();

        log.info("采购价目表开始推送金蝶 map = {}", JSONUtil.toJsonStr(map));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = KingdeeApiThreadLocal.get();

        //操作项，分录禁用
        String operate = (String) map.get("operate");

        /**
         * 分录启用/禁用
         */
        if (SyncOperateEnum.OPERATE_SUB_EFFECTIVE.getCode().equals(operate) || SyncOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode().equals(operate)) {
            excuteOperation(apiUtils, map, operate);
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
     * 删除
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,String operate) {
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.PURCHASE_PRICE.getCode(),operate);
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
    public void operateApprove (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //操作项，分录禁用
        String operate = (String) map.get("operate");

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

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
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {

            log.error("采购价目表查看失败 map = {}", JSONUtil.toJsonStr(map));
            //更新数据
            Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (isAdd) {
                //禁用启用
                JSONArray jsonArray = excuteOperation(apiUtils, map, operate);
                //更新明细id
                updateKingdeeDetailId(jsonArray);
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
            setQueryJSONObject(id, apiUtils, platformEntity, map, type, json);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (isAdd) {
                //禁用启用
                JSONArray jsonArray = excuteOperation(apiUtils, map, operate);
                //更新明细id
                updateKingdeeDetailId(jsonArray);
            }
        }

    }



        /**
         * 给修改json对象赋值ID
         */
    public void setQueryJSONObject(String id, KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, JSONObject json) {
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FPriceListEntry_FEntryID,FMaterialId.FNumber,FFROMQTY,FToQty";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DETAIL_ID);
        }
        //主单据id
        KingdeeUtils.makeFieldJson(json, "FId", ".", id);
        //比较
        for (Map<String, Object> queryMap : queryList) {
            JSONArray obj = (JSONArray) json.get("FPriceListEntry");
            JSONArray removeObj = new JSONArray();
            JSONArray addObj = new JSONArray();
            for (Object o : obj) {
                JSONObject jsonObject = JSONUtil.parseObj(o);
                JSONObject newJson = new JSONObject(new LinkedHashMap<>());
                Object o1 = queryMap.get("FMaterialId.FNumber");
                JSONObject o2 = (JSONObject) jsonObject.get("FMaterialId");
                Object fNumber = o2.get("FNumber");
                BigDecimal minQty = MathUtil.valueOf(jsonObject.get("FFROMQTY"));
                BigDecimal maxQty = MathUtil.valueOf(jsonObject.get("FToQty"));
                BigDecimal fMinQty = MathUtil.valueOf(queryMap.get("FFROMQTY"));
                BigDecimal fMaxQty = MathUtil.valueOf(queryMap.get("FToQty"));
                if (o1.equals(fNumber) && MathUtil.compareTo(minQty, fMinQty) == MathUtil.ZERO && MathUtil.compareTo(maxQty, fMaxQty) == MathUtil.ZERO) {
                    newJson.set("FEntryId", queryMap.get("FPriceListEntry_FEntryID"));
                }
                newJson.putAll(jsonObject);
                removeObj.set(o);
                addObj.set(newJson);
            }
            obj.removeAll(removeObj);
            obj.addAll(addObj);
        }

    }

    /**
     * 启用、禁用
     */
    public JSONArray excuteOperation(KingdeeApiUtils apiUtils, Map<String, Object> map, String operate) {

        JSONArray list = JSONUtil.parseArray(map.get("list"));
        String id = (String) map.get("syncKingdeeId");

        List<String> disabledList = new ArrayList<>();
        List<String> unDisabledList = new ArrayList<>();
        JSONArray removeObj = new JSONArray();
        JSONArray addObj = new JSONArray();
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            JSONObject newJson = new JSONObject(new LinkedHashMap<>());
            //同步数据时禁用,需要考虑既有禁用又有启用的情况
            Boolean disabled = (Boolean) jsonObject.get("disabled");
            if (ObjectUtils.isNotEmpty(disabled)) {
                //禁用
                if (disabled) {
                    operate = SyncOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode();
                } else {
                    operate = SyncOperateEnum.OPERATE_SUB_EFFECTIVE.getCode();
                }
            }
            if (StringUtils.isBlank(id)) {
                id = (String) jsonObject.get("syncKingdeeId");
            }

            String skuNo = (String) jsonObject.get("skuNo");
            BigDecimal minQty = MathUtil.valueOf(jsonObject.get("minQty"));
            BigDecimal maxQty = MathUtil.valueOf(jsonObject.get("maxQty"));

            LinkedList<String> queryFilters = new LinkedList<>();
            queryFilters.add(String.format("FId = '%s'", id));
            String filterStr = String.join(" and ", queryFilters);
            //查询子单据id
            String fieldKeys = "FPriceListEntry_FEntryID,FMaterialId.FNumber,FFROMQTY,FToQty,FDisablerId";
            List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);

            log.info("价目明细数据 queryList = {}", JSONUtil.toJsonStr(queryList));
            //比较
            for (Map<String, Object> queryMap : queryList) {
                String number = (String) queryMap.get("FMaterialId.FNumber");
                String detailId = (String) queryMap.get("FPriceListEntry_FEntryID");
                BigDecimal fMinQty = MathUtil.valueOf(queryMap.get("FFROMQTY"));
                BigDecimal fMaxQty = MathUtil.valueOf(queryMap.get("FToQty"));
                String disablerId = (String) queryMap.get("FDisablerId");
                if (StringUtils.equals(skuNo, number) && MathUtil.compareTo(minQty, fMinQty) == MathUtil.ZERO && MathUtil.compareTo(maxQty, fMaxQty) == MathUtil.ZERO) {
                    //禁用
                    if (SyncOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode().equals(operate) && StringUtils.equals("0", disablerId)) {
                        disabledList.add(detailId);
                    }
                    //启用
                    if (SyncOperateEnum.OPERATE_SUB_EFFECTIVE.getCode().equals(operate) && !StringUtils.equals("0", disablerId)) {
                        unDisabledList.add(detailId);
                    }
                    //金蝶明细id赋值
                    newJson.set("kingdeeDetailId", detailId);
                }
            }
            newJson.putAll(jsonObject);
            removeObj.set(obj);
            addObj.set(newJson);
        }
        list.removeAll(removeObj);
        list.addAll(addObj);
        //禁用
        if (CollectionUtils.isNotEmpty(disabledList)) {
            log.info("禁用价目数据 ids = {}", JSONUtil.toJsonStr(disabledList));
            excuteOperation(apiUtils, disabledList, id, SyncOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getKingdeeParam());
        }
        //启用
        if (CollectionUtils.isNotEmpty(unDisabledList)) {
            log.info("启用价目数据 ids = {}", JSONUtil.toJsonStr(unDisabledList));
            excuteOperation(apiUtils, unDisabledList, id, SyncOperateEnum.OPERATE_SUB_EFFECTIVE.getKingdeeParam());
        }
        return list;
    }

    /**
     * @param apiUtils
     * @param list
     * @param id
     * @param operate
     * @description: 启用或禁用
     * @author Will
     * @date: 2023/4/28 11:36
     */
    public void excuteOperation( KingdeeApiUtils apiUtils, List<String> list, String id, String operate) {
        JSONObject viewMap = new JSONObject(new LinkedHashMap<>());
        JSONObject newObj = new JSONObject();
        JSONArray pkEntryIds = new JSONArray();
        newObj.set("id", id);
        newObj.set("EntryIds", String.join(",", list));
        pkEntryIds.put(newObj);
        viewMap.set("PkEntryIds", pkEntryIds);
        //启用禁用
        apiUtils.excuteOperation(operate, JSONUtil.toJsonStr(viewMap));
        log.info("启用、禁用价目数据成功 jsonStr = {}", JSONUtil.toJsonStr(viewMap));
    }

    /**
     * 更新明细id
     */
    public void updateKingdeeDetailId(JSONArray jsonArray) {
        //更新业务单据状态
        Map<String, Object> params = new HashMap<>(MathUtil.THREE);
        params.put("code", ApiModuleTypeEnum.PURCHASE_PRICE.getCode().toString());
        params.put("details", jsonArray);
        scmTaskFeign.updateBusinessSyncKingdeeStatus(params);
    }
}
