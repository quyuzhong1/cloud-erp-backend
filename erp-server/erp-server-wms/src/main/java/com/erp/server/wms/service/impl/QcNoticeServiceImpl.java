package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.QcNoticeDetailImportExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.QcNoticeDetailExcelListener;
import com.erp.server.wms.mapper.QcNoticeMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_NOTICE_REPORT;

/**
 * <p>
 * 质检通知单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-21
 */
@Slf4j
@Service
public class QcNoticeServiceImpl extends SuperServiceImpl<QcNoticeMapper, QcNoticeEntity> implements QcNoticeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private QcNoticeDetailService qcNoticeDetailService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WmsAttachmentService attachmentService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private QcProductService qcProductService;

    @Resource
    private QcResultService qcResultService;

    @Resource
    private QcReportDetailService qcReportDetailService;

    @Resource
    private QcRemarkService qcRemarkService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private TransferOutService transferOutService;
    @Resource
    private TransferOutDetailService transferOutDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(QcNoticeDTO.AddDTO addDTO) {
        QcNoticeEntity qcNoticeEntity = new QcNoticeEntity();
        BeanMapperUtils.copy(addDTO, qcNoticeEntity);
        log.info("开始新增质检通知单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZJTZ);
        qcNoticeEntity.setCode(code);
        boolean save = super.save(qcNoticeEntity);
        if (!save) {
            throw new ServiceException("质检通知单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "质检通知单", qcNoticeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeEntity.getId(), "新增操作");

        qcNoticeDetailService.add(addDTO, qcNoticeEntity.getId());
        return new BaseResultDTO.AddDTO(qcNoticeEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(QcNoticeDTO.UpdateDTO updateDTO) {
        QcNoticeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "质检通知单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        QcNoticeEntity qcNoticeEntity = BeanMapperUtils.map(QcNoticeEntity.class, updateDTO);

        log.info("编辑 开始修改质检通知单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(qcNoticeEntity);
        if (!save) {
            throw new ServiceException("质检通知单保存失败");
        }

        List<String> warehouseIds = Arrays.asList(old.getQcWarehouseId(), old.getPutawayWarehouseId(), qcNoticeEntity.getQcWarehouseId(), qcNoticeEntity.getPutawayWarehouseId());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);

        Map<String, String> map = warehouseEntities.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        old.setQcWarehouseName(map.get(old.getQcWarehouseId()));
        old.setPutawayWarehouseName(map.get(old.getPutawayWarehouseId()));
        qcNoticeEntity.setQcWarehouseName(map.get(qcNoticeEntity.getQcWarehouseId()));
        qcNoticeEntity.setPutawayWarehouseName(map.get(qcNoticeEntity.getPutawayWarehouseId()));

        // 记录主单操作日志
        log.info("编辑 开始记录质检通知单日志数据，单号：【{}】", qcNoticeEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "质检通知单");
        operateLogService.addModuleOperateLogByObj(old, qcNoticeEntity, ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeEntity.getId(), msg);

        qcNoticeDetailService.update(updateDTO, qcNoticeEntity.getId());

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<QcNoticeDTO.ListDTO> paging(PagingDTO<QcNoticeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<QcNoticeDTO.TabListDTO> tabList(PermissionsDTO param) {
        QcNoticeDTO.PagingParamDTO searchParam = new QcNoticeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<QcNoticeDTO.TabListDTO> list = new ArrayList<>();
        List<QcNoticeDTO.TabListDTO> tabList = baseMapper.tabList(searchParam);
        Map<String, QcNoticeDTO.TabListDTO> tabMap = tabList.stream().collect(Collectors.toMap(QcNoticeDTO.TabListDTO::getTabFlag, t ->t));
        if(tabMap.containsKey(ApproveStatusEnum.WAIT_SUBMIT.getCode())){
            QcNoticeDTO.TabListDTO tabListDTO = tabMap.get(ApproveStatusEnum.WAIT_SUBMIT.getCode());
            tabListDTO.setTabFlagName(ApproveStatusEnum.WAIT_SUBMIT.getName());
            list.add(tabListDTO);
        }else{
            list.add(new QcNoticeDTO.TabListDTO(ApproveStatusEnum.WAIT_SUBMIT.getCode(),ApproveStatusEnum.WAIT_SUBMIT.getName(), 0));
        }

        if(tabMap.containsKey(ApproveStatusEnum.APPROVE_ING.getCode())){
            QcNoticeDTO.TabListDTO tabListDTO = tabMap.get(ApproveStatusEnum.APPROVE_ING.getCode());
            tabListDTO.setTabFlagName(ApproveStatusEnum.APPROVE_ING.getName());
            list.add(tabListDTO);
        }else{
            list.add(new QcNoticeDTO.TabListDTO(ApproveStatusEnum.APPROVE_ING.getCode(),ApproveStatusEnum.APPROVE_ING.getName(), 0));
        }

        if(tabMap.containsKey(ApproveStatusEnum.REJECT.getCode())){
            QcNoticeDTO.TabListDTO tabListDTO = tabMap.get(ApproveStatusEnum.REJECT.getCode());
            tabListDTO.setTabFlagName(ApproveStatusEnum.REJECT.getName());
            list.add(tabListDTO);
        }else{
            list.add(new QcNoticeDTO.TabListDTO(ApproveStatusEnum.REJECT.getCode(),ApproveStatusEnum.REJECT.getName(), 0));
        }

        List<QcNoticeDTO.TabListDTO> tabQcStatusList = baseMapper.tabQcStatusList(searchParam);
        Map<String, QcNoticeDTO.TabListDTO> tabQcStatusMap = tabQcStatusList.stream().collect(Collectors.toMap(QcNoticeDTO.TabListDTO::getTabFlag, t -> t));
        List<String> codeList = QcNoticeStatusEnum.getCodeList();
        for (String code : codeList) {
            if(tabQcStatusMap.containsKey(code)){
                QcNoticeDTO.TabListDTO tabListDTO = tabQcStatusMap.get(code);
                tabListDTO.setTabFlagName(QcNoticeStatusEnum.getByCode(code).getName());
                list.add(tabListDTO);
            }else {
                list.add(new QcNoticeDTO.TabListDTO(code,QcNoticeStatusEnum.getByCode(code).getName(), 0));
            }
        }
        return list;
    }

    @Override
    public void exportList(QcNoticeDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("质检通知单导出", EXPORT_WMS_QC_NOTICE_REPORT.getCode(), param);
    }

    @Override
    public PagingVO<QcNoticeDTO.ListDTO> exportList(PagingDTO<QcNoticeDTO.ExportDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcNoticeDTO.ListDTO> pageData = this.baseMapper.listExport(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        QcNoticeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到质检通知单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改质检通知单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动质检通知单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录质检通知单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(QcNoticeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(QcNoticeDTO.UpdateDTO dto) {
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
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        QcNoticeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        //审核通过时需要校验库存，质检通知数量必须小于等于可用库存，否则审核失败，提示库存不足
        if (Objects.equals(approveType, ApproveTypeEnum.PASS)){
            List<QcNoticeDetailEntity> qcNoticeDetailEntities = qcNoticeDetailService.listByMainIds(Collections.singletonList(dto.getId()));
            List<String> skuIds = qcNoticeDetailEntities.stream().map(QcNoticeDetailEntity::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            //质检仓库下的可用库存
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalDTOS = inventoryService.listSkuInventory(skuIds, entity.getQcWarehouseId(), null, InventoryStatusEnum.USABLE.getCode());

            Map<String, Integer> skuInventoryMap = skuInventoryTotalDTOS.stream().collect(Collectors.toMap(InventoryQtyDTO.SkuInventoryTotalDTO::getSkuId, InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal));
            StringBuffer sb = new StringBuffer();
            for (QcNoticeDetailEntity detail : qcNoticeDetailEntities) {
                String skuId = detail.getSkuId();
                String skuNo = detail.getSkuNo();
                Integer noticeQty = detail.getQcNoticeQty();
                Integer inventoryQty = skuInventoryMap.getOrDefault(skuId, 0);
                if (inventoryQty <= 0 || inventoryQty.intValue() < noticeQty.intValue()) {
                    sb.append(StrUtil.format(ApiError.ERROR_92268.msg, skuNo, noticeQty, inventoryQty));
                    sb.append(";");
                }
            }
            String msg = sb.toString();
            if(StringUtils.isNotBlank(msg)){
                msg = "审核失败，"+msg;
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), msg);
            }
        }

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(QcNoticeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(SourceTypeEnum.QC_NOTICE.getCode());
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
        QcNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        //反审核质检通知单时，需要校验所有的质检单明细的质检状态为待质检，否则提示：【SKU】已质检完成，不允许操作反审核
        List<QcNoticeDetailEntity> qcNoticeDetailEntities = qcNoticeDetailService.listByMainIds(Collections.singletonList(id));
        Map<String, String> map = qcNoticeDetailEntities.stream().collect(Collectors.toMap(QcNoticeDetailEntity::getId, QcNoticeDetailEntity::getSkuNo));

        //质检单
        List<QcInfoEntity> qcInfoEntities = qcInfoService.listQCBySourceIdsAndType(Collections.singletonList(entity.getId()),SourceTypeEnum.QC_NOTICE.getCode());
        List<QcInfoEntity> qcInfoList = qcInfoEntities.stream().filter(e ->
                e.getQcStatus().getCode().equals(QcBillStatusEnum.EXEMPTION.getCode())
                        || e.getQcStatus().getCode().equals(QcBillStatusEnum.FINISH_QC.getCode())
                        || e.getQcStatus().getCode().equals(QcBillStatusEnum.WAIT_RE_QC.getCode())
        ).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(qcInfoList)){
            StringBuffer sb = new StringBuffer();
            for (QcInfoEntity qcInfo : qcInfoList) {
                sb.append(map.get(qcInfo.getSourceDetailId()));
                sb.append(";");
            }
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), StrUtil.format(ApiError.ERROR_92269.msg,sb.toString()));
        }
        //反审核成功后，自动删除待质检的质检单，通知单状态变更为待提交
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(QcNoticeEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        QcNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单数据"));
        // 只有待提交数据允许删除
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException("只有待提交或审核不通过数据支持删除");
        }
        // 删除主单数据
        log.info("删除 开始删除质检通知单主单数据，id：【{}】", id);
        super.removeById(id);
        //删除明细
        qcNoticeDetailService.deleteByMainId(id);
        // 删除日志数据
        log.info("删除 开始删除质检通知单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), entity.getCode(), "删除质检通知单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        QcNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】", id);
        log.info("撤销 开始修改质检通知单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.QC_NOTICE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, QcNoticeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public List<QcNoticeDTO.QcInfoView> generateQcInfoView(List<String> ids) {
        List<QcNoticeEntity> qcNoticeEntities = listByIds(ids);
        if(CollUtil.isEmpty(qcNoticeEntities)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "质检通知单");
        }

        qcNoticeEntities.forEach(e -> {
            if(!e.getApproveStatus().equals(ApproveStatusEnum.APPROVE)){
                throw new ServiceException( ApiError.ERROR_92270);
            }
        });
        return baseMapper.listQcInfoView(ids);
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateQcInfo(List<QcNoticeDTO.QcInfoView> dto) {
        if(CollUtil.isEmpty(dto)){
            throw new ServiceException( ApiError.ERROR_92271);
        }
        LocalDate billDate = LocalDate.now();
        LocalDateTime nowTime = LocalDateTime.now();

        List<String> qcNoticeIdList = dto.stream().map(QcNoticeDTO.QcInfoView::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //质检通知单
        List<QcNoticeEntity> qcNoticeList = listByIds(qcNoticeIdList);
        Map<String, QcNoticeEntity> mainMap = qcNoticeList.stream().collect(Collectors.toMap(QcNoticeEntity::getId, t -> t));
        //质检通知单明细
        List<String> qcNoticeDetailIdList = dto.stream().map(QcNoticeDTO.QcInfoView::getDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<QcNoticeDetailEntity> noticeDetailList = qcNoticeDetailService.listByIds(qcNoticeDetailIdList);
        Map<String, QcNoticeDetailEntity> detailMap = noticeDetailList.stream().collect(Collectors.toMap(QcNoticeDetailEntity::getId, t -> t));
        //不参与本次质检的明细
        List<QcNoticeDetailEntity> leftDetailList = qcNoticeDetailService.lambdaQuery().notIn(QcNoticeDetailEntity::getId, qcNoticeDetailIdList).list();

        //仓库
        List<String> qcWarehouseId = qcNoticeList.stream().map(QcNoticeEntity::getQcWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> putawayWarehouseId = qcNoticeList.stream().map(QcNoticeEntity::getPutawayWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        qcWarehouseId.addAll(putawayWarehouseId);
        List<String> warehouseIdList = qcWarehouseId.stream().distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIdList);
        Map<String, WarehouseEntity> warehouseMap = warehouseEntities.stream().collect(Collectors.toMap(WarehouseEntity::getId, t -> t));

        //质检员
        List<String> userIds = dto.stream().map(QcNoticeDTO.QcInfoView::getQcUserId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<FindUserDTO> userInfoList = sysUserFeign.getUserListByUserIds(userIds);
        Map<String, String> userDepartmentMap = userInfoList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getDepartmentId));

        Map<String, String> userInfoMap = userInfoList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));

        //sku包装信息
        List<String> skuIds = dto.stream().map(QcNoticeDTO.QcInfoView::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ProductVO.ProductPackVO> productPackList = plmTaskFeign.getProductPackBySkuIds(skuIds);
        Map<String, ProductVO.ProductPackVO> productPactMap = productPackList.stream().collect(Collectors.toMap(ProductVO.ProductPackVO::getSkuId, t -> t));
        for (QcNoticeDTO.QcInfoView qcInfoView : dto) {
            StringBuffer sb = new StringBuffer();
            if(!productPactMap.containsKey(qcInfoView.getSkuId())){
                sb.append(qcInfoView.getSkuNo());
                sb.append(";");
            }
            String str = sb.toString();
            if(StringUtils.isNotBlank(str)){
                throw new ServiceException( ApiError.ERROR_92272, str);
            }
        }
        //质检单map
        Map<String, QcInfoEntity> qcInfoMap = new HashMap<>();

        //质检通知单审核通过自动生成质检单
        for (QcNoticeDTO.QcInfoView qcInfoView : dto) {
            if(qcInfoView.getQcGoodQty().equals(0) && qcInfoView.getQcBadQty().equals(0)){
                throw new ServiceException( ApiError.ERROR_92274, qcInfoView.getSkuNo());
            }
            QcInfoDTO.SaveOrUpdateDTO addDto = new QcInfoDTO.SaveOrUpdateDTO();
            //来源
            addDto.setSourceCode(qcInfoView.getCode());
            addDto.setSourceId(qcInfoView.getId());
            addDto.setSourceType(SourceTypeEnum.QC_NOTICE.getCode());
            addDto.setSourceDetailId(qcInfoView.getDetailId());
            //质检日期
            addDto.setQcDate(qcInfoView.getQcDate());
            //质检仓库
            addDto.setWarehouseId(qcInfoView.getQcWarehouseId());
            //质检人
            addDto.setQcUserId(qcInfoView.getQcUserId());
            //质检部门
            addDto.setQcDeptId(userDepartmentMap.get(qcInfoView.getQcUserId()));

            //产品信息
            QcProductDTO.AddDTO qcProduct = new QcProductDTO.AddDTO();
            ProductVO.ProductPackVO productPackVO = productPactMap.get(qcInfoView.getSkuId());
            BeanMapper.copy(productPackVO, qcProduct);
            addDto.setQcProduct(qcProduct);

            //质检信息
            QcResultDTO.AddDTO qcInfo = new QcResultDTO.AddDTO();
            BeanMapper.copy(qcInfoView, qcInfo);
            qcInfo.setTotalQty(qcInfoView.getQcQty());
            qcInfo.setBadDescription(qcInfoView.getBadDesc());
            qcInfo.setQcResult(QcResultEnum.CONFORMITY.getCode());//默认OK
            qcInfo.setHandleModeDict("waitHandle");//默认待定
            qcInfo.setIsInsideQc(Boolean.FALSE);
            qcInfo.setId("");
            //不良图片
            qcInfo.setBadImageNameList(qcInfoView.getAttachNameList());
            qcInfo.setBadImageUrlList(qcInfoView.getAttachUrlList());
            addDto.setQcInfo(qcInfo);
            //新增质检单
            QcInfoEntity qcInfoEntity = qcInfoService.add(addDto);
            qcInfoMap.put(qcInfoView.getDetailId(), qcInfoEntity);
            //完成质检
            BatchResultDTO finish = qcInfoService.finish(qcInfoEntity);
            qcInfoService.lambdaUpdate()
                    .set(QcInfoEntity::getQcStatus, QcBillStatusEnum.FINISH_QC)
                    .set(QcInfoEntity::getQcFinishTime, nowTime)
                    .eq(QcInfoEntity::getId, qcInfoEntity.getId())
                    .update();

            //保持不良图片
            List<String> imageNameList = qcInfo.getBadImageNameList();
            List<String> imageUrlList = qcInfo.getBadImageUrlList();
            if(CollUtil.isNotEmpty(imageNameList) && CollUtil.isNotEmpty(imageUrlList)){
                wmsAttachmentService.batchSave(imageUrlList, imageNameList, SourceTypeEnum.QC_NOTICE.getTableName(), qcInfoView.getDetailId());
            }

            //回写质检通知单
            QcNoticeDetailEntity qcNoticeDetailEntity = detailMap.get(qcInfoView.getDetailId());
            qcNoticeDetailEntity.setQcQty(qcInfoView.getQcQty());
            qcNoticeDetailEntity.setQcGoodQty(qcInfoView.getQcGoodQty());
            qcNoticeDetailEntity.setQcBadQty(qcInfoView.getQcBadQty());
            qcNoticeDetailEntity.setQcDiffQty(qcInfoView.getQcDiffQty());
            qcNoticeDetailEntity.setQcProblemDict(qcInfoView.getQcProblemDict());
            qcNoticeDetailEntity.setQcDate(nowTime);
            qcNoticeDetailEntity.setQcUserId(qcInfoView.getQcUserId());
            qcNoticeDetailEntity.setQcUserName(userInfoMap.getOrDefault(qcInfoView.getQcUserId(),""));
            //该sku已完成质检
            qcNoticeDetailEntity.setQcStatus(QcNoticeStatusEnum.FINISH.getCode());
            //该sku待上架
            qcNoticeDetailEntity.setPutawayStatus(PutawayStatusEnum.WAIT.getCode());
            qcNoticeDetailService.updateById(qcNoticeDetailEntity);
            //等下用来生成分步式调出单
            detailMap.put(qcInfoView.getDetailId(), qcNoticeDetailEntity);
            //等下用于回填主表状态
            leftDetailList.add(qcNoticeDetailEntity);
            //质检通知单日志
            operateLogService.addModuleOperateLogByObj(detailMap.get(qcInfoView.getDetailId()), qcNoticeDetailEntity, ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeDetailEntity.getMainId(),"", StrUtil.format("【%s】", qcNoticeDetailEntity.getSkuNo()));
            // 记录主单完成质检操作
            operateLogService.addModuleOperateLog(StrUtil.format("【{}】完成质检", qcNoticeDetailEntity.getSkuNo()), ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeDetailEntity.getMainId(), "完成质检");
        }
        Map<String, List<QcNoticeDetailEntity>> detailMapByMainId = new ArrayList<>(detailMap.values()).stream().collect(Collectors.groupingBy(QcNoticeDetailEntity::getMainId));

        //以单据维度生成分步式调出单。
        String remark ="关联质检单号【{}】";
        String mainRemark ="质检通知单完成质检自动生成";
        for (QcNoticeEntity qcNoticeEntity : qcNoticeList) {
            //质检仓库不等于上架仓库则不生成调出单
            if(!Objects.equals(qcNoticeEntity.getQcWarehouseId(), qcNoticeEntity.getPutawayWarehouseId())){
                //类型
                WarehouseEntity qcWarehouse = warehouseMap.get(qcNoticeEntity.getQcWarehouseId());
                WarehouseEntity putawayWarehouse = warehouseMap.get(qcNoticeEntity.getPutawayWarehouseId());
                String type = TransferTypeEnum.CROSS_ORG.getCode();
                if(Objects.equals(qcWarehouse.getOrgId(),putawayWarehouse.getOrgId())){
                    type = TransferTypeEnum.IN_ORG.getCode();
                }

                //分步式调出单
                TransferOutDTO.AddDTO addOutDTO = new TransferOutDTO.AddDTO();
                addOutDTO.setSourceId(qcNoticeEntity.getId());
                addOutDTO.setSourceType(SourceTypeEnum.QC_NOTICE.getCode());
                addOutDTO.setSourceCode(qcNoticeEntity.getCode());
                addOutDTO.setType(type);
                addOutDTO.setBillDate(billDate);
                addOutDTO.setOutWarehouseId(qcNoticeEntity.getQcWarehouseId());
                addOutDTO.setInWarehouseId(qcNoticeEntity.getPutawayWarehouseId());
                addOutDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
                addOutDTO.setRemark(mainRemark);

                List<TransferOutDetailDTO.AddDTO> detailList = new ArrayList<>();
                List<QcNoticeDetailEntity> qcNoticeDetailList = detailMapByMainId.get(qcNoticeEntity.getId());
                for (QcNoticeDetailEntity detailEntity : qcNoticeDetailList) {
                    if(detailEntity.getQcGoodQty().intValue() > 0){
                        TransferOutDetailDTO.AddDTO transferOutDetail = new TransferOutDetailDTO.AddDTO();
                        transferOutDetail.setSkuId(detailEntity.getSkuId());
                        transferOutDetail.setQty(detailEntity.getQcGoodQty());
                        String code = qcInfoMap.get(detailEntity.getId()).getCode();
                        transferOutDetail.setRemark(StrUtil.format(remark,code));
                        transferOutDetail.setSourceDetailId(detailEntity.getId());
//                    transferOutDetail.setOutWarehouseLocation("");
                        transferOutDetail.setUnit("Pcs");
                        detailList.add(transferOutDetail);
                    }
                }
                addOutDTO.setDetailList(detailList);
                if(CollUtil.isNotEmpty(detailList)){
                    transferOutService.add(addOutDTO);
                }
            }
        }

        //主表回写
        Map<String, List<QcNoticeDetailEntity>> leftDetailMap = leftDetailList.stream().collect(Collectors.groupingBy(QcNoticeDetailEntity::getMainId));
        for (QcNoticeEntity qcNoticeEntity : qcNoticeList) {
            List<QcNoticeDetailEntity> left = leftDetailMap.get(qcNoticeEntity.getId());
            boolean allMatch = left.stream().allMatch(e -> e.getQcStatus().equals(QcNoticeStatusEnum.FINISH.getCode()));
            LocalDateTime approveTime = qcNoticeEntity.getApproveTime();
            // 求LocalDateTime nowTime 跟 LocalDateTime approveTime 时间差 。  精确到小时，不足30分钟时舍弃，大于等于30时进1
            int hoursDiff = getHoursDiff(approveTime, nowTime);
            qcNoticeEntity.setQcTImeliness(hoursDiff);
            if(allMatch){
                qcNoticeEntity.setQcStatus(QcNoticeStatusEnum.FINISH.getCode());
            }else {
                qcNoticeEntity.setQcStatus(QcNoticeStatusEnum.PART.getCode());
            }
            updateById(qcNoticeEntity);
        }
    }

    @Override
    public int getHoursDiff(LocalDateTime approveTime, LocalDateTime nowTime) {
        // 计算两个时间点之间的分钟差
        long minutesDiff = Duration.between(approveTime, nowTime).toMinutes();
        // 处理分钟差的进位规则
        int hoursDiff = (int) (minutesDiff / 60); // 计算完整小时数
        int remainingMinutes = (int) (minutesDiff % 60); // 剩余分钟数
        if (remainingMinutes >= 30) {
            hoursDiff += 1; // 大于等于 30 分钟时进位
        }
        return hoursDiff;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> cancelQcInfoFinish(List<String> detailIdList) {
        //先判断明细id下生成的分步式调出单的情况
        List<QcNoticeDetailEntity> qcNoticeDetailEntities = qcNoticeDetailService.listByIds(detailIdList);
        Map<String, List<QcNoticeDetailEntity>> qcNoticeDetailMap = qcNoticeDetailEntities.stream().collect(Collectors.groupingBy(QcNoticeDetailEntity::getMainId));

        List<String> mainIdList = qcNoticeDetailEntities.stream().map(QcNoticeDetailEntity::getMainId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

        Map<String, QcNoticeEntity> noticeMap = listByIds(mainIdList).stream().collect(Collectors.toMap(QcNoticeEntity::getId, t -> t));

        List<TransferOutEntity> transferOutList = transferOutService.listBySourceIds(mainIdList);

        Map<String, List<TransferOutEntity>> transferOutMap = transferOutList.stream().collect(Collectors.groupingBy(TransferOutEntity::getSourceId));

        List<BatchResultDTO> results = new ArrayList<>();

        for (String qcNoticeId : mainIdList) {
            if(noticeMap.containsKey(qcNoticeId)){
                QcNoticeEntity qcNoticeEntity = noticeMap.get(qcNoticeId);
                List<QcNoticeDetailEntity> qcNoticeDetailList = qcNoticeDetailMap.get(qcNoticeId);

                // 判断是否有未质检的
                List<QcNoticeDetailEntity> waitList = qcNoticeDetailList.stream().filter(e -> e.getQcStatus().equals(QcNoticeStatusEnum.WAIT.getCode())).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(waitList)){
                    String skuNos = waitList.stream().map(QcNoticeDetailEntity::getSkuNo).collect(Collectors.joining("，"));
                    BatchResultDTO fail = BatchResultDTO.fail(qcNoticeEntity.getId(), qcNoticeEntity.getCode(), StrUtil.format(ApiError.ERROR_92277.msg, qcNoticeEntity.getCode(),skuNos));
                    results.add(fail);
                    continue;
                }
                //调入调出仓库是同一个，则不存在调出单
                if(Objects.equals(qcNoticeEntity.getQcWarehouseId(),qcNoticeEntity.getPutawayWarehouseId())){
                    List<QcNoticeDetailEntity> qcNoticeDetail = qcNoticeDetailMap.get(qcNoticeEntity.getId());

                    List<String> detailIds = qcNoticeDetail.stream()
                            .map(QcNoticeDetailEntity::getId)
                            .collect(Collectors.toList());
                    cancelQcNotice(qcNoticeEntity, detailIds);

                    for (QcNoticeDetailEntity qcNoticeDetailEntity : qcNoticeDetail) {
                        // 记录撤销质检操作
                        operateLogService.addModuleOperateLog(StrUtil.format("【{}】撤销质检", qcNoticeDetailEntity.getSkuNo()), ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeEntity.getId(), "撤销质检");
                    }
                    BatchResultDTO success = BatchResultDTO.success(qcNoticeEntity.getId(), qcNoticeEntity.getCode(), "撤销质检");
                    results.add(success);
                }else {
                    if(!transferOutMap.containsKey(qcNoticeId)){
                        if(CollUtil.isNotEmpty(qcNoticeDetailList)){
                            List<String> qcNoticeDetailIdList = qcNoticeDetailList.stream().map(QcNoticeDetailEntity::getId).collect(Collectors.toList());
                            //撤销质检通知单状态
                            cancelQcNotice(qcNoticeEntity,qcNoticeDetailIdList);
                            BatchResultDTO success = BatchResultDTO.success(qcNoticeEntity.getId(), qcNoticeEntity.getCode(), "撤销质检");
                            results.add(success);
                        }else {
                            BatchResultDTO fail = BatchResultDTO.fail(qcNoticeEntity.getId(), qcNoticeEntity.getCode(), StrUtil.format(ApiError.ERROR_92275.msg, qcNoticeEntity.getCode()));
                            results.add(fail);
                        }
                    }else {
                        for (TransferOutEntity transferOutEntity : transferOutMap.get(qcNoticeId)) {
                            if(transferOutEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getCode())
                                    || transferOutEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                                BatchResultDTO fail = BatchResultDTO.fail(qcNoticeEntity.getId(), qcNoticeEntity.getCode(), StrUtil.format(ApiError.ERROR_92276.msg, qcNoticeEntity.getCode(),transferOutEntity.getCode()));
                                results.add(fail);
                            }else {
                                List<TransferOutDetailEntity> transferOutDetailEntities = transferOutDetailService.listByMainId(transferOutEntity.getId());

                                //判断transferOutDetailEntities中的sourceDetailId是否在detailIdList
                                boolean anyMatch = transferOutDetailEntities.stream().anyMatch(t -> detailIdList.contains(t.getSourceDetailId()));
                                if(Boolean.TRUE.equals(anyMatch)){
                                    //执行 删除分步式调出单
                                    transferOutDetailService.removeByMainIds(Collections.singletonList(transferOutEntity.getId()));
                                    transferOutService.removeById(transferOutEntity.getId());
                                    //执行 删除关联的明细的质检单
                                    List<String> qcNoticeDetailIdList = transferOutDetailEntities.stream().map(TransferOutDetailEntity::getSourceDetailId).collect(Collectors.toList());
                                    //撤销质检通知单状态
                                    cancelQcNotice(qcNoticeEntity,qcNoticeDetailIdList);

                                    for (TransferOutDetailEntity transferOutDetailEntity : transferOutDetailEntities) {
                                        // 记录撤销质检操作
                                        operateLogService.addModuleOperateLog(StrUtil.format("【{}】撤销质检", transferOutDetailEntity.getSkuNo()), ModuleTypeEnum.QC_NOTICE.getCode(), transferOutEntity.getSourceId(), "撤销质检");
                                    }
                                    BatchResultDTO success = BatchResultDTO.success(qcNoticeEntity.getId(), qcNoticeEntity.getCode(), "撤销质检");
                                    results.add(success);
                                }
                            }
                        }
                    }
                }
            }


        }
        return results;
    }

    //撤销质检通知单状态
    private void cancelQcNotice(QcNoticeEntity qcNoticeEntity, List<String> detailIds ) {
        List<QcInfoEntity> qcInfoEntities = qcInfoService.listQCBySourceDetailIds(detailIds);
        List<String> qcInfoIdList = qcInfoEntities.stream().map(QcInfoEntity::getId).collect(Collectors.toList());
        //删除质检单以及其明细
        deleteQcInfo(qcInfoIdList);

        qcNoticeDetailService.lambdaUpdate()
                .set(QcNoticeDetailEntity::getQcStatus, QcNoticeStatusEnum.WAIT.getCode())
                .set(QcNoticeDetailEntity::getQcQty, 0)
                .set(QcNoticeDetailEntity::getQcDiffQty, 0)
                .set(QcNoticeDetailEntity::getQcGoodQty, 0)
                .set(QcNoticeDetailEntity::getQcBadQty, 0)
                .set(QcNoticeDetailEntity::getQcDate, null)
                .in(QcNoticeDetailEntity::getId, detailIds)
                .update();
        //查询其余明细的质检状态,是否有包含任一的质检完成状态
        boolean anyMatch = qcNoticeDetailService.lambdaQuery()
                .notIn(QcNoticeDetailEntity::getId, detailIds)
                .eq(QcNoticeDetailEntity::getMainId, qcNoticeEntity.getId())
                .list()
                .stream().anyMatch(e-> e.getQcStatus().equals(QcNoticeStatusEnum.FINISH.getCode()));

        //撤销质检通知单状态
        if(anyMatch){
            lambdaUpdate()
                    .set(QcNoticeEntity::getQcStatus, QcNoticeStatusEnum.PART.getCode())
                    .set(QcNoticeEntity::getQcTImeliness, this.getHoursDiff(qcNoticeEntity.getApproveTime(),LocalDateTime.now()))
                    .eq(QcNoticeEntity::getId, qcNoticeEntity.getId())
                    .update();
        }else {
            lambdaUpdate()
                    .set(QcNoticeEntity::getQcStatus, QcNoticeStatusEnum.WAIT.getCode())
                    .set(QcNoticeEntity::getQcTImeliness, this.getHoursDiff(qcNoticeEntity.getApproveTime(),LocalDateTime.now()))
                    .eq(QcNoticeEntity::getId, qcNoticeEntity.getId())
                    .update();
        }
    }

    @Override
    public List<BatchResultDTO> checkInventory(QcNoticeDTO.AddDTO dto) {
        List<BatchResultDTO> results = new ArrayList<>();
        List<QcNoticeDetailDTO.AddDTO> detailList = dto.getDetailList();
        List<String> skuIds = detailList.stream().map(QcNoticeDetailDTO.AddDTO::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //质检仓库下的可用库存
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalDTOS = inventoryService.listSkuInventory(skuIds, dto.getQcWarehouseId(), null, InventoryStatusEnum.USABLE.getCode());

        Map<String, Integer> skuInventoryMap = skuInventoryTotalDTOS.stream().collect(Collectors.toMap(InventoryQtyDTO.SkuInventoryTotalDTO::getSkuId, InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal));
        for (QcNoticeDetailDTO.AddDTO detail : detailList) {
            String skuId = detail.getSkuId();
            String skuNo = detail.getSkuNo();
            Integer noticeQty = detail.getQcNoticeQty();
            Integer inventoryQty = skuInventoryMap.getOrDefault(skuId, 0);
            if (inventoryQty <= 0 || inventoryQty.intValue() < noticeQty.intValue()) {
                results.add(BatchResultDTO.fail(skuId, skuNo, StrUtil.format(ApiError.ERROR_92273.msg, noticeQty, inventoryQty)));
            }
        }
        return results;
    }

    @Override
    public QcNoticeDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        QcNoticeDetailExcelListener excelListenerUtil = new QcNoticeDetailExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), QcNoticeDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //导入数据处理
        List<QcNoticeDetailImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<QcNoticeDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<QcNoticeDetailDTO.AddDTO> resultList = new ArrayList<>();

        if(CollUtil.isNotEmpty(successList)){
            List<String> skuNoList = successList.stream().map(QcNoticeDetailImportExcelDTO::getSkuNo).collect(Collectors.toList());
            List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNoList);

            Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, t -> t, (o1, o2) -> o1));

            for (QcNoticeDetailImportExcelDTO excelDTO : successList) {
                if(!skuMap.containsKey(excelDTO.getSkuNo())){
                    excelDTO.setErrorMsg("1、SKU不存在；");
                    errorList.add(excelDTO);
                    continue;
                }
                SkuVO skuVO = skuMap.get(excelDTO.getSkuNo());
                QcNoticeDetailDTO.AddDTO addDTO = new QcNoticeDetailDTO.AddDTO();
                addDTO.setSkuId(skuVO.getSkuId());
                addDTO.setSkuNo(skuVO.getSkuNo());
                addDTO.setProductName(skuVO.getSkuName());
                addDTO.setQcNoticeQty(Integer.parseInt(excelDTO.getQcNoticeQty()));
                resultList.add(addDTO);
            }
        }

        QcNoticeDTO.ImportDTO importDTO = new QcNoticeDTO.ImportDTO();
        String url = "";
        if (!CollectionUtils.isEmpty(errorList)) {
            String fileName = "质检通知单明细错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, QcNoticeDetailImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(resultList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }


    //删除质检单以及其明细
    private void deleteQcInfo(List<String> qcInfoIdList) {
        if(CollUtil.isNotEmpty(qcInfoIdList)){
            qcInfoService.lambdaUpdate().in(QcInfoEntity::getId, qcInfoIdList).remove();
            qcProductService.lambdaUpdate().in(QcProductEntity::getMainId, qcInfoIdList).remove();
            qcResultService.lambdaUpdate().in(QcResultEntity::getMainId, qcInfoIdList).remove();
            qcReportDetailService.lambdaUpdate().in(QcReportDetailEntity::getMainId, qcInfoIdList).remove();
            qcRemarkService.lambdaUpdate().in(QcRemarkEntity::getMainId, qcInfoIdList).remove();
        }
    }

    @Override
    public QcNoticeDTO.ViewDTO view(String id) {
        QcNoticeEntity qcNoticeEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单数据"));
        QcNoticeDTO.ViewDTO data = BeanMapperUtils.map(QcNoticeDTO.ViewDTO.class, qcNoticeEntity);

        List<QcNoticeDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(QcNoticeDetailDTO.ViewDTO.class, qcNoticeDetailService.listByMainIds(Arrays.asList(id)));
        data.setDetailList(detailList);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(QcNoticeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.QC_NOTICE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private void fillOne(QcNoticeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        List<String> warehouseIdList = new ArrayList<>();
        warehouseIdList.add(data.getQcWarehouseId());
        warehouseIdList.add(data.getPutawayWarehouseId());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIdList);
        Map<String, String> warehouseMap = warehouseEntities.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        //质检仓库
        data.setQcWarehouseName(warehouseMap.get(data.getQcWarehouseId()));

        //上架仓库
        data.setPutawayWarehouseName(warehouseMap.get(data.getPutawayWarehouseId()));

        //质检类型
        data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));

        //单据状态
        data.setApproveStatusName(data.getApproveStatus().getName());

        if(CollUtil.isNotEmpty(data.getDetailList())){
            List<QcNoticeDetailDTO.ViewDTO> detailList = data.getDetailList();
            List<String> ids = detailList.stream().map(QcNoticeDetailDTO.ViewDTO::getId).collect(Collectors.toList());
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(ids);
            Map<String, List<WmsAttachmentDTO.UpdateDTO>> listMap = attachmentList.stream().collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getBusinessId));

            for (QcNoticeDetailDTO.ViewDTO dto : detailList) {
                if(listMap.containsKey(dto.getId())){
                    List<WmsAttachmentDTO.UpdateDTO> updateDTOS = listMap.get(dto.getId());
                    List<String> imageUrls = updateDTOS.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
                    List<String> imageNames = updateDTOS.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
                    dto.setAttachNameList(imageNames);
                    dto.setAttachUrlList(imageUrls);
                }

            }
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(QcNoticeEntity::getId, id)
                .set(QcNoticeEntity::getApproveUserId, userInfo.getUid())
                .set(QcNoticeEntity::getApproveUserName, userInfo.getUserName())
                .set(QcNoticeEntity::getApproveStatus, approveStatus)
                .set(QcNoticeEntity::getApproveTime, LocalDateTime.now())
                .update(new QcNoticeEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(QcNoticeEntity::getId, id)
                .set(QcNoticeEntity::getApproveUserId, "")
                .set(QcNoticeEntity::getApproveUserName, "")
                .set(QcNoticeEntity::getApproveStatus, approveStatus)
                .set(QcNoticeEntity::getApproveTime, null)
                .update(new QcNoticeEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(QcNoticeEntity::getId, id)
                .set(QcNoticeEntity::getApproveStatus, approveStatus)
                .update(new QcNoticeEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<QcNoticeDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        List<String> ids = list.stream().map(QcNoticeDTO.ListDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //
        List<QcInfoEntity> qcInfoEntities = qcInfoService.listQCBySourceIdsAndType(ids, SourceTypeEnum.QC_NOTICE.getCode());
        Map<String, String> map = qcInfoEntities.stream().collect(Collectors.toMap(QcInfoEntity::getSourceDetailId, QcInfoEntity::getCode, (o1, o2) -> o1));

        List<String> qcWarehouseId = list.stream().map(QcNoticeDTO.ListDTO::getQcWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> putawayWarehouseId = list.stream().map(QcNoticeDTO.ListDTO::getPutawayWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        qcWarehouseId.addAll(putawayWarehouseId);

        List<String> warehouseIdList = qcWarehouseId.stream().distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIdList);
        Map<String, String> warehouseMap = warehouseEntities.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        LocalDateTime nowTime = LocalDateTime.now();
        // 属性赋值
        for (QcNoticeDTO.ListDTO data : list) {
            //质检仓库
            data.setQcWarehouseName(warehouseMap.get(data.getQcWarehouseId()));

            //上架仓库
            data.setPutawayWarehouseName(warehouseMap.get(data.getPutawayWarehouseId()));

            //质检类型
            data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));

            //单据质检状态
            data.setQcStatusName(QcNoticeStatusEnum.getByCode(data.getQcStatus()).getName());

            //单据状态
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

            //sku 质检状态
            data.setQcDetailStatusName(QcNoticeStatusEnum.getByCode(data.getQcDetailStatus()).getName());

            //上架状态 待上架:wait  部分上架：part  已上架：finish
            data.setPutawayStatusName(PutawayStatusEnum.getByCode(data.getPutawayStatus()).getName());

            data.setQcInfoCode(map.getOrDefault(data.getDetailId(), ""));
//            if(!data.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
//                data.setQcTimeliness(this.getHoursDiff(data.getApproveTime(),nowTime));
//            }
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(QcNoticeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }


}
