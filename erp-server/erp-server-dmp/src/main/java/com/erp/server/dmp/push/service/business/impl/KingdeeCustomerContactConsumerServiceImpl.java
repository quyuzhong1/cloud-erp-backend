package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
import com.erp.server.dmp.push.service.business.KingdeeCustomerContactConsumerService;
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
 * @Classname KingdeeCustomerContactConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 19:20
 * @Created by yl
 */
@Slf4j
@Service
public class KingdeeCustomerContactConsumerServiceImpl implements KingdeeCustomerContactConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi(KingdeePushModuleEnum.BD_COMMONCONTACT)
    public void executeConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_CONTACT.getCode();
        //操作项
        String operate = (String) map.get("operate");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = KingdeeApiThreadLocal.get();

        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            operateDelete(apiUtils,platformEntity, map);
        } else {
            operateApprove(apiUtils,platformEntity, map,type);
        }
    }


    /**
     * 启用、禁用
     */
    public void excuteOperation(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
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
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 10:35
     * @param apiUtils
     * @param platformEntity
     * @param map
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map) {
        //操作项
        String operate = (String) map.get("operate");
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.CUSTOMER_CONTACT.getCode(),operate);
        return;
    }

    /**
     * @description: 审核
     * @author Will
     * @date: 2023/9/26 10:30
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }
        //地址编号
        String addressCode = String.valueOf(map.get("addressCode"));
        //联系人金蝶id
        String syncKingdeeId = String.valueOf(map.get("syncKingdeeId"));
        //联系人编号
        String code = String.valueOf(map.get("code"));
        //如果所有编码都没有无法同步，需要手动设置好编号
        if (StringUtils.isBlank(addressCode) || (StringUtils.isBlank(syncKingdeeId) && StringUtils.isBlank(code))) {
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_ADDRESS_OR_CONTRACT);
        }

        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {
            //更新数据
            Boolean flag = kingdeeCommonService.saveOrUpdateCustomerContact(platformEntity, map, apiUtils, json, param, type);
            if (flag) {
                //如果新增是禁用状态需要调用禁用接口
                if (Boolean.valueOf(map.get("disabled").toString())) {
                    //启用、禁用
                    excuteOperation(apiUtils, platformEntity, map, type);
                }
            }
            return;
        }
        String id = String.valueOf(model.get("Id"));
        String forbidStatus = String.valueOf(model.get("ForbidStatus"));
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        Boolean flag = Boolean.FALSE;
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //已禁用不
            if (forbidStatus.equals("B")) {
                return;
            }
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //给修改json对象赋值ID
        KingdeeUtils.makeFieldJson(json, "FCONTACTID", ".", id);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        //更新数据
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            kingdeeCommonService.saveOrUpdateCustomerContact(platformEntity, map, apiUtils, json, param, type);
            if ((forbidStatus.equals("B") && Boolean.valueOf(map.get("disabled").toString()).equals(Boolean.FALSE)) || (forbidStatus.equals("A") && Boolean.valueOf(map.get("disabled").toString()))) {
                //启用、禁用
                excuteOperation(apiUtils, platformEntity, map, type);
            }
        }
    }
}
