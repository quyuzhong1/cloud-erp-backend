package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.TabApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;
import com.erp.model.tms.dto.excel.InitFirstMileAllocationDetailExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.ReconciliationTypeEnum;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.convert.InitFirstMileAllocationConverter;
import com.erp.server.tms.listener.InitFirstMileAllocationDetailExcelListener;
import com.erp.server.tms.mapper.InitFirstMileAllocationMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 期初头程分摊 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
 */
@Slf4j
@Service
public class InitFirstMileAllocationServiceImpl extends SuperServiceImpl<InitFirstMileAllocationMapper, InitFirstMileAllocationEntity> implements InitFirstMileAllocationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private InitFirstMileAllocationDetailService initFirstMileAllocationDetailService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;
    @Lazy
    @Resource
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InitFirstMileAllocationDTO.AddDTO addDTO) {
        InitFirstMileAllocationEntity initFirstMileAllocationEntity = new InitFirstMileAllocationEntity();
        BeanMapperUtils.copy(addDTO, initFirstMileAllocationEntity);
        // 数据处理
        handleData(initFirstMileAllocationEntity);
        log.info("开始新增期初头程分摊");
        boolean save = super.save(initFirstMileAllocationEntity);
        if (!save) {
            throw new ServiceException("期初头程分摊保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "期初头程分摊", initFirstMileAllocationEntity.getCode());
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), initFirstMileAllocationEntity.getId(), "新增操作");
        // 新增明细
        if (!CollectionUtils.isEmpty(addDTO.getDetailList())) {
            List<InitFirstMileAllocationDetailEntity> detailEntityList = BeanMapperUtils.copyList(InitFirstMileAllocationDetailEntity.class, addDTO.getDetailList());
            initFirstMileAllocationDetailService.buildDetail(detailEntityList, initFirstMileAllocationEntity.getId());
        }
        return new BaseResultDTO.AddDTO(initFirstMileAllocationEntity.getId(), initFirstMileAllocationEntity.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InitFirstMileAllocationDTO.UpdateDTO updateDTO) {
        InitFirstMileAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "期初头程分摊"));

        InitFirstMileAllocationEntity initFirstMileAllocationEntity = BeanMapperUtils.map(InitFirstMileAllocationEntity.class, updateDTO);
        // 数据处理
        handleData(initFirstMileAllocationEntity);
        log.info("编辑 开始修改期初头程分摊数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(initFirstMileAllocationEntity);
        if (!save) {
            throw new ServiceException("期初头程分摊保存失败");
        }
        // 修改明细
        if (!CollectionUtils.isEmpty(updateDTO.getDetailList())) {
            List<InitFirstMileAllocationDetailEntity> detailEntityList = BeanMapperUtils.copyList(InitFirstMileAllocationDetailEntity.class, updateDTO.getDetailList());
            initFirstMileAllocationDetailService.buildDetail(detailEntityList, initFirstMileAllocationEntity.getId());
        }else {
            //明细为空则清空
            initFirstMileAllocationDetailService.removeByMainId(initFirstMileAllocationEntity.getId());
        }
        // 记录主单操作日志
        log.info("编辑 开始记录期初头程分摊日志数据，单号：【{}】", initFirstMileAllocationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), initFirstMileAllocationEntity.getCode(), "期初头程分摊");
        operateLogService.addModuleOperateLogByObj(old, initFirstMileAllocationEntity, ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), initFirstMileAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<InitFirstMileAllocationDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<InitFirstMileAllocationDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<InitFirstMileAllocationDTO.TabListDTO> tabListDTOList = new ArrayList<>(4);
        tabListDTOList.add(InitFirstMileAllocationDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.WAIT_SUBMIT.getCode()).tabFlagName(TabApproveStatusEnum.WAIT_SUBMIT.getName()).count(getTabCount(TabApproveStatusEnum.WAIT_SUBMIT.getCode(), list)).build());
        tabListDTOList.add(InitFirstMileAllocationDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.APPROVE_ING.getCode()).tabFlagName(TabApproveStatusEnum.APPROVE_ING.getName()).count(getTabCount(TabApproveStatusEnum.APPROVE_ING.getCode(), list)).build());
        tabListDTOList.add(InitFirstMileAllocationDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.REJECT.getCode()).tabFlagName(TabApproveStatusEnum.REJECT.getName()).count(getTabCount(TabApproveStatusEnum.REJECT.getCode(), list)).build());
        tabListDTOList.add(InitFirstMileAllocationDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.APPROVE.getCode()).tabFlagName(TabApproveStatusEnum.APPROVE.getName()).count(getTabCount(TabApproveStatusEnum.APPROVE.getCode(), list)).build());
        return tabListDTOList;
    }

    @Override
    public PagingVO<InitFirstMileAllocationDTO.PagingVO> paging(PagingDTO<InitFirstMileAllocationDTO.PagingParamDTO> dto) {
        InitFirstMileAllocationDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InitFirstMileAllocationDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InitFirstMileAllocationDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<InitFirstMileAllocationDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 导出excel
     *
     * @param dto
     * @param response
     * @return
     */
    @Override
    public void exportExcel(InitFirstMileAllocationDTO.PagingParamDTO dto, HttpServletResponse response) {
        dto.setPermissionSql(dto.getPermissionSql());
        List<InitFirstMileAllocationDTO.PagingVO> list = baseMapper.exportList(dto);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 填充字段值
        fillPagingDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/initFirstMileAllocationExport.xlsx";
        String name = "期初头程分摊导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public BatchResultDTO approve(InitFirstMileAllocationEntity entity, String type, String comment, Boolean isNeedProcess) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98006.msg);
        }
        log.info("期初头程分摊记录【{}】，code=【{}】", ApproveTypeEnum.getName(type), entity.getCode());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE.getStatus());
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.REJECT.getStatus());
        } else if (ApproveTypeEnum.CANCEL.getStatus().equals(type)) {
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个期初头程分摊记录【%s】", ApproveTypeEnum.getName(type), entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "审核操作成功");
    }

    @Override
    public BatchResultDTO disApprove(InitFirstMileAllocationEntity entity) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98014.msg);
        }
        log.info("期初头程分摊记录反审核，code=【{}】", entity.getCode());
        //更新单据为待提交
        updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个期初头程分摊记录【%s】", entity.getCode()), ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "反审核操作成功");
    }

    @Override
    public BatchResultDTO cancel(InitFirstMileAllocationEntity entity) {
        return approve(entity, ApproveTypeEnum.CANCEL.getStatus(), "", Boolean.FALSE);
    }

    @Override
    public BatchResultDTO submit(InitFirstMileAllocationEntity entity) {
        //只有待提交状态才能发起提交
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_1029.msg);
        }
        log.info("期初头程分摊记录提交审核，code=【{}】", entity.getCode());
        //更新单据为审核中
        updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("提交审核了一个期初头程分摊记录【%s】", entity.getCode()), ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), entity.getId(), "提交审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "提交审核操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(InitFirstMileAllocationEntity entity) {
        List<InitFirstMileAllocationDetailEntity> detailEntityList = initFirstMileAllocationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        //校验记录是否已被使用 费用分摊是否已使用
        List<FirstMileSkuCostAllocationEntity> firstMileSkuCostAllocationEntityList = firstMileSkuCostAllocationService.listByInitFirstMileDetailIds
                (detailEntityList.stream().map(InitFirstMileAllocationDetailEntity::getId).distinct().collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(firstMileSkuCostAllocationEntityList)){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "期初费用分摊已使用不能删除");
        }
        initFirstMileAllocationDetailService.removeByMainId(entity.getId());
        this.lambdaUpdate().eq(InitFirstMileAllocationEntity::getId, entity.getId()).remove();
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除记录操作成功");
    }

    @Override
    public InitFirstMileAllocationDTO.ViewDTO view(String id) {
        InitFirstMileAllocationEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("期初头程分摊记录不存在");
        }
        InitFirstMileAllocationDTO.ViewDTO viewDTO = new InitFirstMileAllocationDTO.ViewDTO();
        BeanMapperUtils.copy(entity, viewDTO);
        viewDTO.setStatusName(ApproveStatusEnum.getName(viewDTO.getStatus()));
        List<InitFirstMileAllocationDetailEntity> detailEntityList = initFirstMileAllocationDetailService.listByMainIds(Collections.singletonList(id));
        if (!CollectionUtils.isEmpty(detailEntityList)) {
            List<InitFirstMileAllocationDetailDTO.ViewDTO> viewDTOList = InitFirstMileAllocationConverter.INSTANCE.detailToViewDTO(detailEntityList);
            viewDTO.setDetailList(viewDTOList);
        }
        return viewDTO;
    }

    /**
     * 导入excel
     *
     * @param excelFile
     * @param response
     * @return
     */
    @Override
    public InitFirstMileAllocationDTO.ImportDTO importFile(MultipartFile excelFile,List<InitFirstMileAllocationDetailDTO.AddDTO> detailList, HttpServletResponse response) {
        InitFirstMileAllocationDetailExcelListener excelListenerUtil = new InitFirstMileAllocationDetailExcelListener(detailList);
        try {
            EasyExcel.read(excelFile.getInputStream(), InitFirstMileAllocationDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<InitFirstMileAllocationDetailExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        } else if (excelDateList.size() > 5000) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<InitFirstMileAllocationDetailExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<InitFirstMileAllocationDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();

        InitFirstMileAllocationDTO.ImportDTO importDTO = new InitFirstMileAllocationDTO.ImportDTO();
        String url = "";
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "期初错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, InitFirstMileAllocationDetailExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/initFirstMileAllocationDetailTemplate.xlsx";
        String excelName = "期初头程分摊导入模板.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateAndSubmit(InitFirstMileAllocationDTO.UpdateDTO dto) {
        this.update(dto);
        this.submit(this.getById(dto.getId()));
    }

    @Override
    public List<BatchResultDTO> generateReconciliation(InitFirstMileAllocationDTO.ReconciliationDTO dto) {
        List<String> detailIds = dto.getDetailIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<InitFirstMileAllocationDetailEntity> detailEntityList = initFirstMileAllocationDetailService.listByIds(detailIds);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            resultDTOS.add(BatchResultDTO.fail(String.join(",", detailIds), String.join(",", detailIds), StrUtil.format("期初明细【{}】记录不存在", String.join(",", detailIds))));
            return resultDTOS;
        }
        List<String> sourceIds = detailEntityList.stream().map(InitFirstMileAllocationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryEntityList)) {
            resultDTOS.add(BatchResultDTO.fail(String.join(",", sourceIds), String.join(",", sourceIds), StrUtil.format("头程发货单【{}】记录不存在", String.join(",", sourceIds))));
            return resultDTOS;
        }
        //过滤未审核期初账单
        List<String> ids = detailEntityList.stream().map(InitFirstMileAllocationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<InitFirstMileAllocationEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            resultDTOS.add(BatchResultDTO.fail(String.join(",", ids), String.join(",", ids), StrUtil.format("期初【{}】记录不存在", String.join(",", ids))));
            return resultDTOS;
        }
        List<InitFirstMileAllocationEntity> unApproveList = entityList.stream().filter(e -> !ApproveStatusEnum.APPROVE.getStatus().equals(e.getStatus())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unApproveList)) {
            List<String> codeList = unApproveList.stream().map(InitFirstMileAllocationEntity::getCode).distinct().collect(Collectors.toList());
            resultDTOS.add(BatchResultDTO.fail(String.join(",", codeList), String.join(",", codeList), StrUtil.format("期初编号【{}】未审核单据不能下推对账单", String.join(",", codeList))));
            return resultDTOS;
        }
        //整理下推对账单数据
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByOutstockIdList(sourceIds);
        if (sourceIds.size() != logisticsBillEntityList.size()) {
            List<String> existIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getOutstockId).distinct().collect(Collectors.toList());
            List<String> notExistIds = sourceIds.stream().filter(e -> !existIds.contains(e)).distinct().collect(Collectors.toList());
            List<String> notExistCodes = firstMileDeliveryEntityList.stream().filter(e -> !CollectionUtils.isEmpty(notExistIds) && notExistIds.contains(e.getId())).map(FirstMileDeliveryEntity::getCode).distinct().collect(Collectors.toList());
            resultDTOS.add(BatchResultDTO.fail(String.join(",", notExistIds), String.join(",", notExistCodes), StrUtil.format("头程发货单【{}】无关联物流单，请下推物流单后生成对账单", String.join(",", notExistCodes))));
            return resultDTOS;
        }
        // 当前添加的主账单记录
        Map<String, TmsFirstMileReconciliationEntity> currentMainEntityMap = new HashMap<>();
        for (LogisticsBillEntity logisticsBillEntity : logisticsBillEntityList) {
            BatchResultDTO updateResult;
            String id = logisticsBillEntity.getId();
            try {
                updateResult = tmsFirstMileLogisticService.singleGenerateReconciliation(id, dto.getReconciliationId(), dto.getDateList(), currentMainEntityMap, ReconciliationTypeEnum.INIT_PERIOD.getCode());

            } catch (Exception e) {
                log.error("头程对账生成失败", e);
                LogisticsBillEntity entity = tmsFirstMileLogisticService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    updateResult = BatchResultDTO.fail(id, id, "物流单不存在, 头程对账生成失败");
                    resultDTOS.add(updateResult);
                    continue;
                }
                updateResult = BatchResultDTO.fail(id, entity.getTransportNo(), e.getMessage());
            }
            resultDTOS.add(updateResult);
        }
        return resultDTOS;
    }

    private void updateApproveStatusForApprove(List<String> ids, String status) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(status)) {
            return;
        }
        this.lambdaUpdate().in(InitFirstMileAllocationEntity::getId, ids)
                .set(InitFirstMileAllocationEntity::getStatus, status)
                .update();
    }

    /**
     * 根据状态获取分页统计数量
     *
     * @param status
     * @param list
     * @return
     */
    private Integer getTabCount(String status, List<InitFirstMileAllocationDTO.TabListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return MathUtil.ZERO;
        }
        InitFirstMileAllocationDTO.TabListDTO tabListDTO = list.stream().filter(e -> Objects.nonNull(e) && status.equals(e.getTabFlag())).findFirst().orElse(null);
        if (Objects.nonNull(tabListDTO)) {
            return tabListDTO.getCount();
        } else {
            return MathUtil.ZERO;
        }
    }

    private void fillPagingDb(List<InitFirstMileAllocationDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> sourceIds = list.stream().map(InitFirstMileAllocationDTO.PagingVO::getSourceId).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listByOutstockIds(sourceIds);
        Map<String, String> logisticsBillMap = logisticsBillEntityList.stream().collect(Collectors.toMap(LogisticsBillEntity::getOutstockId, LogisticsBillEntity::getId));
        list.forEach(e -> {
            e.setStatusName(ApproveStatusEnum.getName(e.getStatus()));
            if (!logisticsBillMap.isEmpty()) {
                e.setLogisticsBillId(logisticsBillMap.get(e.getSourceId()));
            }
            e.setProductCost(new BigDecimal(e.getProductCost()).stripTrailingZeros().toPlainString());
        });
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(InitFirstMileAllocationEntity initFirstMileAllocationEntity) {
        // 生成单号
        if (StrUtil.isBlank(initFirstMileAllocationEntity.getCode())) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QCFT);
            initFirstMileAllocationEntity.setCode(code);
        }
        if (StrUtil.isBlank(initFirstMileAllocationEntity.getStatus())) {
            initFirstMileAllocationEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
        }
    }
}
