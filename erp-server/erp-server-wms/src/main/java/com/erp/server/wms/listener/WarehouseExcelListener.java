package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Lambda
 * @Classname WarehouseExcelListener
 * @Description TODO
 * @Date 2023-03-22 17:22
 * @Created by yl
 */
public class WarehouseExcelListener extends AnalysisEventListener<WarehouseExcelDTO> {

    private WarehouseService warehouseService;

    private List<DictBasicDTO> dictBasicList;

    private List<FindUserDTO> userList;

    private List<BaseIdDTO> orgList;

    private List<WarehouseEntity> existList;

    private List<WarehouseEntity> addWarehouseList = new ArrayList<>();


    /**
     * 错误信息
     */
    private List<WarehouseExcelDTO> errorList = new ArrayList<>();


    public WarehouseExcelListener(WarehouseService warehouseService, List<DictBasicDTO> dictBasicList, List<FindUserDTO> userList, List<BaseIdDTO> orgList, List<WarehouseEntity> existList) {
        this.warehouseService = warehouseService;
        this.dictBasicList = dictBasicList;
        this.userList = userList;
        this.orgList = orgList;
        this.existList = existList;

    }


    /**
     * 每解析一行数据回调一遍
     *
     * @param warehouseExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-22 17:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(WarehouseExcelDTO warehouseExcelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        WarehouseEntity addEntity = new WarehouseEntity();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(warehouseExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //类型名称
        String typeName = warehouseExcelDTO.getTypeName();
        String typeId = dictBasicList.stream().filter(d -> d.getName().equals(typeName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(typeId)) {
            errorMsgList.add("仓库类型不存在");
        }
        String name = warehouseExcelDTO.getName();
        long nameCount = existList.stream().filter(w -> name.equals(w.getName())).count();
        if (nameCount > 0) {
            errorMsgList.add("仓库名称已存在");
        }


        long addNameCount = addWarehouseList.stream().filter(w -> name.equals(w.getName())).count();
        if (addNameCount > 0) {
            errorMsgList.add("仓库名称已存在");
        }

        String kingdeeWarehouseCode = warehouseExcelDTO.getKingdeeWarehouseCode();
        long codeCount = existList.stream().filter(w -> kingdeeWarehouseCode.equals(w.getKingdeeWarehouseCode())).count();
        if (codeCount > 0) {
            errorMsgList.add("金蝶仓库编号已存在");
        }

        long addCodeCount = addWarehouseList.stream().filter(w -> kingdeeWarehouseCode.equals(w.getKingdeeWarehouseCode())).count();
        if (addCodeCount > 0) {
            errorMsgList.add("金蝶仓库编号已存在");
        }
        addEntity.setKingdeeWarehouseCode(kingdeeWarehouseCode);
        addEntity.setName(warehouseExcelDTO.getName());
        addEntity.setTypeId(typeId);
        //组织
        String orgName = warehouseExcelDTO.getOrgName();
        String orgId = orgList.stream().filter(d -> d.getName().equals(orgName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(orgId)) {
            errorMsgList.add("仓库组织不存在");
        }
        addEntity.setOrgId(orgId);
        //是否虚拟仓
        String isVirtual = warehouseExcelDTO.getIsVirtual();
        List virtualList = Arrays.asList("是", "否");
        //不为 是否
        if (!virtualList.contains(isVirtual)) {
            errorMsgList.add("是否虚拟仓有误");
        }
        addEntity.setIsVirtual(isVirtual.equals("是"));
        //仓库负责人
        String chargeName = warehouseExcelDTO.getChargeName();
        if(StringUtils.isNotBlank(chargeName)){
            String chargeId = userList.stream().filter(d -> d.getUserName().equals(chargeName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (StringUtils.isBlank(chargeId)) {
                errorMsgList.add("仓库负责人有误");
            }
            addEntity.setChargeId(chargeId);
        }
        //联系人
        String contacts = warehouseExcelDTO.getContacts();
        addEntity.setContacts(contacts);
        //联系电话
        String contactTelNumber = warehouseExcelDTO.getContactTelNumber();
        addEntity.setContactTelNumber(contactTelNumber);
        //仓库地址
        String address = warehouseExcelDTO.getAddress();
        addEntity.setAddress(address);
        //状态
        String enabled = warehouseExcelDTO.getEnabled();
        addEntity.setDisabled(!"启用".equals(enabled));
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            warehouseExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(warehouseExcelDTO);
            return;
        }
        //保存的数据
        addWarehouseList.add(addEntity);
    }


    /**
     * 数据全部解析完后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-22 17:59
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(addWarehouseList)) {
            warehouseService.saveBatch(addWarehouseList);
        }
    }

    public List<WarehouseExcelDTO> getErrorList() {
        return errorList;
    }
}
