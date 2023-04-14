package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private List<SupplierContactDTO.ImportAddDTO> contactList = new ArrayList<>();

    /**
     * 账户信息
     */
    List<SupplierAccountDTO.ImportAddDTO> bankAccountList = new ArrayList<>();

    /**
     * 资质信息
     */
    List<SupplierCredentialDTO.ImportAddDTO> credentialList = new ArrayList<>();


    //币种信息
    private List<CurrencyDTO.ViewDTO> currencyList;
    /**
     * 错误信息
     */
    private List<SupplierImportExcelDTO> errorList = new ArrayList<>();

    private List<SupplierDTO.ImportAddDTO> addList = new ArrayList<>();

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

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

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
        SupplierDTO.ImportAddDTO addDTO = new SupplierDTO.ImportAddDTO();
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
        addDTO.setGradeName(gradeName);
        //采购员
        String purchaseUserName = excelDTO.getPurchaseUserName();
        if (StringUtils.isNotBlank(purchaseUserName)) {
            String purchaseUserId = userList.stream().filter(u -> u.getUserName().equals(purchaseUserName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (StringUtils.isBlank(purchaseUserId)) {
                errorMsgList.add("采购员不存在");
            }
            addDTO.setPurchaseUserId(purchaseUserId);
        }
        addDTO.setPurchaseUserName(purchaseUserName);

        addDTO.setCompanyAddress(excelDTO.getCompanyAddress());
        addDTO.setCompanyWebsite(excelDTO.getCompanyWebsite());
        //启用状态
        String enabled = excelDTO.getEnabled();
        if (StringUtils.isNotBlank(enabled)) {
            addDTO.setDisabled(!enabled.equals("启用"));
        }

        //结算方式
        String payMethodName = excelDTO.getPayMethodName();
        if (StringUtils.isNotBlank(payMethodName)) {
            String payMethodId = dictBasicList.stream().filter(d -> d.getName().equals(payMethodName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(payMethodId)) {
                errorMsgList.add("结算方式不存在");
            }
            addDTO.setPayMethodId(payMethodId);
        }

        //结算币种
        String payCurrency = excelDTO.getPayCurrency();
        if (StringUtils.isNotBlank(payCurrency)) {
            CurrencyDTO.ViewDTO currency = currencyList.stream().filter(c -> c.getName().equals(payCurrency)).findFirst().orElse(null);
            if (Objects.isNull(currency)) {
                errorMsgList.add("结算币种不存在");
            } else {
                addDTO.setPayCurrency(currency.getId());
            }
        }


        //分类名
        String categoryName = excelDTO.getCategoryName();
        if (StringUtils.isNotBlank(categoryName)) {
            String categoryId = dictBasicList.stream().filter(d -> d.getName().equals(categoryName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(categoryId)) {
                errorMsgList.add("供应商分类不存在");
            }
            addDTO.setCategoryId(categoryId);
        }

        addDTO.setCategoryName(categoryName);
        //联系人信息
        SupplierContactDTO.ImportAddDTO contact = new SupplierContactDTO.ImportAddDTO();
        String person = excelDTO.getPerson();
        contact.setPerson(excelDTO.getPerson());
        contact.setPosition(excelDTO.getPosition());
        contact.setEmail(excelDTO.getEmail());
        contact.setRemark(excelDTO.getContactRemark());
        String contactEnabled = excelDTO.getContactEnabled();
        if (StringUtils.isNotBlank(contactEnabled)) {
            contact.setDisabled(!contactEnabled.equals("启用"));
        }
        contact.setTelNumber(excelDTO.getTelNumber());
        String isDefault = excelDTO.getIsDefault();
        if (StringUtils.isNotBlank(isDefault)) {
            boolean isDefaultResult = isDefault.equals("是");
            //当是默认联系人的时候
            if (isDefaultResult) {
                //存在的 默认联系人
                SupplierContactDTO.ImportAddDTO existContact = contactList.stream().filter(c -> c.getSupplierName().equals(name) &&
                        c.getIsDefault()).findFirst().orElse(null);
                if (existContact != null) {
                    errorMsgList.add("默认联系人已存在");
                }
            }
            contact.setIsDefault(isDefaultResult);
        }
        //当不为空的时候就要检查 联系人是否为空
        if (!checkObjAllFieldsIsNull(contact)) {
            if (StringUtils.isBlank(person)) {
                errorMsgList.add("联系人不能为空");
            }
        }
        contact.setSupplierName(name);


        //账户信息
        SupplierAccountDTO.ImportAddDTO bankAccount = new SupplierAccountDTO.ImportAddDTO();
        bankAccount.setBankAccount(excelDTO.getBankAccount());
        String payee = excelDTO.getPayee();
        bankAccount.setPayee(payee);
        //银行信息
        String bankName = excelDTO.getBankName();
        if (StringUtils.isNotBlank(bankName)) {
            String bankId = bankList.stream().filter(b -> b.getName().equals(bankName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(bankId)) {
                errorMsgList.add("银行不存在");
            }
            bankAccount.setBankId(bankId);
        }

        bankAccount.setBankSubbranch(excelDTO.getBankSubbranch());
        bankAccount.setRemark(excelDTO.getAccountRemark());
        //银行支付方式
        String bankPayMethodName = excelDTO.getBankPayMethodName();
        if (StringUtils.isNotBlank(bankPayMethodName)) {
            String bankPayMethodId = dictBasicList.stream().filter(d -> d.getName().equals(bankPayMethodName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(bankPayMethodId)) {
                errorMsgList.add("支付方式不存在");
            }
            bankAccount.setPayMethodId(bankPayMethodId);

        }

        //当不为空的时候就要检查 账户名称是否为空
        if (!checkObjAllFieldsIsNull(bankAccount)) {
            if (StringUtils.isBlank(payee)) {
                errorMsgList.add("账户名称不能为空");
            }
        }

        bankAccount.setSupplierName(name);

        //资质信息
        SupplierCredentialDTO.ImportAddDTO credential = new SupplierCredentialDTO.ImportAddDTO();
        String credentialName = excelDTO.getCredentialName();
        credential.setName(credentialName);
        credential.setRemark(excelDTO.getCredentialRemark());


        //存在错误数据则直接返回 因为 这里可能给一个错误的 日期格式
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        //有效日期 起
        String effectiveDate = excelDTO.getEffectiveDate();
        //有效日期 止
        String expireDate = excelDTO.getExpireDate();
        LocalDate effective = StringUtils.isBlank(effectiveDate) ? null : LocalDate.parse(effectiveDate, dateTimeFormatter);
        LocalDate expire = StringUtils.isBlank(expireDate) ? null : LocalDate.parse(expireDate, dateTimeFormatter);

        if (effective != null && expire != null) {
            if (effective.compareTo(expire) > 0) {
                errorMsgList.add("资质有效起不能大于资质有效止");
            }
        }
        credential.setExpireDate(expire);
        credential.setEffectiveDate(effective);

        //当不为空的时候就要检查 资质名称是否为空
        if (!checkObjAllFieldsIsNull(credential)) {
            if (StringUtils.isBlank(credentialName)) {
                errorMsgList.add("资质名称不能为空");
            }
        }
        credential.setSupplierName(name);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        //资质名称
        if (StringUtils.isNotBlank(credentialName)) {
        //已存在的
        SupplierCredentialDTO.ImportAddDTO existCredential = credentialList.stream().filter(c -> c.getSupplierName().equals(name) &&
                    c.getName().equals(credentialName)).findFirst().orElse(null);
        if (Objects.isNull(existCredential)) {
            credentialList.add(credential);
        }
        }


        if(StringUtils.isNotBlank(payee)){
        //已存在的
        SupplierAccountDTO.ImportAddDTO existAccount = bankAccountList.stream().filter(b -> b.getPayee().equals(excelDTO.getPayee()) &&
                b.getSupplierName().equals(name)).findFirst().orElse(null);
        if (Objects.isNull(existAccount)) {
            bankAccountList.add(bankAccount);
        }
        }

        if(StringUtils.isNotBlank(person)){
        //存在的联系人
        SupplierContactDTO.ImportAddDTO existContact = contactList.stream().filter(c -> c.getSupplierName().equals(name) &&
                    c.getPerson().equals(person)).findFirst().orElse(null);
        if (Objects.isNull(existContact)) {
            contactList.add(contact);
        }
        }


        //在已添加的供应商里面找到对应供应商信息
        SupplierDTO.ImportAddDTO existSupplier = addList.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
        if (Objects.isNull(existSupplier)) {
            addList.add(addDTO);
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
            for (SupplierDTO.ImportAddDTO add : addList) {
                String supplierName = add.getName();
                List<SupplierContactDTO.ImportAddDTO> contactAddList = contactList.stream().filter(c -> c.getSupplierName().equals(supplierName) && StringUtils.isNotBlank(c.getPerson())).collect(Collectors.toList());
                add.setContactList(contactAddList);
                List<SupplierAccountDTO.ImportAddDTO> bankAccountAddList = bankAccountList.stream().filter(c -> c.getSupplierName().equals(supplierName) && StringUtils.isNotBlank(c.getPayee())).collect(Collectors.toList());
                add.setBankAccountList(bankAccountAddList);
                List<SupplierCredentialDTO.ImportAddDTO> credentialAddList = credentialList.stream().filter(c -> c.getSupplierName().equals(supplierName) && StringUtils.isNotBlank(c.getName())).collect(Collectors.toList());
                add.setCredentialList(credentialAddList);
            }
            supplierService.batchImportSupplier(addList);
        }


    }


    /**
     * 获取错误信息
     *
     * @param
     * @return java.util.List<com.erp.model.scm.dto.excel.SupplierImportExcelDTO>
     * @author yl
     * @date 2023-03-30 16:13
     */
    public List<SupplierImportExcelDTO> getErrorList() {
        return errorList;
    }


    /**
     * 检查obj所有字段是否为空
     *
     * @param obj
     * @return
     */
    public boolean checkObjAllFieldsIsNull(Object obj) {
        // 如果对象为null直接返回true
        if (null == obj) {
            return true;
        }
        try {
            // 挨个获取对象属性值
            for (Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                // 如果有一个属性值不为null，且值不是空字符串，就返回false
                if (f.get(obj) != null && StringUtils.isNotBlank(f.get(obj).toString())) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }
}
