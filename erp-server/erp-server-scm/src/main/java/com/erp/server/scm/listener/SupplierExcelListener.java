package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.SupplierContactDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    //币种信息
    private List<CurrencyDTO.ViewDTO> currencyList;
    /**
     * 错误信息
     */
    private List<SupplierImportExcelDTO> errorList = new ArrayList<>();

    private List<SupplierDTO.AddDTO> addList = new ArrayList<>();

    public SupplierExcelListener(SupplierService supplierService, List<SupplierGradeEntity> supplierGradeList, List<DictBasicEntity> dictBasicList,
                                 List<SupplierEntity> supplierList, List<FindUserDTO> userList, List<CurrencyDTO.ViewDTO> currencyList) {
        this.supplierService = supplierService;
        this.supplierGradeList = supplierGradeList;
        this.dictBasicList = dictBasicList;
        this.supplierList = supplierList;
        this.userList = userList;
        this.currencyList = currencyList;
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
        //供应商名称
        String name = excelDTO.getName();
        long count = supplierList.stream().filter(s -> s.getName().equals(name)).count();
        if (count > 0) {
            errorMsgList.add("供应商名称已存在");
        }
        SupplierDTO.AddDTO addDTO = new SupplierDTO.AddDTO();
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
        //结算币种
        String payCurrency = excelDTO.getPayCurrency();
        if (StringUtils.isNotBlank(payCurrency)) {
            long currencyCount = currencyList.stream().filter(c -> c.getId().equals(payCurrency)).count();
            if (currencyCount <= 0) {
                errorMsgList.add("结算币种不存在");
            }
        }
        //联系人信息
        List<SupplierContactDTO.AddDTO> contactList = new ArrayList<>(5);
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

    }
}
