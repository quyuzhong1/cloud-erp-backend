package com.erp.server.sys.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.sys.dto.excel.KingdeeBusinessOperatorImportExcelDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname UserKingdeePostExcelListener
 * @Date 2023-06-02 16:05
 * @Created by yl
 */
public class KingdeeBusinessOperatorExcelListener extends AnalysisEventListener<KingdeeBusinessOperatorImportExcelDTO> {


    private List<FindUserDTO> userList;

    private KingdeeBusinessOperatorService kingdeeBusinessOperatorService;


    private List<KingdeeBusinessOperatorEntity> kingdeeBusinessOperatorList;


    private List<KingdeeBusinessOperatorEntity> addOrUpdateList = new ArrayList<>();

    private List<KingdeeBusinessOperatorImportExcelDTO> errorList = new ArrayList<>();


    public KingdeeBusinessOperatorExcelListener(KingdeeBusinessOperatorService kingdeeBusinessOperatorService, List<FindUserDTO> userList, List<KingdeeBusinessOperatorEntity> kingdeeBusinessOperatorList) {
        this.userList = userList;
        this.kingdeeBusinessOperatorList = kingdeeBusinessOperatorList;
        this.kingdeeBusinessOperatorService = kingdeeBusinessOperatorService;
    }

    /**
     * 解析一行 执行一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-02 16:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(KingdeeBusinessOperatorImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //用户名
        String userName = excelDTO.getKingdeeUserName();
        FindUserDTO user = userList.stream().filter(u -> u.getRealName().equals(userName)).findFirst().orElse(null);
        if (Objects.isNull(user)) {
            errorMsgList.add("用户不存在ERP");
        }
        //存在错误数据则直接返回 因为 这里可能给一个错误的 日期格式
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        KingdeeBusinessOperatorEntity business = kingdeeBusinessOperatorList.stream().filter(
                p -> p.getErpUserId().equals(user.getUserId()) &&
                        p.getKingdeeUserId().equals(user.getSyncKingdeeId())
        ).findFirst().orElse(new KingdeeBusinessOperatorEntity());
        business.setKingdeePostCode(excelDTO.getKingdeePostCode());
        business.setKingdeeOrgCode(excelDTO.getKingdeeOrgCode());
        business.setErpUserId(user.getUserId());

        business.setKingdeeOrgName(excelDTO.getKingdeeOrgName());
        business.setKingdeeType(excelDTO.getKingdeeType());
        business.setKingdeeTypeId(excelDTO.getKingdeeTypeId());
        business.setTypeName(excelDTO.getTypeName());
        business.setKingdeeUserId(excelDTO.getKingdeeUserId());
        business.setKingdeeUserCode(excelDTO.getKingdeeUserCode());
        business.setKingdeeUserName(excelDTO.getKingdeeUserName());
        String disabledStr = excelDTO.getDisabled();
        Boolean disabled = Boolean.TRUE;
        if (StringUtils.isNotBlank(disabledStr)) {
            disabled = disabledStr.equals("否");
        }
        business.setDisabled(disabled);
        addOrUpdateList.add(business);
    }


    /**
     * 全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-02 16:06
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        kingdeeBusinessOperatorService.saveOrUpdateBatch(addOrUpdateList);
    }

    public List<KingdeeBusinessOperatorImportExcelDTO> getErrorList() {
        return errorList;
    }
}
