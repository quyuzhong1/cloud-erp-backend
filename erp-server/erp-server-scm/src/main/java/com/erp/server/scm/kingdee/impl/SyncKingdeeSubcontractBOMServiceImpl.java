package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.scm.dto.SubcontractBOMDTO;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractBOMService;
import com.erp.server.scm.service.ScmPushMsgService;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.erp.server.scm.service.SubcontractOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: wtr
 * @Date: 2026/1/19 8:42
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@Service
public class SyncKingdeeSubcontractBOMServiceImpl implements SyncKingdeeSubcontractBOMService {

    @Resource
    private ScmPushMsgService scmPushMsgService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate) {
        //生成任务
        if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(dto, operate, DmpOutputConstant.getQuerySyncMap());
        }else {
            return saveTask(dto, operate, this.newSyncDataToKingdee(dto, operate));
        }
    }

    private DmpPushTaskEntity saveTask (SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.SUBCONTRACT_BOM.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();

        if(CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();

            dmpSyncTaskDTO.setSourceId(dto.getId());
            dmpSyncTaskDTO.setSourceCode(dto.getSourceCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SUBCONTRACT_BOM.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SUBCONTRACT_BOM_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            dmpSyncTaskDTO.setParentId(dto.getSourceId());
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }

        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.SUBCONTRACT_BOM.getCode());
        scmPushMsgEntity.setSourceId(dto.getId());
        scmPushMsgEntity.setSourceCode(dto.getSourceCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        scmPushMsgEntity.setParentId(dto.getSourceId());
        scmPushMsgService.save(scmPushMsgEntity);

        return null;
    }

    @Override
    public Map<String, Object> newSyncDataToKingdee(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id",dto.getId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //委外用料清单号
        resultMap.put("FBillNo", dto.getCode());

        resultMap.put("syncKingdeeId", dto.getSyncKingdeeId());

        resultMap.put("TargetBillTypeId", "SUB_OutSrcBOMChange");

        SubcontractOrderEntity subcontractOrderEntity = subcontractOrderService.getById(dto.getSourceId());
        SysAccountingCompanyEntity accountCompany = sysUserFeign.getCompanyById(subcontractOrderEntity.getSubcontractOrgId());
        if (Objects.nonNull(accountCompany)) {
            //委外组织
            resultMap.put("FSubOrgId", accountCompany.getCode());
        }
        //委外订单编号
        resultMap.put("FSubReqBillNO", subcontractOrderEntity.getCode());

        //委外订单类型
        resultMap.put("FSubReqType", "WWYLQDBGD01_SYS");

        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listByMainId(dto.getSourceId());

        List<JSONObject> list = new ArrayList<>();
        for (SubcontractOrderDetailEntity subcontractOrderDetailEntity : subcontractOrderDetailList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",subcontractOrderDetailEntity.getId());
            //产品编码
            jsonObject.set("FMaterialID", subcontractOrderDetailEntity.getSkuNo());
            list.add(jsonObject);
        }
        resultMap.put("list",list);
        return resultMap;
    }
}
