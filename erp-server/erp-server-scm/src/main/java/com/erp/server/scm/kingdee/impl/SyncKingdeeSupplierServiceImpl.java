package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSupplierService;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.SupplierAccountService;
import com.erp.server.scm.service.SupplierGradeService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSupplierServiceImpl implements SyncKingdeeSupplierService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SupplierGradeService supplierGradeService;



    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(SupplierEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //公司地址
        resultMap.put("companyAddress",entity.getCompanyAddress());
        //公司网址
        resultMap.put("companyWebsite",entity.getCompanyWebsite());

        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaseUserId());
        if (ObjectUtils.isNotEmpty(findUserDTO)) {
            //采购员
            resultMap.put("purchaseUserCode",findUserDTO.getCode());
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

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SUPPLIER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return supplierService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }
}
