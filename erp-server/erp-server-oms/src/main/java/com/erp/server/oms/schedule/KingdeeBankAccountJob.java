package com.erp.server.oms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.kingdee.KingdeeShopEntity;
import com.erp.model.oms.dto.BankAccountDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.oms.service.BankAccountService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金蝶银行账号定时任务
 *
 * @author Lambda
 * @Classname KingdeeBankAccountJob
 * @Description TODO
 * @Date 2024-03-07 14:25
 * @Created by yl
 */
@Component
@Slf4j
public class KingdeeBankAccountJob {

    @Resource
    private BankAccountService bankAccountService;

    @Resource
    private SysUserFeign sysUserFeign;


    @XxlJob("syncBankAccount")
    public void syncBankAccountJob() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.CN_BANKACNT.getCode());
        //查询子单据id
        String fieldKeys = "FNumber,FName,FUseOrgId.FNumber,FBANKACNTID,FFORBIDSTATUS,FDOCUMENTSTATUS";
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 1000;
        Boolean dataSign = true;
        List<BankAccountDTO.KingdeeBankAccountDTO> bankAccountDTOList = new ArrayList<>(200);
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList("", fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<BankAccountDTO.KingdeeBankAccountDTO> entityList = result.stream().map(entity ->
                    BeanUtil.toBean(entity, BankAccountDTO.KingdeeBankAccountDTO.class)).collect(Collectors.toList());
            bankAccountDTOList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<BankAccountEntity> dbList = bankAccountService.list();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.emptyList());
        List<BankAccountEntity> saveOrUpdateList = new ArrayList<>(10);
        for (BankAccountDTO.KingdeeBankAccountDTO item : bankAccountDTOList) {
            String kingdeeId = item.getKingdeeId();
            String bankAccountNo = item.getBankAccountNo();
            String accountName = item.getAccountName();
            String kingdeeStatus = item.getKingdeeStatus();
            //禁用状态
            String kingdeeDisabledStatus = item.getKingdeeDisabledStatus();
            Boolean disabled = "B".equals(kingdeeDisabledStatus);
            String userOrgCode = item.getUserOrgCode();
            BaseIdDTO.CodeDTO orgInfo = orgList.stream().filter(org -> org.getCode().equals(userOrgCode)).
                    findFirst().orElse(null);
            if (Objects.isNull(orgInfo)) {
                continue;
            }
            BankAccountEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);
            if (Objects.isNull(dbEntity)) {
                BankAccountEntity addEntity = new BankAccountEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setBankAccountNo(bankAccountNo);
                addEntity.setAccountName(accountName);
                addEntity.setKindeeOrgCode(userOrgCode);
                addEntity.setKingdeeStatus(kingdeeStatus);
                addEntity.setDisabled(disabled);
                addEntity.setOrgId(orgInfo.getId());
                addEntity.setOrgName(orgInfo.getName());
                saveOrUpdateList.add(addEntity);
            } else {
                //表示有 是否修改
                if (!dbEntity.getBankAccountNo().equals(bankAccountNo) || !dbEntity.getAccountName().equals(accountName) ||
                        !dbEntity.getKindeeOrgCode().equals(userOrgCode) || !dbEntity.getKingdeeStatus().
                        equals(kingdeeStatus) || !dbEntity.getDisabled().equals(disabled)) {
                    dbEntity.setBankAccountNo(bankAccountNo);
                    dbEntity.setAccountName(accountName);
                    dbEntity.setKindeeOrgCode(userOrgCode);
                    dbEntity.setKingdeeStatus(kingdeeStatus);
                    dbEntity.setDisabled(disabled);
                    dbEntity.setOrgId(orgInfo.getId());
                    dbEntity.setOrgName(orgInfo.getName());
                    saveOrUpdateList.add(dbEntity);
                }
            }

        }
        bankAccountService.saveOrUpdateBatch(saveOrUpdateList);
    }
}
