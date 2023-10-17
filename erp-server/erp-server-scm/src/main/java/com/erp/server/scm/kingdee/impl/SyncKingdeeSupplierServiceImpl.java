package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.enums.SysDictBasicEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSupplierService;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.SupplierAccountService;
import com.erp.server.scm.service.SupplierContactService;
import com.erp.server.scm.service.SupplierGradeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(SupplierEntity entity, String operate) {
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
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }
        //名称
        resultMap.put("name",entity.getName());
        //公司地址
        resultMap.put("companyAddress",entity.getCompanyAddress());
        //公司网址
        resultMap.put("companyWebsite",entity.getCompanyWebsite());
        //是否禁用
        resultMap.put("disabled",entity.getDisabled());

        //审核未通过、非反审核不推送
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus()) && !SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            return;
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
        if(StrUtils.isNotEmpty(entity.getPaymentCondition())) {
            List<DictBasicDTO.ViewDTO> dicts = sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());
            List<String> dictCodes = dicts.stream().map(DictBasicDTO.ViewDTO::getValue).distinct().collect(Collectors.toList());
            if(CollUtil.isNotEmpty(dictCodes) && dictCodes.contains(entity.getPaymentCondition())) {
                resultMap.put("paymentCondition",entity.getPaymentCondition());
            }
        }

        List<JSONObject>  blankList = new ArrayList<>();
        List<SupplierAccountEntity> accountList = supplierAccountService.listBySupplierId(entity.getId());

        if (CollectionUtils.isNotEmpty(accountList)) {
            //查询银行信息
            List<String> bankIds = accountList.stream().map(SupplierAccountEntity::getBankId).distinct().collect(Collectors.toList());
            List<BaseIdDTO> bankList = sysUserFeign.getBankList(bankIds);
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
                //备注
                bank.set("remark",accountEntity.getRemark());
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

        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (SupplierEntity entity, String operate, Map<String, Object> resultMap) {
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
        dmpMqFeign.sendMqAndSaveTask(dmpSyncTaskDTO);
    }
}
