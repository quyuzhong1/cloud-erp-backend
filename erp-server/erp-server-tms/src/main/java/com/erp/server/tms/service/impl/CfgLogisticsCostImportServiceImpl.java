package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.excel.CfgLogisticsCostExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportCfgTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportCostTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportImportTypeEnum;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.wms.dto.excel.SampleBorrowImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.CfgLogisticsCostExcelListener;
import com.erp.server.tms.mapper.CfgLogisticsCostImportMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 费用项配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@Service
public class CfgLogisticsCostImportServiceImpl extends SuperServiceImpl<CfgLogisticsCostImportMapper, CfgLogisticsCostImportEntity> implements CfgLogisticsCostImportService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;
    @Resource
    private CfgLogisticsCostImportFieldService cfgLogisticsCostImportFieldService;
    @Resource
    private CommonService commonService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FileFeign fileFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsCostImportDTO.AddDTO dto) {
        //校验是否已存在（配置生成单据+平台+识别名称+费用来源+sheet 为唯一）
        isExist(dto.getBusinessType(), dto.getDictPlatform(), dto.getName(), dto.getSheetName(),dto.getCostType(),"");

        dto.setImportType(String.join(",", dto.getImportTypeList()));
        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity = new CfgLogisticsCostImportEntity();
        BeanMapperUtils.copy(dto, cfgLogisticsCostImportEntity);
        log.info("开始新增费用项配置");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FYPZ);
        cfgLogisticsCostImportEntity.setCode(code);
        boolean save = super.save(cfgLogisticsCostImportEntity);
        if(!save) {
            throw new ServiceException("费用项配置保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "费用项配置" , cfgLogisticsCostImportEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(), cfgLogisticsCostImportEntity.getId(), "新增操作");

        String id = cfgLogisticsCostImportEntity.getId();
        //处理明细
        List<CfgLogisticsCostImportDetailDTO.AddDTO> detailList = dto.getDetailList();
        List<String> targetFieldIds = detailList.stream().map(CfgLogisticsCostImportDetailDTO.AddDTO::getTargetFieldId).collect(Collectors.toList());
        List<CfgLogisticsCostImportFieldEntity> cfgLogisticsCostImportFieldEntities = cfgLogisticsCostImportFieldService.listByIds(targetFieldIds);
        Map<String, CfgLogisticsCostImportFieldEntity> fieldMap = cfgLogisticsCostImportFieldEntities.stream().collect(Collectors.toMap(CfgLogisticsCostImportFieldEntity::getId, cfgLogisticsCostImportFieldEntity -> cfgLogisticsCostImportFieldEntity,(o1,o2)->o1));
        for (CfgLogisticsCostImportDetailDTO.AddDTO addDTO : detailList) {
            CfgLogisticsCostImportFieldEntity fieldEntity = fieldMap.get(addDTO.getTargetFieldId());
            if(Objects.nonNull(fieldEntity)){
                addDTO.setTargetField(fieldEntity.getField());
                addDTO.setTargetFieldName(fieldEntity.getFieldName());
                addDTO.setTargetFieldType(fieldEntity.getFieldType());
                addDTO.setMainId(id);
            }
        }
        List<CfgLogisticsCostImportDetailEntity> detailEntityList = BeanMapper.copyList(detailList, CfgLogisticsCostImportDetailEntity.class);
        int i = 0;
        for (CfgLogisticsCostImportDetailEntity cfgLogisticsCostImportDetailEntity : detailEntityList) {
            cfgLogisticsCostImportDetailEntity.setIndex(i++);
        }
        cfgLogisticsCostImportDetailService.saveBatch(detailEntityList);

        return new BaseResultDTO.AddDTO(cfgLogisticsCostImportEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "dto.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsCostImportDTO.UpdateDTO dto) {
        //校验是否已存在（配置生成单据+平台+识别名称+费用来源+sheet 为唯一）
        isExist(dto.getBusinessType(), dto.getDictPlatform(), dto.getName(), dto.getSheetName(),dto.getCostType(),dto.getId());

        CfgLogisticsCostImportEntity old = super.getById(dto.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置"));
        dto.setImportType(String.join(",", dto.getImportTypeList()));
        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity =  BeanMapperUtils.map(CfgLogisticsCostImportEntity.class, dto);
        log.info("编辑 开始修改费用项配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgLogisticsCostImportEntity);
        if(!save) {
            throw new ServiceException("费用项配置保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录费用项配置日志数据，单号：【{}】", cfgLogisticsCostImportEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgLogisticsCostImportEntity.getCode(), "费用项配置");
        operateLogService.addModuleOperateLogByObj(old, cfgLogisticsCostImportEntity, ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(), cfgLogisticsCostImportEntity.getId(), msg);

        String id = dto.getId();
        List<CfgLogisticsCostImportDetailEntity> detailList = BeanMapper.copyList(dto.getDetailList(), CfgLogisticsCostImportDetailEntity.class);
        int i = 0;
        for (CfgLogisticsCostImportDetailEntity cfgLogisticsCostImportDetailEntity : detailList) {
            cfgLogisticsCostImportDetailEntity.setMainId(id);
            cfgLogisticsCostImportDetailEntity.setIndex(i++);
        }

        List<CfgLogisticsCostImportDetailEntity> oldDetailList = cfgLogisticsCostImportDetailService.lambdaQuery().eq(CfgLogisticsCostImportDetailEntity::getMainId, id).list();
        commonService.updateDetail(id,ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(),cfgLogisticsCostImportDetailService, detailList, oldDetailList,Arrays.asList("sourceField","sourceDetailField"));
        return Boolean.TRUE;
    }

    //校验是否已存在（配置生成单据+平台+识别名称+费用来源+sheet 为唯一）
    private void isExist(String businessType,String dictPlatform,String name,String sheetName,String costType,String id) {
        Integer count = lambdaQuery()
                .eq(CfgLogisticsCostImportEntity::getBusinessType, businessType)
                .eq(CfgLogisticsCostImportEntity::getDictPlatform, dictPlatform)
                .eq(CfgLogisticsCostImportEntity::getName, name)
                .eq(CfgLogisticsCostImportEntity::getSheetName, sheetName)
                .eq(CfgLogisticsCostImportEntity::getCostType, costType)
                //若id不为空则作为参数
                .ne(StringUtils.isNotBlank(id), CfgLogisticsCostImportEntity::getId, id)
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.COMMON_HAS_EXIST, "费用配置");
        }
    }

    @Override
    public PagingVO<CfgLogisticsCostImportDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgLogisticsCostImportDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgLogisticsCostImportDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgLogisticsCostImportDTO.PagingParamDTO searchParam = new CfgLogisticsCostImportDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgLogisticsCostImportDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<CfgLogisticsCostImportDTO.TabListDTO> result = new ArrayList<>();
        result.add(new CfgLogisticsCostImportDTO.TabListDTO("all","全部",0));
        CfgLogisticsCostImportDTO.TabListDTO tTabListDTO = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(new CfgLogisticsCostImportDTO.TabListDTO("t", "", 0));
        tTabListDTO.setTabFlagName("启用");
        CfgLogisticsCostImportDTO.TabListDTO fTabListDTO = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(new CfgLogisticsCostImportDTO.TabListDTO("f", "", 0));
        fTabListDTO.setTabFlagName("停用");
        result.add(tTabListDTO);
        result.add(fTabListDTO);
        return result;
    }


    @Override
    public CfgLogisticsCostImportDTO.ViewDTO view(String id) {
        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到费用项配置数据"));
        CfgLogisticsCostImportDTO.ViewDTO data = BeanMapperUtils.map(CfgLogisticsCostImportDTO.ViewDTO.class, cfgLogisticsCostImportEntity);
        // 数据填充处理
        fillOne(data);
        //明细
        List<CfgLogisticsCostImportDetailEntity> detailList = cfgLogisticsCostImportDetailService.lambdaQuery().eq(CfgLogisticsCostImportDetailEntity::getMainId, id).list();
        //detailList根据Integer index字段排序
        detailList.sort(Comparator.comparingInt(CfgLogisticsCostImportDetailEntity::getIndex));
        data.setDetailList(detailList);
        return data;
    }

    private void fillOne(CfgLogisticsCostImportDTO.ViewDTO data) {
        if (Objects.nonNull(data)) {
            //费用配置-配置单据
            List<DictBasicDTO.ViewDTO> dictBasicEntities = dictBasicService.getByKey(DictBasicEnum.CFG_COST_BUSINESSKEY.getType());
            DictBasicDTO.ViewDTO viewDTO = dictBasicEntities.stream().filter(e -> e.getCode().equals(data.getBusinessType())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            // 属性赋值
            data.setBusinessTypeName(viewDTO.getName());

            data.setCfgTypeName(CfgLogisticsCostImportCfgTypeEnum.getName(data.getCfgType()));

            data.setCostTypeName(CfgLogisticsCostImportCostTypeEnum.getName(data.getCostType()));

            String importType = data.getImportType();
            if(StringUtils.isNotBlank(importType)){
                List<String> importTypeList = Arrays.asList(importType.split(","));
                data.setImportTypeList(importTypeList);

                List<String> importTypeNameList = new ArrayList<>();
                for (String s : importTypeList) {
                    importTypeNameList.add(CfgLogisticsCostImportImportTypeEnum.getName(s));
                }
                data.setImportTypeNameList(importTypeNameList);
            }

            String dictPlatformName ="";
            if(Objects.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),data.getCfgType())){
                //物流商
                List<BaseDropDownDTO.DisabledDTO> logisticsSupplierList = logisticsSupplierService.listAll(false);
                BaseDropDownDTO.DisabledDTO disabledDTO = logisticsSupplierList.stream().filter(e -> e.getCode().equals(data.getDictPlatform())).findFirst().orElse(new BaseDropDownDTO.DisabledDTO());
                dictPlatformName = disabledDTO.getValue();
            }else {
                //销售平台
                List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class)
                        .eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
                        .eq(DictBasicEntity::getValue,data.getDictPlatform())
                        .list();
                if(CollUtil.isNotEmpty(salesPlatformList)){
                    dictPlatformName = salesPlatformList.get(0).getName();
                }
            }
            data.setDictPlatformName(dictPlatformName);
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgLogisticsCostImportDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

       //费用配置-配置单据
       List<DictBasicDTO.ViewDTO> dictBasicEntities = dictBasicService.getByKey(DictBasicEnum.CFG_COST_BUSINESSKEY.getType());
       Map<String, String> map = dictBasicEntities.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getCode, DictBasicDTO.ViewDTO::getName, (o1, o2) -> o1));
       //物流商
       List<BaseDropDownDTO.DisabledDTO> logisticsSupplierList = logisticsSupplierService.listAll(false);
       Map<String, String> logisticsSupplierMap = logisticsSupplierList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getCode, BaseDropDownDTO.DisabledDTO::getValue, (o1, o2) -> o1));
       //销售平台
       List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();
       Map<String, String> salesPlatformMap = salesPlatformList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName, (o1, o2) -> o1));
       //目标字段


       // 属性赋值
        for(CfgLogisticsCostImportDTO.ListDTO data : list) {
            data.setBusinessTypeName(map.get(data.getBusinessType()));

            data.setCfgTypeName(CfgLogisticsCostImportCfgTypeEnum.getName(data.getCfgType()));

            data.setCostTypeName(CfgLogisticsCostImportCostTypeEnum.getName(data.getCostType()));

            String importType = data.getImportType();
            if(StringUtils.isNotBlank(importType)){
                data.setImportTypeName(Arrays.stream(importType.split(","))
                        .map(CfgLogisticsCostImportImportTypeEnum::getName)
                        .collect(Collectors.joining(",")));
            }

            String dictPlatformName ="";
            if(Objects.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),data.getCfgType())){
                dictPlatformName = logisticsSupplierMap.get(data.getDictPlatform());
            }else {
                dictPlatformName = salesPlatformMap.get(data.getDictPlatform());
            }
            data.setDictPlatformName(dictPlatformName);

            data.setDisabledName(data.getDisabled() ? "停用" : "启用");
        }
   }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgLogisticsCostImportEntity entity = super.getById(id);
        entity = Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置"));
        entity.setIsDeleted(Boolean.TRUE);
        updateById(entity);
        // 操作日志
        String msg = StrUtil.format("用户【{}】删除【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(),"费用项配置" , entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(), entity.getId(), OperationTypeEnum.DELETE.getName());
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateDisabled(String id,Boolean disabled) {
        CfgLogisticsCostImportEntity entity = super.getById(id);
        entity = Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置"));
        if(!Objects.equals(entity.getDisabled(),disabled)){
            entity.setDisabled(disabled);
            updateById(entity);
            String str = disabled ? "停用" : "启用";
            // 操作日志
            String msg = StrUtil.format("用户【{}】{}【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), str,"费用项配置" , entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(), entity.getId(), OperationTypeEnum.DISABLED.getName());
        }
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DISABLED);
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入费用配置", IMPORT_TMS_CFG_LOGISTICS_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCfgLogisticsCost(BaseDTO.ImportDTO dto) {
        //费用配置-配置单据
        List<DictBasicDTO.ViewDTO> dictBasicEntities = dictBasicService.getByKey(DictBasicEnum.CFG_COST_BUSINESSKEY.getType());
        Map<String, String> dictBasicMap = dictBasicEntities.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getName, DictBasicDTO.ViewDTO::getCode, (o1, o2) -> o1));
        //物流商
        List<BaseDropDownDTO.DisabledDTO> logisticsSupplierList = logisticsSupplierService.listAll(false);
        Map<String, String> logisticsSupplierMap = logisticsSupplierList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getValue, BaseDropDownDTO.DisabledDTO::getCode, (o1, o2) -> o1));
        //销售平台
        List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();
        Map<String, String> salesPlatformMap = salesPlatformList.stream().collect(Collectors.toMap(DictBasicEntity::getName, DictBasicEntity::getValue, (o1, o2) -> o1));
        //目标字段
        List<CfgLogisticsCostImportFieldEntity> list = cfgLogisticsCostImportFieldService.list();
        Map<String, List<CfgLogisticsCostImportFieldEntity>> fieldMap = list.stream().collect(Collectors.groupingBy(CfgLogisticsCostImportFieldEntity::getBusinessType));

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
        CfgLogisticsCostExcelListener excelListenerUtil = new CfgLogisticsCostExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),dictBasicMap,logisticsSupplierMap, salesPlatformMap , fieldMap, userList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SampleBorrowImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<CfgLogisticsCostExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            //排序
            List<CfgLogisticsCostExcelDTO> sortedErrorList = errorList.stream()
                    .filter(e -> e.getNo() != null && !e.getNo().isEmpty()) // 过滤掉 null 或空字符串
                    .sorted(Comparator.comparingInt(e -> {
                        try {
                            return Integer.parseInt(e.getNo());
                        } catch (Exception ex) {
                            // 处理非数字字符串，可以返回一个默认值
                            return 0;
                        }
                    }))
                    .collect(Collectors.toList());
            String fileName = "费用配置错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, CfgLogisticsCostExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<CfgLogisticsCostExcelDTO> successList, List<String> errorNoList, List<CfgLogisticsCostExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if(CollUtil.isNotEmpty(errorNoList)){
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            //全部返回到错误列表
            List<CfgLogisticsCostExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        CfgLogisticsCostImportServiceImpl bean = ApplicationContextUtils.getBean(CfgLogisticsCostImportServiceImpl.class);
        //按序号分组
        Map<String, List<CfgLogisticsCostExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(CfgLogisticsCostExcelDTO::getNo));
        for (Map.Entry<String, List<CfgLogisticsCostExcelDTO>> entry : collect.entrySet()) {
            List<CfgLogisticsCostExcelDTO> value = entry.getValue();
            CfgLogisticsCostExcelDTO importMainDTO = value.get(0);
            CfgLogisticsCostImportDTO.AddDTO addDTO = new CfgLogisticsCostImportDTO.AddDTO();
            BeanMapper.copy(importMainDTO,addDTO);
            List<CfgLogisticsCostImportDetailDTO.AddDTO> addDTOS = BeanMapper.copyList(value, CfgLogisticsCostImportDetailDTO.AddDTO.class);
            addDTO.setDetailList(addDTOS);
            //新增
            bean.add(addDTO);
        }
    }

    @Override
    public void exportList(CfgLogisticsCostImportDTO.PagingParamDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("费用项配置导出", EXPORT_TMS_CFG_LOGISTICS_COST.getCode(), dto);
    }

    @Override
    public List<CfgLogisticsCostImportEntity> listByImport(String fileName, String businessType, String costType) {
        return lambdaQuery().eq(CfgLogisticsCostImportEntity::getName,fileName)
                .eq(CfgLogisticsCostImportEntity::getBusinessType,businessType)
                .eq(CfgLogisticsCostImportEntity::getCostType,costType)
                .eq(CfgLogisticsCostImportEntity::getDisabled,Boolean.FALSE)
                .list();
    }
}
