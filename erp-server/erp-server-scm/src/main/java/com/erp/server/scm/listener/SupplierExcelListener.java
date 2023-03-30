package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SupplierExcelListener
 * @Description TODO
 * @Date 2023-03-30 9:46
 * @Created by yl
 */
public class SupplierExcelListener extends AnalysisEventListener<SupplierImportExcelDTO> {

    private SupplierService supplierService;

    private List<SupplierGradeEntity> supplierGradeList;

    private List<DictBasicEntity> dictBasicList;

    private List<FindUserDTO> userList;
    private List<SupplierEntity> supplierList;

    private List<BaseIdDTO> bankList;

    /**
     * 联系人信息
     */
    private List<SupplierContactDTO.AddDTO> contactList=new ArrayList<>();

    /**
     *  账户信息
     */
    List<SupplierAccountDTO.AddDTO> bankAccountList=new ArrayList<>();

    /**
     * 资质信息
     */
    List<SupplierCredentialDTO.AddDTO> credentialList=new ArrayList<>();


    //币种信息
    private List<CurrencyDTO.ViewDTO> currencyList;
    /**
     * 错误信息
     */
    private List<SupplierImportExcelDTO> errorList = new ArrayList<>();

    private List<SupplierDTO.AddDTO> addList = new ArrayList<>();

    public SupplierExcelListener(SupplierService supplierService, List<SupplierGradeEntity> supplierGradeList, List<DictBasicEntity> dictBasicList,
                                 List<SupplierEntity> supplierList, List<FindUserDTO> userList,
                                 List<CurrencyDTO.ViewDTO> currencyList, List<BaseIdDTO> bankList) {
        this.supplierService = supplierService;
        this.supplierGradeList = supplierGradeList;
        this.dictBasicList = dictBasicList;
        this.supplierList = supplierList;
        this.userList = userList;
        this.currencyList = currencyList;
        this.bankList = bankList;
    }


    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SupplierImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        SupplierDTO.AddDTO addDTO = new SupplierDTO.AddDTO();

        //供应商名称
        String name = excelDTO.getName();
        long count = supplierList.stream().filter(s -> s.getName().equals(name)).count();
        if (count > 0) {
            errorMsgList.add("供应商名称已存在");
        }
        addDTO.setName(name);
        //等级名称
        String gradeName = excelDTO.getGradeName();
        String gradeId = supplierGradeList.stream().filter(g -> g.getName().equals(gradeName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(gradeId)) {
            errorMsgList.add("供应商等级不存在");
        }
        addDTO.setGradeId(gradeId);
        //采购员
        String purchaseUserName = excelDTO.getPurchaseUserName();
        String purchaseUserId = "";
        if (StringUtils.isNotBlank(purchaseUserName)) {
            purchaseUserId = userList.stream().filter(u -> u.getUserName().equals(purchaseUserName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            errorMsgList.add("采购员不存在");
        }
        addDTO.setPurchaseUserId(purchaseUserId);
        addDTO.setCompanyAddress(excelDTO.getCompanyAddress());
        addDTO.setCompanyWebsite(excelDTO.getCompanyWebsite());
        //启用状态
        String enabled = excelDTO.getEnabled();
        addDTO.setDisabled(!enabled.equals("启用"));
        //结算方式
        String payMethodName = excelDTO.getPayMethodName();
        String payMethodId = "";
        if (StringUtils.isNotBlank(payMethodName)) {
            payMethodId = dictBasicList.stream().filter(d -> d.getName().equals(payMethodName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(payMethodId)) {
                errorMsgList.add("结算方式不存在");
            }
        }
        addDTO.setPayMethodId(payMethodId);
        //结算币种
        String payCurrency = excelDTO.getPayCurrency();
        if (StringUtils.isNotBlank(payCurrency)) {
            long currencyCount = currencyList.stream().filter(c -> c.getId().equals(payCurrency)).count();
            if (currencyCount <= 0) {
                errorMsgList.add("结算币种不存在");
            }
        }
        addDTO.setPayCurrency(payCurrency);
        //在已添加的供应商里面找到对应供应商信息
        SupplierDTO.AddDTO existSupplier = addList.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
        if (Objects.isNull(existSupplier)) {
            addList.add(addDTO);
        }

        //联系人信息
        SupplierContactDTO.AddDTO contact = new SupplierContactDTO.AddDTO();
        contact.setPerson(excelDTO.getPerson());
        contact.setPosition(excelDTO.getPosition());
        contact.setEmail(excelDTO.getEmail());
        contact.setRemark(excelDTO.getContactRemark());
        String contactEnabled = excelDTO.getContactEnabled();
        contact.setDisabled(!contactEnabled.equals("启用"));
        contact.setTelNumber(excelDTO.getTelNumber());
        String isDefault = excelDTO.getIsDefault();
        contact.setIsDefault(isDefault.equals("是"));
        contact.setSupplierName(name);
        //存在的联系人
        SupplierContactDTO.AddDTO existContact = contactList.stream().filter(c -> c.getSupplierName().equals(name) &&
                c.getPerson().equals(excelDTO.getPerson())).findFirst().orElse(null);
        if (Objects.isNull(existContact)) {
            contactList.add(contact);
        }


        //账户信息
        SupplierAccountDTO.AddDTO bankAccount = new SupplierAccountDTO.AddDTO();
        bankAccount.setBankAccount(excelDTO.getBankAccount());
        bankAccount.setPayee(excelDTO.getPayee());
        //银行信息
        String bankName = excelDTO.getBankName();
        String bankId = "";
        if (StringUtils.isNotBlank(bankName)) {
            bankId = bankList.stream().filter(b -> b.getName().equals(bankName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(bankId)) {
                errorMsgList.add("银行不存在");
            }
        }
        bankAccount.setBankId(bankId);
        bankAccount.setBankSubbranch(excelDTO.getBankSubbranch());
        bankAccount.setRemark(excelDTO.getAccountRemark());
        //银行支付方式
        String bankPayMethodName = excelDTO.getBankPayMethodName();
        String bankPayMethodId = "";
        if (StringUtils.isNotBlank(bankPayMethodName)) {
            bankPayMethodId = dictBasicList.stream().filter(d -> d.getName().equals(bankPayMethodName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        }
        if (StringUtils.isBlank(bankPayMethodId)) {
            errorMsgList.add("支付方式不存在");
        }
        bankAccount.setSupplierName(name);
        bankAccount.setPayMethodId(bankPayMethodId);
        //已存在的
        SupplierAccountDTO.AddDTO existAccount = bankAccountList.stream().filter(b -> b.getPayee().equals(excelDTO.getPayee()) &&
                b.getSupplierName().equals(name)).findFirst().orElse(null);
        if (Objects.isNull(existAccount)) {
            bankAccountList.add(bankAccount);
        }

        //资质信息
        SupplierCredentialDTO.AddDTO credential = new SupplierCredentialDTO.AddDTO();
        credential.setName(excelDTO.getCredentialName());
        credential.setRemark(excelDTO.getCredentialRemark());
        //有效日期 起
        LocalDate effectiveDate = excelDTO.getEffectiveDate();
        //有效日期 止
        LocalDate expireDate = excelDTO.getExpireDate();
        if (effectiveDate != null && expireDate != null) {
            if (effectiveDate.compareTo(expireDate) > 0) {
                errorMsgList.add("资质有效起不能大于资质有效止");
            }
        }
        credential.setExpireDate(expireDate);
        credential.setEffectiveDate(effectiveDate);
        credential.setSupplierName(name);
        //已存在的
        SupplierCredentialDTO.AddDTO existCredential = credentialList.stream().filter(c -> c.getSupplierName().equals(name) &&
                c.getName().equals(excelDTO.getCredentialName())).findFirst().orElse(null);
        if (Objects.isNull(existCredential)) {
            credentialList.add(credential);
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

    }


    /**
     * 数据全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:51
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(addList)) {
            for (SupplierDTO.AddDTO add : addList) {
                String supplierName = add.getName();
                List<SupplierContactDTO.AddDTO> contactAddList = contactList.stream().filter(c -> c.getSupplierName().equals(supplierName)).collect(Collectors.toList());
                add.setContactList(contactAddList);

                List<SupplierAccountDTO.AddDTO> bankAccountAddList = bankAccountList.stream().filter(c -> c.getSupplierName().equals(supplierName)).collect(Collectors.toList());
                add.setBankAccountList(bankAccountAddList);

                List<SupplierCredentialDTO.AddDTO> credentialAddList = credentialList.stream().filter(c -> c.getSupplierName().equals(supplierName)).collect(Collectors.toList());
                add.setCredentialList(credentialAddList);
            }

            System.out.println(JSONObject.toJSON(addList));

        }


    }


    /**
     * 获取错误信息
     * @author yl
     * @date 2023-03-30 16:13
     * @param
     * @return java.util.List<com.erp.model.scm.dto.excel.SupplierImportExcelDTO>
     */
    public List<SupplierImportExcelDTO> getErrorList() {
        return errorList;
    }
}
