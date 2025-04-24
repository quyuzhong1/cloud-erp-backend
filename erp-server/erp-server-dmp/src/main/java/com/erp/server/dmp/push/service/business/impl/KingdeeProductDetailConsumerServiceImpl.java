package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.core.util.StrUtil;
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
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeProductDetailConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.CfgSettingService;
import com.kingdee.bos.webapi.entity.RepoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeProductDetailConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-01 20:10
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeProductDetailConsumerServiceImpl implements KingdeeProductDetailConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private CfgSettingService cfgSettingService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeConsumer(Map<String, Object> map) {

        //同步模块类型
        Integer type = ApiModuleTypeEnum.PRODUCT_DETAIL.getCode();

        //编码转换
        map.put("code",map.get("skuNo"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_MATERIAL.getCode());
        //操作项
        String operate = (String) map.get("operate");

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
     * @description: 审核
     * @author Will
     * @date: 2023/9/26 11:56
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map,platformEntity.getId(),type);
        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }
        //判断金蝶系统是否已存在该数据
        JSONObject model;
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            //是否进行基础性校验
            String value = cfgSettingService.getValue(SettingEnum.KINGDEE_BASE_CHECK_KEY);
            if (StrUtil.isNotBlank(value)) {
                Boolean isCheck = Boolean.valueOf(value);
                param.setIsVerifyBaseDataField(isCheck);
            }
            //未查找到数据，新增数据
            RepoResult save = apiUtils.saveKingDee(param);
            //新增成功后编辑二级类目
            String id = save.getId();
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FMATERIALID",".",id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList)Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        Integer id = (Integer) model.get("Id");
        Boolean flag = Boolean.FALSE;
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            flag = kingdeeCommonService.unAudit(apiUtils,String.valueOf(id));
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {

            LinkedList<String> queryFilters = new LinkedList<>();
            queryFilters.add(String.format("FMATERIALID = '%s'", id));
            String filterStr = String.join(" and ", queryFilters);
            //查询子单据id
            String fieldKeys = "FSubHeadEntity_FEntryId,SubHeadEntity_FEntryId,SubHeadEntity1_FEntryId,SubHeadEntity2_FEntryId,SubHeadEntity3_FEntryId,SubHeadEntity4_FEntryId,SubHeadEntity5_FEntryId," +
                    "SubHeadEntity6_FEntryId,SubHeadEntity7_FEntryId";
            List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
            if (CollectionUtils.isEmpty(queryList)) {
                return;
            }
            Map<String, Object> queryMap = queryList.get(0);
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FMATERIALID",".", id);
            Iterator iter = queryMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                KingdeeUtils.makeFieldJson(json, String.valueOf(entry.getKey()),".",entry.getValue());
            }
            //需要修改字段
            json.remove("FCreateOrgId");
            json.remove("FUseOrgId");
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        }
    }

    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/9/26 11:58
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
     * @description: 删除
     * @author Will
     * @date: 2023/9/26 12:00
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param operate
     */
    public void operateDelete(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,String operate) {
        //删除
        kingdeeCommonService.handleDelete(apiUtils,platformEntity,map,ApiModuleTypeEnum.PRODUCT_DETAIL.getCode(),operate);
        return;
    }
}
