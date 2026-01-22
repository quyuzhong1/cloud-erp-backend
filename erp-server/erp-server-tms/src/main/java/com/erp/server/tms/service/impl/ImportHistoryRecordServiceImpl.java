package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;
import com.erp.model.tms.enums.ImportHistoryRecordStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.listener.ImportHistoryRecordExcelListener;
import com.erp.server.tms.mapper.ImportHistoryRecordMapper;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.erp.server.tms.service.ImportHistoryRecordService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 导入历史记录表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-01-19
 */
@Slf4j
@Service
public class ImportHistoryRecordServiceImpl extends SuperServiceImpl<ImportHistoryRecordMapper, ImportHistoryRecordEntity> implements ImportHistoryRecordService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;
    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;



    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ImportHistoryRecordDTO.AddDTO addDTO) {
        ImportHistoryRecordEntity importHistoryRecordEntity = new ImportHistoryRecordEntity();
        BeanMapperUtils.copy(addDTO, importHistoryRecordEntity);

        // 数据处理
        handleData(importHistoryRecordEntity);

        log.info("开始新增物流授权单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        importHistoryRecordEntity.setCode(code);
        boolean save = super.save(importHistoryRecordEntity);
        if(!save) {
            throw new ServiceException("物流授权单保存失败");
        }

        return new BaseResultDTO.AddDTO(importHistoryRecordEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ImportHistoryRecordDTO.UpdateDTO addOrUpdateDTO) {
        ImportHistoryRecordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "物流授权单"));
        ImportHistoryRecordEntity importHistoryRecordEntity =  BeanMapperUtils.map(ImportHistoryRecordEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(importHistoryRecordEntity);
        log.info("编辑 开始修改物流授权单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(importHistoryRecordEntity);
        if(!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ImportHistoryRecordDTO.ListDTO> paging(PagingDTO<ImportHistoryRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ImportHistoryRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ImportHistoryRecordEntity importHistoryRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public ImportHistoryRecordDTO.ViewDTO view(String id) {
    ImportHistoryRecordEntity importHistoryRecordEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到物流授权单数据"));
    ImportHistoryRecordDTO.ViewDTO data = BeanMapperUtils.map(ImportHistoryRecordDTO.ViewDTO.class, importHistoryRecordEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    @Override
    public BatchResultDTO preprocessingImportExcel(BaseDTO.ImportDTO importDTO,ImportHistoryRecordDTO.ImportDTO dto) {
        if (CharSequenceUtil.isNotBlank(importDTO.getFileName())) {
            throw new ServiceException(ApiError.LOGISTICS_IMPORT_FILE_NAME_NOT_FOUND);
        }
        //查询配置主表信息
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(importDTO.getFileName(), dto.getBusinessType(), dto.getCostType());
        if (CollUtil.isEmpty(cfgLogisticsCostImportList)) {
            return new BatchResultDTO(importDTO.getTaskId(),importDTO.getFileName(),"未找到配置信息",Boolean.TRUE);
        }
        //查询配置明细信息
        List<String> mainIdList = cfgLogisticsCostImportList.stream().map(CfgLogisticsCostImportEntity::getId).distinct().collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> importDetailList =  cfgLogisticsCostImportDetailService.listByMainIdList(mainIdList);
        if (CollUtil.isEmpty(importDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
        }
        Map<String,List<CfgLogisticsCostImportDetailEntity>> impotyDetailMap = importDetailList.stream().collect(Collectors.groupingBy(CfgLogisticsCostImportDetailEntity::getMainId));

        //下载文件
        byte[] bytes = fileFeign.downloadFile(importDTO.getFileUrl());

        //获取批次号，同一个文件同一次导入用同一个批次号
        String batchNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        dto.setCode(batchNo);

        for (CfgLogisticsCostImportEntity costImportEntity : cfgLogisticsCostImportList) {
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList = impotyDetailMap.get(costImportEntity.getId());
            if (CollUtil.isEmpty(cfgImportDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
            }
            //查询配置的唯一识别号
            List<CfgLogisticsCostImportDetailEntity> cfgDetailList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cfgDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND,importDTO.getFileName());
            }

            ImportHistoryRecordExcelListener excelListenerUtil = new ImportHistoryRecordExcelListener(costImportEntity,cfgImportDetailList,dto,importDTO);
            try {
                EasyExcel.read(new ByteArrayInputStream(bytes), excelListenerUtil)
                        .headRowNumber(costImportEntity.getHeaderRow())
                        .sheet(costImportEntity.getSheetName()).doRead();
            } catch (ExcelCommonException e) {
                log.error("导入格式错误！", e);
                throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
            }
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(importDTO.getTaskId());
            importResultDTO.setCount(excelListenerUtil.getCount());
            //导出错误数据
            List<JSONObject> errorList = excelListenerUtil.getErrorList();
            String url = "";
            if (CollectionUtils.isNotEmpty(errorList)) {
                String fileName = "物流商费用错误数据.xlsx";
                File file = ExcelUtil.customExportUtil(fileName, errorList, excelListenerUtil.getHeadList());
                if (!file.isDirectory()) {
                    url = FastDFSClientUtil.uploadFile(file, fileName);
                }
            }
            importResultDTO.setErrorUrl(url);
            importResultDTO.setFinishTime(LocalDateTime.now());
            importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
            importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            downloadTaskFeign.updateTask(importResultDTO);
        }
        return new BatchResultDTO(importDTO.getTaskId(),importDTO.getFileName(),"成功",Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(ImportHistoryRecordDTO.ImportDTO dto,BaseDTO.ImportDTO importDTO,CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                        List<JSONObject> successList, List<JSONObject> errorList2, List<String> headList, Map<Integer, String> headMap) {
        if (headList.size() != headList.stream().distinct().count()) {
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_HEAD_EXIST);
        }
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //查询配置的唯一键字段
        List<CfgLogisticsCostImportDetailEntity> cfgDetailList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());

        //根据唯一字段进行数据查询




        ImportHistoryRecordDTO.AddDTO addDTO = new ImportHistoryRecordDTO.AddDTO();
        addDTO.setReconciliationMonth(dto.getReconciliationMonth());
        addDTO.setBusinessType(costImportEntity.getBusinessType());
        addDTO.setFileUrl(importDTO.getFileUrl());
        addDTO.setFileName(importDTO.getFileName());
        addDTO.setStatus(ImportHistoryRecordStatusEnum.WAIT_HANDLE.getStatus());
        addDTO.setType(dto.getType());
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        addDTO.setOperationUserId(userInfo.getUid());
    }

    private void fillOne(ImportHistoryRecordDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<ImportHistoryRecordDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(ImportHistoryRecordDTO.ListDTO data : list) {
            //对账月份
            if (CharSequenceUtil.isNotBlank(data.getReconciliationMonth())) {
                data.setReconciliationMonthStr(LocalDateUtil.parseStrToLocalDate(data.getReconciliationMonth()).format(DateTimeFormatter.ofPattern("yyyy年MM月")));
            }
            //处理状态名称
            data.setStatusName(ImportHistoryRecordStatusEnum.getName(data.getStatus()));
        }
   }
}
