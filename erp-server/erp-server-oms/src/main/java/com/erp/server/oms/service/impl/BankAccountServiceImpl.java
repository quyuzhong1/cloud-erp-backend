package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.SqlConstants;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.BankAccountDTO;
import com.erp.model.oms.dto.excel.KingdeeBankAccountExcelDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.KingdeeBankAccountListener;
import com.erp.server.oms.mapper.BankAccountMapper;
import com.erp.server.oms.service.BankAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        return lambdaQuery().eq(BankAccountEntity::getBankAccountNo, bankAccountNo).last( SqlConstants.LIMIT_1).one();
    }

    @Override
    public List<BankAccountEntity> findByOrgId(String orgId) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).list();
    }

    @Override
    public List<BankAccountEntity> findByOrgIdAndAccountNo(String orgId, String bankAccountNo) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).eq(BankAccountEntity::getId, bankAccountNo).list();
    }

    /**
     * 根据组织id和银行名称获取到收款账户信息
     *
     * @param orgId
     * @param accountName
     * @return
     */
    @Override
    public BankAccountEntity findByOrgIdAndAccountName(String orgId, String accountName) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).eq(BankAccountEntity::getAccountName, accountName).
                last( SqlConstants.LIMIT_1).one();

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
            EasyExcelFactory.read(excelFile.getInputStream(), KingdeeBankAccountExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("金蝶银行账号导入错误！>>>", e);
            return Boolean.FALSE;
        }
        List<KingdeeBankAccountExcelDTO> errorList = excelListener.getErrorList();
        if (!errorList.isEmpty()) {
            String fileName = "金蝶银行账号错误信息";
            ExcelUtil.export(fileName, "BankAccountError", errorList, KingdeeBankAccountExcelDTO.class, response);
            return Boolean.FALSE;
        }


        return Boolean.TRUE;
    }

    @Override
    public List<BankAccountEntity> listByAccountNameList(List<String> receiveAccountList) {
        if (CollectionUtils.isEmpty(receiveAccountList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BankAccountEntity::getAccountName,receiveAccountList).list();
    }

    @Override
    public PagingVO<BankAccountDTO.PagingViewDTO> paging(PagingDTO<BankAccountDTO.PagingParamDTO> dto) {
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        BankAccountDTO.PagingParamDTO params = dto.getParams();
        IPage<BankAccountDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<BankAccountDTO.PagingViewDTO> list = pageData.getRecords();
        for (BankAccountDTO.PagingViewDTO item : list) {
            Boolean disabled = item.getDisabled();
            String disabledName = Boolean.TRUE.equals(disabled) ? "禁用" : "启用";
            item.setDisabledName(disabledName);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public BankAccountDTO.ViewDTO view(String id) {
        BankAccountEntity bankAccount = this.getById(id);
        if (Objects.isNull(bankAccount)) {
            throw new ServiceException("银行账号不存在");
        }
        BankAccountDTO.ViewDTO viewDTO = new BankAccountDTO.ViewDTO();
        BeanMapper.copy(bankAccount, viewDTO);
        Boolean disabled = viewDTO.getDisabled();
        String disabledName = Boolean.TRUE.equals(disabled) ? "禁用" : "启用";
        viewDTO.setDisabledName(disabledName);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(BankAccountDTO.AddDTO dto) {
        BankAccountEntity bankAccount = new BankAccountEntity();
        BeanMapper.copy(dto, bankAccount);
        // 数据处理
        handleData(bankAccount);
        return this.save(bankAccount);
    }

    @Override
    public void updateDisable(List<String> ids, boolean disable) {
        if (CollectionUtils.isNotEmpty(ids)) {
            this.lambdaUpdate().set(BankAccountEntity::getDisabled, disable).
                    in(BankAccountEntity::getId, ids).update();
        }

    }

    private void handleData(BankAccountEntity bankAccount) {
        String bankAccountNo = bankAccount.getBankAccountNo();
        int count = this.lambdaQuery().eq(BankAccountEntity::getBankAccountNo, bankAccountNo).count();
        if (count > 0) {
            throw new ServiceException("银行账号已存在");
        }
        String orgId = bankAccount.getOrgId();
        SysAccountingCompanyEntity org= sysUserFeign.getCompanyById(orgId);
        if (Objects.isNull(org)) {
            throw new ServiceException("使用组织不存在");
        }
        bankAccount.setOrgName(org.getCompanyName());
        bankAccount.setKindeeOrgCode(org.getCode());

    }

}
