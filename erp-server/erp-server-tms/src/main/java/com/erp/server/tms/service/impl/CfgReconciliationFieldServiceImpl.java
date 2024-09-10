package com.erp.server.tms.service.impl;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldImportExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.CfgReconciliationTypeEnum;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.tms.convert.CfgReconciliationFieldConverter;
import com.erp.server.tms.listener.CfgReconciliationFieldExcelListener;
import com.erp.server.tms.mapper.CfgReconciliationFieldMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_CFG_RECONCILIATION_FIELD;

/**
 * <p>
 * 对账字段配置表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class CfgReconciliationFieldServiceImpl extends SuperServiceImpl<CfgReconciliationFieldMapper, CfgReconciliationFieldEntity> implements CfgReconciliationFieldService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgReconciliationFieldDTO.UpdateDTO updateDTO) {
        CfgReconciliationFieldEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "对账字段配置单"));
        CfgReconciliationFieldEntity cfgReconciliationFieldEntity = BeanMapperUtils.map(CfgReconciliationFieldEntity.class, updateDTO);

        // 数据处理
        handleData(cfgReconciliationFieldEntity);
        log.info("编辑 开始修改对账字段配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgReconciliationFieldEntity);
        if (!save) {
            throw new ServiceException("对账字段配置单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录对账字段配置单日志数据，id：【{}】", cfgReconciliationFieldEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgReconciliationFieldEntity.getId(), "对账字段配置单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgReconciliationFieldEntity, ModuleTypeEnum.CFG_FIELD_RECONCILIATION.getCode(), cfgReconciliationFieldEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgReconciliationFieldDTO.PagingVO> paging(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgReconciliationFieldDTO.PagingVO> pageData = this.baseMapper.paging(query, dto.getParams());
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgReconciliationFieldEntity entity = this.getById(id);
        if (null == entity) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "对账字段配置");
        }
        // TODO 检查下游单据

//        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "对账字段配置");
//        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_LOGISTICS_CHANNEL.getCode(), entity.getId(), "删除对账字段配置");

        removeById(id);

        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public Boolean exportExcel(CfgReconciliationFieldDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("对账字段配置数据", EXPORT_TMS_CFG_RECONCILIATION_FIELD.getCode(), dto);
        return Boolean.TRUE;
    }

    private List<CfgReconciliationFieldExportExcelDTO> convertExcelList(List<CfgReconciliationFieldExportDTO> sourceList) {
        Map<String, CfgReconciliationTypeEnum> typeMap = Arrays.stream(CfgReconciliationTypeEnum.values())
                .collect(Collectors.toMap(CfgReconciliationTypeEnum::getCode, Function.identity()));

        Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldMap = this.mapErpFieldByUniqueCode(null);

        return sourceList.stream().map(e -> {
            CfgReconciliationTypeEnum cfgReconciliationTypeEnum = typeMap.get(e.getReconciliationType());
            CfgReconciliationFieldExportExcelDTO dto = new CfgReconciliationFieldExportExcelDTO();
            BeanMapperUtils.copy(e, dto);
            // 对账类型
            dto.setReconciliationTypeName(null == cfgReconciliationTypeEnum ? "" : cfgReconciliationTypeEnum.getName());
            // 数大臣
            CfgReconciliationFieldDTO.ErpFieldDropDownDTO fieldDropDownDTO = erpFieldMap.get(CfgReconciliationFieldDTO.ErpFieldDropDownDTO.convertUniqueCode(e.getSourceType(), e.getSourceId()));
            dto.setErpFieldName(null == fieldDropDownDTO ? "" : fieldDropDownDTO.getErpFieldName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/cfgReconciliationFieldTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        CfgReconciliationFieldExcelListener excelListenerUtil = new CfgReconciliationFieldExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), CfgReconciliationFieldImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<CfgReconciliationFieldImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<CfgReconciliationFieldImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<CfgReconciliationFieldImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportCfgReconciliationFieldFile(successList, errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/cfgReconciliationFieldError.xlsx";
        String name = "cfgReconciliationFieldError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
        return Boolean.FALSE;
    }

    @Override
    public Map<String, CfgReconciliationFieldEntity> mapByUniqueCode() {
        List<CfgReconciliationFieldEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.toMap(CfgReconciliationFieldEntity::currentUniqueCode, Function.identity()));
    }

    @Override
    public List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldList(List<String> reconciliationTypeList) {
        List<String> keyList = Arrays.asList(DictBasicEnum.CFG_FIRST_MILE_ERP_FIELD.getType(), DictBasicEnum.CFG_B2C_DECLARE_ERP_FIELD.getType());
        List<CfgReconciliationTypeEnum> queryType = Arrays.stream(CfgReconciliationTypeEnum.values()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(reconciliationTypeList)) {
            queryType = reconciliationTypeList.stream()
                    .map(CfgReconciliationTypeEnum::getByCode)
                    .collect(Collectors.toList());
            keyList = queryType.stream()
                    .filter(Objects::nonNull).map(e -> e.getDictBasicEnum().getType())
                    .collect(Collectors.toList());
        }
        // 查询字典
        List<DictBasicEntity> list = dictBasicService.getByKeyList(keyList);

        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            resultList = list.stream()
                    .map(e -> new CfgReconciliationFieldDTO.ErpFieldDropDownDTO(
                            CfgReconciliationTypeEnum.getByDictBasicEnum(e.getType()).getCode(),
                            e.getName(),
                            SourceTypeEnum.DICT_BASIC.getCode(),
                            e.getId(), e.getCode()
                    )).collect(Collectors.toList());
        }
        Map<String, CfgReconciliationTypeEnum> typeMap = queryType
                .stream()
                .collect(Collectors.toMap(e -> e.getCostAttributionEnum().getCode(), Function.identity()));

        List<String> dictCostAttributionList = queryType
                .stream()
                .map(e -> e.getCostAttributionEnum().getCode())
                .distinct()
                .collect(Collectors.toList());
        // 查询费用管理配置表
        List<TmsCfgCostEntity> costEntityList = tmsCfgCostService.lambdaQuery()
                .in(CollectionUtils.isNotEmpty(dictCostAttributionList), TmsCfgCostEntity::getDictCostAttribution, dictCostAttributionList)
                .list();
        if (CollectionUtils.isNotEmpty(costEntityList)) {
            List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> attrList = costEntityList.stream()
                    .map(e -> {
                        CfgReconciliationTypeEnum cfgReconciliationTypeEnum = typeMap.get(e.getDictCostAttribution());
                        return new CfgReconciliationFieldDTO.ErpFieldDropDownDTO(
                                null == cfgReconciliationTypeEnum ? "" : cfgReconciliationTypeEnum.getCode(),
                                e.getCostName(),
                                SourceTypeEnum.TMS_CFG_COST.getCode(),
                                e.getId(),
                                e.getDictCostCategory()
                        );
                    }).collect(Collectors.toList());
            resultList.addAll(attrList);
        }
        return resultList;
    }

    @Override
    public CfgReconciliationFieldDTO.ViewDTO view(String id) {
        CfgReconciliationFieldEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到对账字段配置数据"));
        CfgReconciliationFieldDTO.ViewDTO data = BeanMapperUtils.map(CfgReconciliationFieldDTO.ViewDTO.class, entity);
        // 设置对账类型
        CfgReconciliationTypeEnum typeEnum = CfgReconciliationTypeEnum.getByCode(entity.getReconciliationType());
        data.setReconciliationTypeName(null == typeEnum ? "" : typeEnum.getName());

        // 设置来源ERP字段名
        Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldNameMap = this.mapErpFieldByUniqueCode(null);

        CfgReconciliationFieldDTO.ErpFieldDropDownDTO dropDownDTO = erpFieldNameMap.get(CfgReconciliationFieldDTO.ErpFieldDropDownDTO.convertUniqueCode(data.getSourceType(), data.getSourceId()));
        data.setErpFieldName(null == dropDownDTO ? "" : dropDownDTO.getErpFieldName());
        return data;
    }

    @Override
    public Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> mapErpFieldByUniqueCode(List<String> reconciliationTypeList) {
        // 数大臣字段配置
        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldNameList = this.erpFieldList(reconciliationTypeList);
        if (CollectionUtils.isEmpty(erpFieldNameList)) {
            return Collections.emptyMap();
        }
        return erpFieldNameList
                .stream()
                .collect(Collectors.toMap(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::combineUniqueCode, Function.identity()));
    }

    @Override
    public List<CfgReconciliationFieldDTO.ErpFieldViewDTO> getByReconciliationType(String reconciliationType) {
        List<CfgReconciliationFieldEntity> list = lambdaQuery()
                .eq(CfgReconciliationFieldEntity::getReconciliationType, reconciliationType)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //费用配置
        List<String> sourceIdList = list.stream().map(CfgReconciliationFieldEntity::getSourceId).distinct().collect(Collectors.toList());
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByIds(sourceIdList);

        //配置字典信息
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.CFG_B2C_DECLARE_ERP_FIELD.getType()));

        List<CfgReconciliationFieldDTO.ErpFieldViewDTO> resultList = new ArrayList<>();
        for (CfgReconciliationFieldEntity fieldEntity : list) {
            CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldViewDTO = BeanMapperUtils.map(CfgReconciliationFieldDTO.ErpFieldViewDTO.class, fieldEntity);
            if (StrUtil.equals(fieldEntity.getSourceType(), SourceTypeEnum.TMS_CFG_COST.getCode())) {
                //费用名称
                TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(obj -> StrUtil.equals(obj.getId(), fieldEntity.getSourceId()))
                        .findFirst()
                        .orElse(null);
                if (null != tmsCfgCostEntity) {
                    erpFieldViewDTO.setErpFieldName(tmsCfgCostEntity.getCostName());
                    erpFieldViewDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                }
            } else {
                //字段名称
                DictBasicEntity dictBasicEntity = dictList.stream().filter(obj -> StrUtil.equals(obj.getId(), fieldEntity.getSourceId())).findFirst().orElse(new DictBasicEntity());
                erpFieldViewDTO.setErpFieldName(dictBasicEntity.getName());
                erpFieldViewDTO.setErpFieldCode(dictBasicEntity.getCode());
            }
            resultList.add(erpFieldViewDTO);
        }
        return resultList;
    }

    @Override
    public List<CfgReconciliationFieldEntity> listByCfgCostIdList(List<String> cfgCostIdList) {
        if (CollectionUtils.isEmpty(cfgCostIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(CfgReconciliationFieldEntity::getSourceId, cfgCostIdList).list();
    }

    @Override
    public LinkedList<String> erpFieldListName(List<String> typeList, boolean nullThrow) {
        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> list = this.erpFieldList(Collections.singletonList(CfgReconciliationTypeEnum.FIRST_MILE.getCode()));
        if (CollectionUtils.isEmpty(list)) {
            if (nullThrow) {
                throw new ServiceException("配置字段缺失，请联系管理员");
            } else {
                return new LinkedList<>();
            }
        }
        // 配置的字段名称列表
        return list.stream()
                .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getErpFieldName)
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public LinkedList<String> thirdFieldListName(List<String> typeList, String supplierId, boolean nullThrow) {
        List<CfgReconciliationFieldEntity> list = listByTypeList(typeList, supplierId);
        if (CollectionUtils.isEmpty(list)) {
            if (nullThrow) {
                throw new ServiceException("配置字段缺失，请联系管理员");
            } else {
                return new LinkedList<>();
            }
        }
        // 配置的字段名称列表
        return list.stream()
                .map(CfgReconciliationFieldEntity::getThirdFieldName)
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * @param typeList
     * @return List<CfgReconciliationFieldEntity>
     * @description:
     * @author Will
     * @date: 2024/4/18 16:11
     */
    @Override
    public List<CfgReconciliationFieldEntity> listByTypeList(List<String> typeList, String supplierId) {
        return lambdaQuery()
                .in(CollectionUtils.isNotEmpty(typeList), CfgReconciliationFieldEntity::getReconciliationType, typeList)
                .eq(StrUtil.isNotBlank(supplierId), CfgReconciliationFieldEntity::getThirdCode, supplierId)
                .eq(CfgReconciliationFieldEntity::getStatus, Boolean.TRUE)
                .list();
    }

    @Override
    public List<BaseDropDownDTO.SupplierDisabledDTO> logisticsSupplierList(List<String> reconciliationTypeList) {
        List<BaseDropDownDTO.SupplierDisabledDTO> resulList = new ArrayList<>();
        if (CollectionUtils.isEmpty(reconciliationTypeList)){
            reconciliationTypeList = Arrays.stream(CfgReconciliationTypeEnum.values())
                    .map(CfgReconciliationTypeEnum::getCode)
                    .collect(Collectors.toList());
        }

        if (reconciliationTypeList.contains(CfgReconciliationTypeEnum.B2C_DECLARE.getCode())) {
            List<TransferLogisticsSupplierEntity> list = transferLogisticsSupplierService.lambdaQuery()
                    .list();
            List<BaseDropDownDTO.SupplierDisabledDTO> currentList = list.stream()
                    .map(e -> new BaseDropDownDTO.SupplierDisabledDTO(e.getSupplierId(), e.getSupplierName(), e.getDisabled()))
                    .distinct()
                    .collect(Collectors.toList());
            resulList.addAll(currentList);
        }
        if (reconciliationTypeList.contains(CfgReconciliationTypeEnum.FIRST_MILE.getCode())) {
            List<LogisticsSupplierEntity> list = logisticsSupplierService.lambdaQuery()
                    .list();
            List<BaseDropDownDTO.SupplierDisabledDTO> currentList = list.stream()
                    .map(e -> new BaseDropDownDTO.SupplierDisabledDTO(e.getSupplierId(), e.getSupplierName(), e.getDisabled()))
                    .distinct()
                    .collect(Collectors.toList());
            resulList.addAll(currentList);
        }
        return resulList.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public PagingVO<CfgReconciliationFieldExportExcelDTO> exportCfgReconciliationField(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto) {
        Page<CfgReconciliationFieldExportDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        // 转换
        List<CfgReconciliationFieldExportExcelDTO> resultList = convertExcelList(page.getRecords());
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    private void handleImportCfgReconciliationFieldFile(List<CfgReconciliationFieldImportExcelDTO> successList, List<CfgReconciliationFieldImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        // 核对类型Map
        Map<String, CfgReconciliationTypeEnum> inStockTypeMap = Arrays.stream(CfgReconciliationTypeEnum.values())
                .collect(Collectors.toMap(CfgReconciliationTypeEnum::getName, Function.identity()));

        // 物流商Map
        Map<String, List<BaseDropDownDTO.SupplierDisabledDTO>> supplierMap = this.logisticsSupplierList(null)
                .stream()
                .collect(Collectors.groupingBy(BaseDropDownDTO.SupplierDisabledDTO::getValue));

        // 数大臣字段配置
        Map<String, List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO>> allErpFieldNameGroupMap = this.erpFieldList(null)
                .stream().collect(Collectors.groupingBy(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getReconciliationType));

        // 已映射的信息(已成功的添加到次Map)
        // 唯一键：一个物流商+同一个数大臣字段仅可创建一个
        // Map<{thirdCode}_{erpField}, 配置Entity>
        Map<String, CfgReconciliationFieldEntity> allEntityMap = this.mapByUniqueCode();

        // 校验和处理
        for (CfgReconciliationFieldImportExcelDTO importExcelDTO : successList) {
            // 核对类型Map
            CfgReconciliationTypeEnum cfgReconciliationTypeEnum = inStockTypeMap.get(importExcelDTO.getReconciliationTypeName());

            // 物流商Map
            List<BaseDropDownDTO.SupplierDisabledDTO> supplierEntities = supplierMap.get(importExcelDTO.getThirdName());
            if (CollectionUtils.isEmpty(supplierEntities)) {
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】物流商不存在", importExcelDTO.getThirdName()));
                errorList.add(importExcelDTO);
                continue;
            }
            BaseDropDownDTO.SupplierDisabledDTO supplierEntity = supplierEntities.stream()
                    .filter(e -> e.getValue().equalsIgnoreCase(importExcelDTO.getThirdName()))
                    .findFirst()
                    .orElse(null);
            if (null == supplierEntity) {
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】物流商不存在", importExcelDTO.getThirdName()));
                errorList.add(importExcelDTO);
                continue;
            }
            // 数大臣字段配置
            List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> currentFieldList = allErpFieldNameGroupMap.get(cfgReconciliationTypeEnum.getCode());
            if (CollectionUtils.isEmpty(currentFieldList)) {
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】 当前对账核对类型数大臣字段不存在", importExcelDTO.getErpFieldName()));
                errorList.add(importExcelDTO);
                continue;
            }
            CfgReconciliationFieldDTO.ErpFieldDropDownDTO erpFieldDTO = currentFieldList.stream()
                    .filter(e -> e.getErpFieldName().equalsIgnoreCase(importExcelDTO.getErpFieldName()))
                    .findFirst()
                    .orElse(null);
            if (null == erpFieldDTO) {
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】数大臣字段不存在", importExcelDTO.getErpFieldName()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (!erpFieldDTO.getReconciliationType().equalsIgnoreCase(cfgReconciliationTypeEnum.getCode())) {
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】数大臣字段不属于【{}】核对类型", importExcelDTO.getErpFieldName(), cfgReconciliationTypeEnum.getName()));
                errorList.add(importExcelDTO);
                continue;
            }

            CfgReconciliationFieldEntity historyEntity = allEntityMap.get(CfgReconciliationFieldEntity.combineUniqueCode(cfgReconciliationTypeEnum.getCode(),
                    supplierEntity.getSupplierId(),
                    erpFieldDTO.getSourceType(),
                    erpFieldDTO.getSourceId()));
            if (null != historyEntity) {
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】【{}】【{}】历史配置已存在",
                        importExcelDTO.getReconciliationTypeName(),
                        importExcelDTO.getThirdName(),
                        importExcelDTO.getErpFieldName()
                ));
                errorList.add(importExcelDTO);
                continue;
            }

            try {
                // 组合DTO
                CfgReconciliationFieldEntity addEntity = CfgReconciliationFieldConverter.INSTANCE.combineAddEntity(importExcelDTO, cfgReconciliationTypeEnum, supplierEntity, erpFieldDTO);
                if (!this.save(addEntity)) {
                    throw new ServiceException("保存处理失败");
                }
            } catch (Exception e) {
                importExcelDTO.setErrorMsg(StrUtil.format("处理异常【{}】", ExceptionUtil.stacktraceToOneLineString(e, 255)));
                errorList.add(importExcelDTO);
            }
        }
    }

    private void fillList(List<CfgReconciliationFieldDTO.PagingVO> records) {
        // 根据sourceType和sourceId查询字段名称
        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldNameList = this.erpFieldList(null);
        Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldNameMap = erpFieldNameList
                .stream()
                .collect(Collectors.toMap(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::combineUniqueCode, Function.identity()));
        // 对账类型
        Map<String, CfgReconciliationTypeEnum> typeEnumMap = Arrays.stream(CfgReconciliationTypeEnum.values())
                .collect(Collectors.toMap(CfgReconciliationTypeEnum::getCode, Function.identity()));

        for (CfgReconciliationFieldDTO.PagingVO record : records) {
            CfgReconciliationTypeEnum cfgReconciliationTypeEnum = typeEnumMap.get(record.getReconciliationType());
            record.setReconciliationTypeName(null == cfgReconciliationTypeEnum ? "" : cfgReconciliationTypeEnum.getName());

            CfgReconciliationFieldDTO.ErpFieldDropDownDTO dropDownDTO = erpFieldNameMap.get(CfgReconciliationFieldDTO.ErpFieldDropDownDTO.convertUniqueCode(record.getSourceType(), record.getSourceId()));
            record.setErpFieldName(null == dropDownDTO ? "" : dropDownDTO.getErpFieldName());
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgReconciliationFieldEntity entity) {
        // 验证数据 & 数据赋值
        if (null == entity.getStatus()) {
            entity.setStatus(true);
        }
        CfgReconciliationTypeEnum typeEnum = CfgReconciliationTypeEnum.getByCode(entity.getReconciliationType());
        if (null == typeEnum) {
            throw new ServiceException("配置核对类型不存在");
        }
        // 当前对账类型是否校验物流商
        if (CfgReconciliationTypeEnum.checkSupplier(typeEnum)) {
            BaseDropDownDTO.SupplierDisabledDTO supplierDisabledDTO = null;
            List<BaseDropDownDTO.SupplierDisabledDTO> supplierEntityList = this.logisticsSupplierList(Collections.singletonList(entity.getReconciliationType()));
            if (CollectionUtils.isNotEmpty(supplierEntityList)) {
                supplierDisabledDTO = supplierEntityList.stream().filter(e -> e.getSupplierId().equals(entity.getThirdCode())).findFirst().orElse(null);
            }
            if (null == supplierDisabledDTO) {
                throw new ServiceException("物流商不存在");
            }
            entity.setThirdName(supplierDisabledDTO.getValue());
        }

        // 设置来源ERP字段名
        Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldNameMap = this.mapErpFieldByUniqueCode(Collections.singletonList(typeEnum.getCode()));
        CfgReconciliationFieldDTO.ErpFieldDropDownDTO dropDownDTO = erpFieldNameMap.get(CfgReconciliationFieldDTO.ErpFieldDropDownDTO.convertUniqueCode(entity.getSourceType(), entity.getSourceId()));
        if (null == dropDownDTO) {
            throw new ServiceException("数大臣ERP字段不存在");
        }
        // 检查历史是否存在
        Integer count = this.lambdaQuery()
                .eq(CfgReconciliationFieldEntity::getReconciliationType, entity.getReconciliationType())
                .eq(CfgReconciliationFieldEntity::getThirdCode, entity.getThirdCode())
                .eq(CfgReconciliationFieldEntity::getThirdName, entity.getThirdName())
                .eq(CfgReconciliationFieldEntity::getSourceType, entity.getSourceType())
                .eq(CfgReconciliationFieldEntity::getSourceId, entity.getSourceId())
                .ne(CfgReconciliationFieldEntity::getId, entity.getId())
                .count();
        if (count > 1) {
            String msg = StrUtil.format("【{}】【{}】【{}】历史配置已存在",
                    typeEnum.getName(),
                    entity.getThirdName(),
                    dropDownDTO.getErpFieldName());
            throw new ServiceException(msg);
        }

    }
}
