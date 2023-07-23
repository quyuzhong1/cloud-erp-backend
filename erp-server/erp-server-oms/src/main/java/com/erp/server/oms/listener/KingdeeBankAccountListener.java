package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KingdeeBankAccountExcelDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.sys.dto.excel.UserKingdeePostImportExcelDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.server.oms.service.BankAccountService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname KingdeeBankAccountListener
 * @Date 2023-07-19 9:12
 * @Created by yl
 */
public class KingdeeBankAccountListener extends AnalysisEventListener<KingdeeBankAccountExcelDTO> {


    private BankAccountService bankAccountService;

    private List<BaseIdDTO.CodeDTO> sysAccountingCompanyList;

    private List<BankAccountEntity> addBankAccountList = new ArrayList<>();

    private List<KingdeeBankAccountExcelDTO> errorList = new ArrayList<>();

    public KingdeeBankAccountListener(BankAccountService bankAccountService, List<BaseIdDTO.CodeDTO> sysAccountingCompanyList) {
        this.bankAccountService = bankAccountService;
        this.sysAccountingCompanyList = sysAccountingCompanyList;
    }

    @Override
    public void invoke(KingdeeBankAccountExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String orgCode = excelDTO.getKindeeOrgCode();
        String orgId = sysAccountingCompanyList.stream().filter(s -> s.getCode().equals(orgCode)).
                map(BaseIdDTO.CodeDTO::getId).findFirst().orElse("");
        if (StringUtils.isBlank(orgId)) {
            errorMsgList.add("未找到系统的组织");
        }
        BankAccountEntity bankAccount = new BankAccountEntity();
        String bankAccountNo = excelDTO.getBankAccountNo();
        bankAccount.setAccountName(excelDTO.getAccountName());
        bankAccount.setBankAccountNo(bankAccountNo);
        bankAccount.setBankType(excelDTO.getBankType());
        bankAccount.setKindeeOrgCode(orgCode);
        bankAccount.setOrgId(orgId);
        bankAccount.setOrgName(excelDTO.getOrgName());
        addBankAccountList.add(bankAccount);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        bankAccountService.saveBatch(addBankAccountList);

    }

    public List<KingdeeBankAccountExcelDTO> getErrorList() {
        return errorList;
    }
}
