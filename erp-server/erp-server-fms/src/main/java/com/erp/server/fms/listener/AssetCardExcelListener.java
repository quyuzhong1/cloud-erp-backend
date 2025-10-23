package com.erp.server.fms.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.exception.ServiceException;
import com.erp.model.fms.dto.AssetCardDTO;
import com.erp.model.fms.dto.AssetCardDetailDTO;
import com.erp.model.fms.dto.excel.AssetCardImportExcelDTO;
import com.erp.model.fms.enums.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.fms.service.AssetCardService;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资产卡片导入Excel监听器
 * 
 * @author wuht
 * @date 2025-01-10
 */
@Slf4j
public class AssetCardExcelListener extends AnalysisEventListener<AssetCardImportExcelDTO> {

    private final AssetCardService assetCardService;
    private final AssetLocationService assetLocationService;
    private final SysUserFeign sysUserFeign;
    private final String taskId;
    private final String importType;
    private final Integer importCount;
    
    private final List<AssetCardImportExcelDTO> successList = new ArrayList<>();
    private final List<AssetCardImportExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;
    
    // 缓存数据
    private List<FindUserDTO> userList;
    private List<SysDepartmentDTO> deptList;
    private Map<String, String> assetLocationMap;

    public AssetCardExcelListener(AssetCardService assetCardService, 
                                 AssetLocationService assetLocationService,
                                 SysUserFeign sysUserFeign,
                                 String taskId, 
                                 String importType, 
                                 Integer importCount) {
        this.assetCardService = assetCardService;
        this.assetLocationService = assetLocationService;
        this.sysUserFeign = sysUserFeign;
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        
        // 初始化缓存数据
        initCacheData();
    }

    private void initCacheData() {
        try {
            // 获取用户列表
            userList = sysUserFeign.getUserList();
            // 获取部门列表
            deptList = sysUserFeign.getDeptList();
            // 获取资产位置列表
            List<com.erp.model.fms.entity.AssetLocationEntity> assetLocationList = assetLocationService.list();
            assetLocationMap = assetLocationList.stream()
                .collect(Collectors.toMap(
                    com.erp.model.fms.entity.AssetLocationEntity::getAddress,
                    com.erp.model.fms.entity.AssetLocationEntity::getId,
                    (existing, replacement) -> existing
                ));
        } catch (Exception e) {
            log.error("初始化缓存数据失败", e);
        }
    }

    @Override
    public void invoke(AssetCardImportExcelDTO data, AnalysisContext context) {
        count++;
        try {
            // 数据验证
            validateData(data);
            
            // 数据转换
            AssetCardDTO.AddDTO addDTO = convertToAddDTO(data);
            
            // 保存数据
            assetCardService.add(addDTO);
            
            successList.add(data);
            log.info("第{}行数据导入成功：{}", count, data.getName());
            
        } catch (Exception e) {
            log.error("第{}行数据导入失败：{}", count, e.getMessage());
            data.setErrorMsg(e.getMessage());
            errorList.add(data);
            errorNoList.add(String.valueOf(data.getNo()));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("资产卡片导入完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        // 处理导入结果
        handleImportResult();
    }

    private void validateData(AssetCardImportExcelDTO data) {
        if (data.getNo() == null) {
            throw new ServiceException("序号不能为空");
        }
        
        if (StringUtils.isBlank(data.getOrgName())) {
            throw new ServiceException("资产组织不能为空");
        }
        
        if (StringUtils.isBlank(data.getUnit())) {
            throw new ServiceException("计量单位不能为空");
        }
        
        if (StringUtils.isBlank(data.getType())) {
            throw new ServiceException("资产类别不能为空");
        }
        
        if (StringUtils.isBlank(data.getStatus())) {
            throw new ServiceException("资产状态不能为空");
        }
        
        if (StringUtils.isBlank(data.getChangeMethod())) {
            throw new ServiceException("变动方式不能为空");
        }
        
        if (StringUtils.isBlank(data.getName())) {
            throw new ServiceException("资产名称不能为空");
        }
        
        if (data.getStartUseDate() == null) {
            throw new ServiceException("开始使用日期不能为空");
        }
        
        if (StringUtils.isBlank(data.getAssetLocationName())) {
            throw new ServiceException("资产位置不能为空");
        }
        
        if (data.getQty() == null || data.getQty() <= 0) {
            throw new ServiceException("数量必须大于0");
        }
        
        if (StringUtils.isBlank(data.getUseDeptName())) {
            throw new ServiceException("使用部门不能为空");
        }
        
        if (StringUtils.isBlank(data.getCostType())) {
            throw new ServiceException("费用项目不能为空");
        }
        
        // 验证枚举值
        validateEnumValues(data);
        
        // 验证关联数据
        validateRelatedData(data);
    }

    private void validateEnumValues(AssetCardImportExcelDTO data) {
        // 验证计量单位
        if (UnitEnum.getName(data.getUnit()).isEmpty()) {
            throw new ServiceException("计量单位【" + data.getUnit() + "】不存在");
        }
        
        // 验证资产类别
        if (AssetCategoryEnum.getName(data.getType()).isEmpty()) {
            throw new ServiceException("资产类别【" + data.getType() + "】不存在");
        }
        
        // 验证资产状态
        if (AssetStatusEnum.getName(data.getStatus()).isEmpty()) {
            throw new ServiceException("资产状态【" + data.getStatus() + "】不存在");
        }
        
        // 验证变动方式
        if (ChangeMethodEnum.getName(data.getChangeMethod()).isEmpty()) {
            throw new ServiceException("变动方式【" + data.getChangeMethod() + "】不存在");
        }
        
        // 验证费用项目
        if (DepreciationChargeEnum.getName(data.getCostType()).isEmpty()) {
            throw new ServiceException("费用项目【" + data.getCostType() + "】不存在");
        }
    }

    private void validateRelatedData(AssetCardImportExcelDTO data) {
        // 验证资产位置
        if (!assetLocationMap.containsKey(data.getAssetLocationName())) {
            throw new ServiceException("资产位置【" + data.getAssetLocationName() + "】不存在");
        }
        
        // 验证使用部门
        if (CollUtil.isNotEmpty(deptList)) {
            boolean deptExists = deptList.stream()
                .anyMatch(dept -> data.getUseDeptName().equals(dept.getName()));
            if (!deptExists) {
                throw new ServiceException("使用部门【" + data.getUseDeptName() + "】不存在");
            }
        }
    }

    private AssetCardDTO.AddDTO convertToAddDTO(AssetCardImportExcelDTO data) {
        AssetCardDTO.AddDTO addDTO = new AssetCardDTO.AddDTO();
        
        // 主表数据
        addDTO.setOrgName(data.getOrgName());
        addDTO.setType(data.getType());
        addDTO.setName(data.getName());
        addDTO.setUnit(data.getUnit());
        addDTO.setQty(data.getQty());
        addDTO.setStartUseDate(data.getStartUseDate());
        addDTO.setRemark(data.getRemark());
        addDTO.setStatus(data.getStatus());
        addDTO.setChangeMethod(data.getChangeMethod());
        addDTO.setSourceType(CardSourceEnum.MANUAL_CREATE.getCode());
        
        // 明细数据
        AssetCardDetailDTO.AddDTO detailDTO = new AssetCardDetailDTO.AddDTO();
        detailDTO.setAssetLocationId(assetLocationMap.get(data.getAssetLocationName()));
        detailDTO.setQty(data.getQty());
        detailDTO.setUseDeptName(data.getUseDeptName());
        detailDTO.setCostType(data.getCostType());
        detailDTO.setRemark(data.getDetailRemark());
        
        addDTO.setDetailList(Collections.singletonList(detailDTO));
        
        return addDTO;
    }

    private void handleImportResult() {
        // 这里可以添加导入结果处理逻辑，比如发送通知等
        log.info("导入任务完成，任务ID：{}，成功：{}，失败：{}", taskId, successList.size(), errorList.size());
    }

    public List<AssetCardImportExcelDTO> getSuccessList() {
        return successList;
    }

    public List<AssetCardImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
    }
}
