package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.erp.server.wms.service.WarehouseMappingService;
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

 * @Date 2023-03-22 17:22
 * @Created by yl
 */
public class WarehouseExcelListener extends AnalysisEventListener<WarehouseExcelDTO> {

    private WarehouseService warehouseService;

    private List<DictBasicEntity> dictBasicList;

    private List<FindUserDTO> userList;

    private List<BaseIdDTO.CodeDTO> orgList;

    private List<WarehouseEntity> existList;

    private List<WarehouseEntity> addWarehouseList = new ArrayList<>();

    private WarehouseMappingService warehouseMappingService;


    /**
     * 错误信息
     */
    private List<WarehouseExcelDTO> errorList = new ArrayList<>();


    public WarehouseExcelListener(WarehouseService warehouseService, List<DictBasicEntity> dictBasicList, List<FindUserDTO> userList, List<BaseIdDTO.CodeDTO> orgList
            , List<WarehouseEntity> existList, WarehouseMappingService warehouseMappingService) {
        this.warehouseService = warehouseService;
        this.dictBasicList = dictBasicList;
        this.userList = userList;
        this.orgList = orgList;
        this.existList = existList;
        this.warehouseMappingService = warehouseMappingService;

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
        WarehouseDTO.AddDTO addDTO = new WarehouseDTO.AddDTO();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(warehouseExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            warehouseExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(warehouseExcelDTO);
            return;
        }
        //类型名称
        String typeName = warehouseExcelDTO.getTypeName();
        String typeId = dictBasicList.stream().filter(d -> d.getName().equals(typeName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (CharSequenceUtil.isBlank(typeId)) {
            errorMsgList.add("仓库类型不存在");
        }

        //仓库经营类型
        String warehouseManageTypeName = warehouseExcelDTO.getWarehouseManageTypeName();
        String warehouseManageType = dictBasicList.stream().filter(d -> d.getName().equals(warehouseManageTypeName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
        if (CharSequenceUtil.isBlank(warehouseManageType)) {
            errorMsgList.add("仓库经营类型不存在");
        }
        addDTO.setWarehouseManageType(warehouseManageType);
        //地理位置
        String geographyLocationName = warehouseExcelDTO.getGeographyLocationName();
        String geographyLocation = dictBasicList.stream().filter(d -> d.getName().equals(geographyLocationName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
        if (CharSequenceUtil.isBlank(geographyLocation)) {
            errorMsgList.add("仓库地理位置不存在");
        }
        addDTO.setGeographyLocation(geographyLocation);
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
        WarehouseEntity warehouseEntity = null;
        if (CharSequenceUtil.isNotBlank(warehouseExcelDTO.getOnwayWarehouseName())) {
            warehouseEntity = existList.stream()
                    .filter(req -> req.getName().equals(warehouseExcelDTO.getOnwayWarehouseName())
                            && !req.getDisabled()
                            && ApproveStatusEnum.APPROVE.equals(req.getApproveStatus())
                    )
                    .findFirst()
                    .orElse(null);
            if (ObjectUtil.isEmpty(warehouseEntity)) {
                errorMsgList.add("在途仓库名称不存在");
            }
        }

        WarehouseMappingEntity checkThirdWarehouseNameExist = warehouseMappingService.checkThirdWarehouseNameExist(warehouseExcelDTO.getThirdWarehouseName(), PlatformDictEnum.ALI_EXPRESS.getCode());
        if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist)) {
            errorMsgList.add((CharSequenceUtil.format(ApiError.THIRD_WAREHOUSE_NAME_EXIST.msg, PlatformDictEnum.ALI_EXPRESS.getCode(), warehouseExcelDTO.getThirdWarehouseName())));
        }

        addDTO.setKingdeeWarehouseCode(kingdeeWarehouseCode);
        addDTO.setName(warehouseExcelDTO.getName());
        addDTO.setTypeId(typeId);
        //组织
        String orgName = warehouseExcelDTO.getOrgName();
        String orgId = orgList.stream().filter(d -> d.getName().equals(orgName)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (CharSequenceUtil.isBlank(orgId)) {
            errorMsgList.add("仓库组织不存在");
        }
        addDTO.setOrgId(orgId);
        //是否虚拟仓
        String isVirtual = warehouseExcelDTO.getIsVirtual();
        List virtualList = Arrays.asList("是", "否");
        //不为 是否
        if (!virtualList.contains(isVirtual)) {
            errorMsgList.add("是否虚拟仓有误");
        }
        addDTO.setIsVirtual("是".equals(isVirtual));
        //仓库负责人
        String chargeName = warehouseExcelDTO.getChargeName();
        if(CharSequenceUtil.isNotBlank(chargeName)){
            String chargeId = userList.stream().filter(d -> d.getUserName().equals(chargeName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (CharSequenceUtil.isBlank(chargeId)) {
                errorMsgList.add("仓库负责人有误");
            }
            addDTO.setChargeId(chargeId);
        }
        //联系人
        String contacts = warehouseExcelDTO.getContacts();
        addDTO.setContacts(contacts);
        //联系电话
        String contactTelNumber = warehouseExcelDTO.getContactTelNumber();
        addDTO.setContactTelNumber(contactTelNumber);
        //仓库地址
        String address = warehouseExcelDTO.getAddress();
        addDTO.setAddress(address);
        //状态
        String enabled = warehouseExcelDTO.getEnabled();
        addDTO.setDisabled(!"启用".equals(enabled));
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            warehouseExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(warehouseExcelDTO);
            return;
        }
        if (ObjectUtil.isNotEmpty(warehouseEntity)) {
            addDTO.setOnwayWarehouseId(warehouseEntity.getId());
            addDTO.setOnwayWarehouseName(warehouseEntity.getName());
        }
        if (CharSequenceUtil.isNotBlank(warehouseExcelDTO.getThirdWarehouseName())) {
            addDTO.setThirdWarehouseName(warehouseExcelDTO.getThirdWarehouseName());
        }

        //保存的数据
        warehouseService.add(addDTO);
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
