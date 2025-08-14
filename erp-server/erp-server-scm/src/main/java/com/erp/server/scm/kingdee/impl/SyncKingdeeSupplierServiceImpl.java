package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSupplierService;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSupplierServiceImpl implements SyncKingdeeSupplierService {

    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SupplierGradeService supplierGradeService;

    @Resource
    private SupplierContactService supplierContactService;


    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;

    @Autowired
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private ScmPushMsgService scmPushMsgService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(SupplierEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (SupplierEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SUPPLIER.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SUPPLIER.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SUPPLIER_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.SUPPLIER.getCode());
        scmPushMsgEntity.setSourceId(entity.getId());
        scmPushMsgEntity.setSourceCode(entity.getCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        scmPushMsgService.save(scmPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(SupplierEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //供货识别码
        resultMap.put("identificationCode",entity.getIdentificationCode());
        //注册资金(万)
        resultMap.put("registeredCapital",entity.getRegisteredCapital());
        //名称
        resultMap.put("name",entity.getName());
        //公司地址
        resultMap.put("companyAddress",entity.getCompanyAddress());
        //公司网址
        resultMap.put("companyWebsite",entity.getCompanyWebsite());
        //是否禁用
        resultMap.put("disabled",entity.getDisabled());

        //审核未通过、非反审核不推送
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus())
                && !SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            return null;
        }

        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaseUserId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                //采购员
                resultMap.put("purchaseUserCode",findUserDTO.getCode());
            }
        }

        DictBasicEntity category = dictBasicService.getById(entity.getCategoryId());
        if (ObjectUtils.isNotEmpty(category)) {
            //供应商分类
            resultMap.put("category",category.getValue());
        }
        SupplierGradeEntity grade = supplierGradeService.getBySupplierId(entity.getGradeId());
        if (ObjectUtils.isNotEmpty(grade)) {
            //供应商等级
            resultMap.put("grade",grade.getName());
        }
        //结算币别
        resultMap.put("payCurrency",entity.getPayCurrency());

        DictBasicEntity payMethod = dictBasicService.getById(entity.getPayMethodId());
        if (ObjectUtils.isNotEmpty(payMethod)) {
            //结算方式
            resultMap.put("payMethod",payMethod.getValue());
        }

        // 付款条件
        String paymentCondition = entity.getPaymentCondition();
        if (StrUtils.isNotEmpty(paymentCondition)) {
            KingdeePaymentConditionEntity conditionEntity = kingdeePaymentConditionService.getByCode(paymentCondition);
            if (Objects.nonNull(conditionEntity)) {
                resultMap.put("paymentCondition", conditionEntity.getCode());
            }
        }

        List<JSONObject>  blankList = new ArrayList<>();
        List<SupplierAccountEntity> accountList = supplierAccountService.listBySupplierIdList(Arrays.asList(entity.getId()));

        if (CollectionUtils.isNotEmpty(accountList)) {
            //查询银行信息
            List<String> bankIds = accountList.stream().map(SupplierAccountEntity::getBankId).distinct().collect(Collectors.toList());
            List<BaseIdDTO> bankList = sysUserFeign.getBankList(bankIds);
            List<DictBasicEntity> list = FeignQuery.list(DictBasicEntity.class);
            Map<String, String> stringMap = list.stream().filter(v -> v.getType().equals(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType()))
                    .collect(Collectors.toMap(DictBasicEntity::getId, DictBasicEntity::getName, (o1, o2) -> o1));
            for (SupplierAccountEntity accountEntity : accountList) {
                JSONObject bank = new JSONObject();
                //银行账号
                bank.set("bankAccount",accountEntity.getBankAccount());
                //收款方
                bank.set("payee",accountEntity.getPayee());
                if (CollectionUtils.isNotEmpty(bankList)) {
                    String bankName = bankList.stream().filter(obj -> obj.getId().equals(accountEntity.getBankId())).map(BaseIdDTO::getName).findFirst().orElse(null);
                    //收款银行
                    bank.set("bankName",bankName);
                }
                //开户银行
                bank.set("bankSubbranch",accountEntity.getBankSubbranch());
                String sb = "【支付方式】 " +
                        stringMap.get(accountEntity.getPayMethodId()) + "\n" +
                        "【备注】 " +
                        accountEntity.getRemark();
                //备注
                bank.set("remark", sb);
                blankList.add(bank);
            }
            resultMap.put("blankList",blankList);
        }

        //联系人信息
        List<SupplierContactDTO.UpdateDTO> contacts = supplierContactService.listBySupplierId(entity.getId());
        if (CollectionUtils.isNotEmpty(contacts)) {
            List<JSONObject>  contactList = new ArrayList<>();
            for (SupplierContactDTO.UpdateDTO updateDTO : contacts) {
                JSONObject contact = new JSONObject();
                contact.set("person",updateDTO.getPerson());
                contact.set("email",updateDTO.getEmail());
                contact.set("telNumber",updateDTO.getTelNumber());
                contact.set("isDefault",updateDTO.getIsDefault());
                contact.set("position",updateDTO.getPosition());
                contact.set("disabled",updateDTO.getDisabled());
                contact.set("remark",updateDTO.getRemark());
                contactList.add(contact);
            }
            resultMap.put("contactList",contactList);
        }
        return resultMap;
	}
}
