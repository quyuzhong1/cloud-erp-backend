package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.excel.util.CollectionUtils;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.model.wms.dto.VirtualAdjustDetailDTO;
import com.erp.model.wms.dto.excel.VirtualAdjustDetailExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.VirtualAdjustDetailEntity;
import com.erp.model.wms.entity.VirtualAdjustEntity;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.listener.VirtualAdjustDetailExcelListener;
import com.erp.server.wms.mapper.VirtualAdjustMapper;
import com.erp.server.wms.service.VirtualAdjustDetailService;
import com.erp.server.wms.service.VirtualAdjustService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.wms.service.VirtualInventoryTransCoreService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualAdjustDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_ADJUST_REPORT;

/**
 * <p>
 * 虚拟仓调整单主表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
 */
@Slf4j
@Service
public class VirtualAdjustServiceImpl extends SuperServiceImpl<VirtualAdjustMapper, VirtualAdjustEntity> implements VirtualAdjustService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private VirtualAdjustDetailService virtualAdjustDetailService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualAdjustDTO.AddDTO addDTO) {
        VirtualAdjustEntity virtualAdjustEntity = new VirtualAdjustEntity();
        BeanUtil.copyProperties(addDTO,virtualAdjustEntity,"approveStatus");
        // 数据处理
        handleData(virtualAdjustEntity);

        log.info("开始新增虚拟仓调整单主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XNKC);
        virtualAdjustEntity.setCode(code);
        boolean save = super.save(virtualAdjustEntity);
        if(!save) {
            throw new ServiceException("虚拟仓调整单主单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓调整单主单" , virtualAdjustEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), virtualAdjustEntity.getId(), "新增操作");
        //新增明细
        virtualAdjustDetailService.addDetail(virtualAdjustEntity.getId(), addDTO.getDetailList());
        return new BaseResultDTO.AddDTO(virtualAdjustEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualAdjustDTO.UpdateDTO addOrUpdateDTO) {
        VirtualAdjustEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓调整单主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        VirtualAdjustEntity virtualAdjustEntity = new VirtualAdjustEntity();
        BeanUtil.copyProperties(addOrUpdateDTO,virtualAdjustEntity,"approveStatus");
        virtualAdjustEntity.setApproveStatus(old.getApproveStatus());
        // 数据处理
        handleData(virtualAdjustEntity);
        log.info("编辑 开始修改虚拟仓调整单主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualAdjustEntity);
        if(!save) {
            throw new ServiceException("虚拟仓调整单主单保存失败");
        }
        //修改明细数据（包含增删改）（如果有明细的话）
        virtualAdjustDetailService.updateDetail(virtualAdjustEntity.getId(), addOrUpdateDTO.getDetailList());
        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓调整单主单日志数据，单号：【{}】", virtualAdjustEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualAdjustEntity.getCode(), "虚拟仓调整单主单");
        operateLogService.addModuleOperateLogByObj(old, virtualAdjustEntity, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), virtualAdjustEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<VirtualAdjustDTO.ListDTO> paging(PagingDTO<VirtualAdjustDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<VirtualAdjustDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<VirtualAdjustDTO.TabListDTO> tabList(PermissionsDTO param) {
        VirtualAdjustDTO.PagingParamDTO searchParam = new VirtualAdjustDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<VirtualAdjustDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(VirtualAdjustDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new VirtualAdjustDTO.TabListDTO(status,"", 0));
            }
        });
        list.forEach(e -> {
            if(StrUtil.isBlank(e.getTabFlagName())) {
                e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
            }
        });
        list.add(new VirtualAdjustDTO.TabListDTO("all", "全部", list.stream().mapToInt(VirtualAdjustDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public Boolean exportList(VirtualAdjustDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("发票管理", EXPORT_WMS_VIRTUAL_ADJUST_REPORT.getCode(), param);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        VirtualAdjustEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到虚拟仓调整单主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改虚拟仓调整单主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        //启动流程（如果需要的话）
        log.info("提交 开始启动虚拟仓调整单主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录虚拟仓调整单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "虚拟仓调整单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(VirtualAdjustDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(VirtualAdjustDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        VirtualAdjustEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "虚拟仓调整单主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(VirtualAdjustEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.VIRTUAL_ADJUST.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        VirtualAdjustEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到虚拟仓调整单主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //反审核
        InventoryBatchUnApproveDTO batchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.VIRTUAL_ADJUST, Collections.singletonList(entity.getId()));
        //扣虚拟仓库库存
        virtualInventoryTransCoreService.batchUnApprove(batchUnApproveDTO);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "虚拟仓调整单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private void validateDisApprove(VirtualAdjustEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        VirtualAdjustEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到虚拟仓调整单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        //删除明细数据（如果有明细数据的话）
        virtualAdjustDetailService.removeByMainId(id);
        // 删除主单数据
        log.info("删除 开始删除虚拟仓调整单主单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除虚拟仓调整单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "虚拟仓调整单主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除虚拟仓调整单主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        VirtualAdjustEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到虚拟仓调整单主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改虚拟仓调整单主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(VirtualAdjustEntity::getId, id)
            .set(VirtualAdjustEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(VirtualAdjustEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "虚拟仓调整单主单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        VirtualAdjustEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到虚拟仓调整单主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改虚拟仓调整单主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "虚拟仓调整单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_ADJUST.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.VIRTUAL_ADJUST.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, VirtualAdjustEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        if (ApproveStatusEnum.APPROVE.equals(approveStatus)) {
            //虚拟库存调整
            adjustVirtualInventory(entity);
        }
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/virtualAdjustDetailTemplate.xlsx";
        String excelName = "虚拟仓调整明细导入模板.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public VirtualAdjustDTO.ImportDTO importFile(MultipartFile excelFile, List<VirtualAdjustDetailDTO.AddDTO> detailList, HttpServletResponse response) {
        VirtualAdjustDetailExcelListener excelListenerUtil = new VirtualAdjustDetailExcelListener(detailList);
        try {
            EasyExcel.read(excelFile.getInputStream(), VirtualAdjustDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<VirtualAdjustDetailExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        } else if (excelDateList.size() > 5000) {
            throw new ServiceException(ApiError.ERROR_EXCEL_IMPORT_SIZE);
        }
        List<VirtualAdjustDetailExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<VirtualAdjustDetailExcelDTO> successList = excelListenerUtil.getSuccessList();

        VirtualAdjustDTO.ImportDTO importDTO = new VirtualAdjustDTO.ImportDTO();
        String url = "";
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "SKU成本错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, InventorySkuCostDetailExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    private void adjustVirtualInventory(VirtualAdjustEntity entity) {
        List<VirtualAdjustDetailEntity> detailEntityList = virtualAdjustDetailService.listByMainIdList(Collections.singletonList(entity.getId()));
        //构建库存调整参数
        detailEntityList.forEach(e -> {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            if (InventoryStatusEnum.USABLE.getCode().equals(e.getDictInventoryStatus())){
                stockParamDTO.setBusinessType(e.getQty() > 0 ? VirtualInventoryBusinessTypeEnum.IN_USABLE.getCode() : VirtualInventoryBusinessTypeEnum.OUT_USABLE.getCode());
            }else {
                stockParamDTO.setBusinessType(e.getQty() > 0? VirtualInventoryBusinessTypeEnum.FREEZE_IN_USABLE.getCode() : VirtualInventoryBusinessTypeEnum.FREEZE_OUT_USABLE.getCode());
            }
            stockParamDTO.setParamList(Collections.singletonList(getOutInStockDTO(entity, e)));
            virtualInventoryTransCoreService.approve(stockParamDTO);
        });
    }

    private static VirtualInventoryStockDTO.OutInStockDTO getOutInStockDTO(VirtualAdjustEntity entity, VirtualAdjustDetailEntity e) {
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setBillDate(entity.getBillDate());
        outInStockDTO.setSourceId(entity.getId());
        outInStockDTO.setSourceCode(entity.getCode());
        outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_ADJUST);
        outInStockDTO.setSourceDetailId(e.getId());
        outInStockDTO.setSkuId(e.getSkuId());
        outInStockDTO.setSkuNo(e.getSkuNo());
        outInStockDTO.setWarehouseId(e.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(e.getVirtualWarehouseId());
        outInStockDTO.setQty(e.getQty() > 0 ? e.getQty() : -e.getQty());
        return outInStockDTO;
    }

    @Override
    public VirtualAdjustDTO.ViewDTO view(String id) {
        VirtualAdjustEntity virtualAdjustEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到虚拟仓调整单主单数据"));
        VirtualAdjustDTO.ViewDTO data = BeanMapperUtils.map(VirtualAdjustDTO.ViewDTO.class, virtualAdjustEntity);
        // 数据填充处理
        fillOne(data, virtualAdjustEntity);
        List<VirtualAdjustDetailEntity> detailEntityList = virtualAdjustDetailService.listByMainIdList(Collections.singletonList(id));
        List<VirtualAdjustDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(VirtualAdjustDetailDTO.ViewDTO.class, detailEntityList);
        detailList.forEach(e -> {
            e.setDictInventoryStatusName(InventoryStatusEnum.getNameByCode(e.getDictInventoryStatus()));
        });
        data.setDetailList(detailList);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(VirtualAdjustEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.VIRTUAL_ADJUST.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(VirtualAdjustDTO.ViewDTO data, VirtualAdjustEntity virtualAdjustEntity) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatus(virtualAdjustEntity.getApproveStatus().getStatus());
        data.setApproveStatusName(virtualAdjustEntity.getApproveStatus().getName());
        data.setInvalidStatusName(InvalidStatusEnum.getName(virtualAdjustEntity.getInvalidStatus()));
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(VirtualAdjustEntity::getId, id)
            .set(VirtualAdjustEntity::getApproveUserId, userInfo.getUid())
            .set(VirtualAdjustEntity::getApproveUserName, userInfo.getUserName())
            .set(VirtualAdjustEntity::getApproveStatus, approveStatus)
            .set(VirtualAdjustEntity::getApproveTime, LocalDateTime.now())
            .update(new VirtualAdjustEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(VirtualAdjustEntity::getId, id)
            .set(VirtualAdjustEntity::getApproveUserId, "")
            .set(VirtualAdjustEntity::getApproveUserName, "")
            .set(VirtualAdjustEntity::getApproveStatus, approveStatus)
            .set(VirtualAdjustEntity::getApproveTime, null)
            .update(new VirtualAdjustEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(VirtualAdjustEntity::getId, id)
        .set(VirtualAdjustEntity::getApproveStatus, approveStatus)
        .update(new VirtualAdjustEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<VirtualAdjustDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(VirtualAdjustDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDictInventoryStatusName(InventoryStatusEnum.getNameByCode(data.getDictInventoryStatus()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(VirtualAdjustEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualAdjustEntity virtualAdjustEntity) {
        if (Objects.isNull(virtualAdjustEntity.getApproveStatus())) {
            virtualAdjustEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        }
    }
}
