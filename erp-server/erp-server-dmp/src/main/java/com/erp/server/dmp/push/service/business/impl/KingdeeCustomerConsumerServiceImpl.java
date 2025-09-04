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
import com.erp.sdk.third.kingdee.utils.KingdeeApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeCustomerConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeCustomerConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 19:13
 * @Created by yl
 */
@Slf4j
@Service
public class KingdeeCustomerConsumerServiceImpl implements KingdeeCustomerConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi(KingdeePushModuleEnum.BD_CUSTOMER)
    public void executeCustomerContactConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_INFO.getCode();

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = KingdeeApiThreadLocal.get();

        //操作项
        String operate = (String) map.get("operate");

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
            operateDisapprove(apiUtils,platformEntity, map,type,json);
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
            operateDelete(apiUtils,platformEntity, map);
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
        String operate = null;
        //启用
        if (!(Boolean) disabled) {
            operate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if ((Boolean) disabled) {
            operate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        if (StringUtils.isNotBlank(operate)) {
            kingdeeCommonService.excuteOperation(apiUtils, map, code, operate);
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
     * @param json
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {

        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        Boolean erpForbidStatus = ObjectUtil.isNotEmpty(map.get("disabled")) ? (Boolean) map.get("disabled") : Boolean.FALSE;
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
            map.put("syncKingdeeId", String.valueOf(model.get("Id")));
            json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);
            param = new KingdeeParamDTO.SaveParamDTO(json);
        } catch (Exception e) {

            //更新数据
            Boolean saveOrUpdateResult = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (saveOrUpdateResult && erpForbidStatus) {
                //启用、禁用
                excuteOperation(apiUtils, map);
            }

            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        String forbidStatus = String.valueOf(model.get("ForbidStatus"));
        Boolean flag = Boolean.FALSE;
        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;

        boolean allowUnApproveStatus = KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus);

        //审核中或已审核则要先反审
        if (allowUnApproveStatus) {
            // 判断禁用状态已禁用数据 先启用再反审核
            if (kingdeeForbidStatus) {
                // 同步ERP禁用状态
                excuteOperation(apiUtils, map);
            }
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //主单据id
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            if (!erpForbidStatus.equals(kingdeeForbidStatus)) {
                //启用、禁用
                excuteOperation(apiUtils,map);
            }
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
     * @param json
     */
    public void operateDisapprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {
        //禁用状态
        Boolean erpForbidStatus = ObjectUtil.isNotEmpty(map.get("disabled")) ? (Boolean) map.get("disabled") : Boolean.FALSE;
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
            map.put("syncKingdeeId", String.valueOf(model.get("Id")));
        } catch (Exception e) {
            //更新数据
            Boolean saveOrUpdateResult = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (saveOrUpdateResult && erpForbidStatus) {
                //启用、禁用
                excuteOperation(apiUtils, map);
            }
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        String forbidStatus = String.valueOf(model.get("ForbidStatus"));
        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;

        boolean allowUnApproveStatus = KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus);
        // 判断状态是否为审核中或已审核
        if (!allowUnApproveStatus) {
            //反审核
            log.warn("单据状态为[{}], 无法反审核，跳过反审核操作", KingdeeDocStatusEnum.getByCode(documentStatus));
            return;
        }
        // 判断禁用状态已禁用数据 先启用再反审核
        if (kingdeeForbidStatus) {
            // 同步ERP禁用状态
            excuteOperation(apiUtils, map);
        }
        //反审核
        kingdeeCommonService.unAudit(apiUtils, id);
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
        //禁用状态
        Boolean erpForbidStatus = ObjectUtil.isNotEmpty(map.get("disabled")) ? (Boolean) map.get("disabled") : Boolean.FALSE;
        //操作项
        String operate = (String) map.get("operate");
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
            map.put("syncKingdeeId", String.valueOf(model.get("Id")));
        } catch (Exception e) {
            //更新数据
            Boolean saveOrUpdateResult = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (saveOrUpdateResult && erpForbidStatus) {
                //启用、禁用
                excuteOperation(apiUtils, map);
            }
            return;
        }
        //查找到数据后，判断其审核状态
        String forbidStatus = String.valueOf(model.get("ForbidStatus"));
        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
        if (kingdeeForbidStatus && erpForbidStatus) {
            // 判断禁用状态是否与金蝶系统一致
            log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}]，跳过{}操作", forbidStatus, map.get("disabled"), operate);
            return;
        }
        // 判断禁用状态是否与金蝶系统一致 A启用 B禁用
        if (erpForbidStatus.equals(kingdeeForbidStatus)) {
            log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), operate, operate);
            return;
        }
        //启用、禁用
        excuteOperation(apiUtils,map);
        return;
    }


    /**
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 9:48
     * @param apiUtils
     * @param platformEntity
     * @param map
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map) {
        //操作项
        String operate = (String) map.get("operate");
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.CUSTOMER_INFO.getCode(),operate);
        return;
    }

}
