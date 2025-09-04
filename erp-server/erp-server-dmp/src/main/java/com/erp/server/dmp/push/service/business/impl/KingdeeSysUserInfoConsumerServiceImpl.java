package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeSysUserInfoConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeSysUserInfoConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 18:27
 * @Created by yl
 */
@Slf4j
@Service
public class KingdeeSysUserInfoConsumerServiceImpl implements KingdeeSysUserInfoConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    /**
     * 同步员工信息
     *
     * @param map
     * @return void
     * @author yl
     * @date 2023-08-01 12:20
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @KingdeeApi(KingdeePushModuleEnum.BD_EMPINFO)
    public void executeSysUserConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.SYS_USER_INFO.getCode();
        //操作项
        String operate = (String) map.get("operate");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = KingdeeApiThreadLocal.get();
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
         * 禁用/反禁用
         */
        if (SyncOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            operateEnable(apiUtils,platformEntity, map,type,json);
        }

        /**
         * 删除
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity, map,type);
        }

    }

    /**
     * 启用禁用
     */
    public void operateEnable(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {
        //操作项
        String operate = (String) map.get("operate");

        Object userState = map.get("userState");
        //0 是禁用
        Boolean erpForbidStatus = String.valueOf(MathUtil.ZERO).equals(String.valueOf(userState));
        String enableOperate = null;
        //启用
        if (String.valueOf(MathUtil.ONE).equals(String.valueOf(userState))) {
            enableOperate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if (String.valueOf(MathUtil.ZERO).equals(String.valueOf(userState))) {
            enableOperate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {

            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
            String forbidStatus = model.getStr("ForbidStatus", "");
            // A启用 B禁用
            Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
            //如果是一致就不处理
            if (erpForbidStatus.equals(kingdeeForbidStatus)) {
                log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), enableOperate, enableOperate);
                return;
            }
            //启用、禁用
            excuteOperation(apiUtils, map, enableOperate);
            return;
        }

        //禁用日期（用于判断是否禁用）
        String forbidStatus = model.getStr("ForbidStatus", "");
        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
        if (SyncOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            //如果是一致就不处理
            if (erpForbidStatus.equals(kingdeeForbidStatus)) {
                log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), operate, operate);
                return;
            }
            //启用、禁用
            excuteOperation(apiUtils, map, enableOperate);
            return;
        } else if (kingdeeForbidStatus && erpForbidStatus) {
            // 判断禁用状态是否与金蝶系统一致
            log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}]，跳过{}操作", forbidStatus, map.get("disabled"), operate);
            return;
        }
    }

    /**
     * 审核
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {
        //操作项
        String operate = (String) map.get("operate");

        Object userState = map.get("userState");
        //0 是禁用
        Boolean erpForbidStatus = String.valueOf(MathUtil.ZERO).equals(String.valueOf(userState));
        String enableOperate = null;
        //启用
        if (String.valueOf(MathUtil.ONE).equals(String.valueOf(userState))) {
            enableOperate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if (String.valueOf(MathUtil.ZERO).equals(String.valueOf(userState))) {
            enableOperate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }

        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {

            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
            String forbidStatus = model.getStr("ForbidStatus", "");
            // A启用 B禁用
            Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
            //如果是一致就不处理
            if (erpForbidStatus.equals(kingdeeForbidStatus)) {
                log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), enableOperate, enableOperate);
                return;
            }
            //启用、禁用
            excuteOperation(apiUtils, map, enableOperate);
            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = model.getStr("DocumentStatus", "");
        //禁用日期（用于判断是否禁用）
        String forbidStatus = model.getStr("ForbidStatus", "");

        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
        String id = String.valueOf(model.get("Id"));
        Boolean flag = Boolean.FALSE;

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //禁用的不能烦审核
            if (!kingdeeForbidStatus) {
                flag = kingdeeCommonService.unAudit(apiUtils, id);
            }else{
                log.warn("金蝶状态为禁用状态数据不需要修改");
                return;
            }

        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //主单据id
            KingdeeUtils.makeFieldJson(json, "FId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (erpForbidStatus.equals(kingdeeForbidStatus)) {
                log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), operate, operate);
                return;
            } else {
                //启用、禁用
                excuteOperation(apiUtils, map, enableOperate);
            }
        }
    }

    /**
     * 删除
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //操作项
        String operate = (String) map.get("operate");
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.SYS_USER_INFO.getCode(),operate);
        return;
    }

    /**
     * 启用或者禁用 组装数据
     * @param apiUtils
     * @param map
     * @return void
     * @author yl
     * @date 2023-08-01 12:25
     */
    public void excuteOperation(KingdeeApiUtils apiUtils, Map<String, Object> map, String operate) {
        if (StringUtils.isBlank(operate)) {
            return;
        }
        //用户状态 1：正常 0：禁用
        Object userState = map.get("userState");
        if (ObjectUtils.isEmpty(userState)) {
            return;
        }
        String code = String.valueOf(map.getOrDefault("code", ""));
        if (StringUtils.isNotBlank(operate)) {
            kingdeeCommonService.excuteOperation(apiUtils, map, code, operate);
        }
    }






}
