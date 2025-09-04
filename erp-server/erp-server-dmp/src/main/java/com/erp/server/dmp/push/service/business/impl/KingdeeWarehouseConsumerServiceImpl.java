package com.erp.server.dmp.push.service.business.impl;

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
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeWarehouseConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeWarehouseConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-02 9:54
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeWarehouseConsumerServiceImpl implements KingdeeWarehouseConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi(KingdeePushModuleEnum.BD_STOCK)
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.WAREHOUSE_INFO.getCode();

        log.info("仓库信息开始推送金蝶 map = {}", JSONUtil.toJsonStr(map));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = KingdeeApiThreadLocal.get();

        //操作项
        String operate = (String) map.get("operate");

        /**
         * 反审核
         */
        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            operateDisapprove(apiUtils, platformEntity, map, type);
        }

        /**
         * 启用、禁用
         */
        if (SyncOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            excuteOperation(apiUtils, map);
            return;
        }

        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils, platformEntity, map, type);
        }

        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity,map,operate);
        }

    }


    /**
     * 启用、禁用
     */
    public void excuteOperation(KingdeeApiUtils apiUtils, Map<String, Object> map) {
        //仓库状态 true禁用,false启用
        Object disabled = map.get("disabled");
        if (ObjectUtils.isEmpty(disabled)) {
            return;
        }
        String code = (String) map.get("code");

        String syncKingdeeId = (String) map.get("syncKingdeeId");
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FStockId = '%s'", syncKingdeeId));
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

    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @description: 反审核
     * @author Will
     * @date: 2023/5/24 17:57
     */
    public void operateDisapprove(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        //反审核
        kingdeeCommonService.handleUnAudit(platformEntity, map, apiUtils, type);
        return;
    }

    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @description: 审核
     * @author Will
     * @date: 2023/5/24 18:10
     */
    public void operateApprove(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {

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
            //更新数据
            Boolean isSuccess = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (isSuccess) {
                //启用、禁用
                excuteOperation(apiUtils, map);
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
            //主单据id
            KingdeeUtils.makeFieldJson(json, "FStockId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            Boolean isSuccess = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (isSuccess) {
                //启用、禁用
                excuteOperation(apiUtils, map);
            }
        }
    }

    /**
     * 删除
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,String operate) {
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.WAREHOUSE_INFO.getCode(),operate);
        return;
    }
}
