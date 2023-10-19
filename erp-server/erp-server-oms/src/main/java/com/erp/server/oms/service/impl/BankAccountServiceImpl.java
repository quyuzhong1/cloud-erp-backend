package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.excel.KingdeeBankAccountExcelDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.KingdeeBankAccountListener;
import com.erp.server.oms.mapper.BankAccountMapper;
import com.erp.server.oms.service.BankAccountService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 银行账号 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-07-04
 */
@Slf4j
@Service
public class BankAccountServiceImpl extends SuperServiceImpl<BankAccountMapper, BankAccountEntity> implements BankAccountService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public BankAccountEntity findByAccountNo(String bankAccountNo) {
        return lambdaQuery().eq(BankAccountEntity::getBankAccountNo, bankAccountNo).last("limit 1").one();
    }

    @Override
    public List<BankAccountEntity> findByOrgId(String orgId) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).list();
    }

    @Override
    public List<BankAccountEntity> findByOrgIdAndAccountNo(String orgId, String bankAccountNo) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).eq(BankAccountEntity::getBankAccountNo, bankAccountNo).list();
    }

    /**
     * 根据组织id和银行名称获取到收款账户信息
     * @param orgId
     * @param accountName
     * @return
     */
    @Override
    public BankAccountEntity findByOrgIdAndAccountName(String orgId, String accountName) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).eq(BankAccountEntity::getAccountName, accountName).
                last("LIMIT 1").one();

    }


    /**
     * 导入金蝶银行账号信息
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-19 10:04
     */
    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        List<BaseIdDTO.CodeDTO> sysAccountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.emptyList());
        KingdeeBankAccountListener excelListener = new KingdeeBankAccountListener(this, sysAccountingCompanyList);
        try {
            EasyExcel.read(excelFile.getInputStream(), KingdeeBankAccountExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("金蝶银行账号导入错误！>>>", e);
            return Boolean.FALSE;
        }
        List<KingdeeBankAccountExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "金蝶银行账号错误信息";
            ExcelUtil.export(fileName, "BankAccountError", errorList, KingdeeBankAccountExcelDTO.class, response);
            return Boolean.FALSE;
        }


        return Boolean.TRUE;
    }

}
