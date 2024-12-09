package com.erp.server.plm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.MouldRefundStatusEnum;
import com.erp.model.plm.enums.SysLogClassPathEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.wms.feign.WarehouseLocationFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.mapper.MouldInfoMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具主表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldInfoServiceImpl extends SuperServiceImpl<MouldInfoMapper, MouldInfoEntity> implements MouldInfoService {

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private MouldDetailService mouldDetailService;

    @Resource
    private SysLogService sysLogService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private MouldStoreLocationService mouldStoreLocationService;

    @Resource
    private MouldProductService mouldProductService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WarehouseLocationFeign warehouseLocationFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private MouldDocInfoService mouldDocInfoService;

    @Resource
    private MouldRefundAgreementService mouldRefundAgreementService;

    @Resource
    private MouldRefundVoucherService mouldRefundVoucherService;

    @Resource
    private MouldRefProductService mouldRefProductService;

    @Resource
    private WorkflowFeign workflowFeign;


    @Override
    public PagingVO<MouldInfoDTO.PagingViewDTO> paging(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {

        Page<MouldInfoDTO.PagingViewDTO> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(page.getRecords());
        return new PagingVO<>(page);
    }

    /**
     * 处理数据
     *
     * @param records 记录
     */
    private void fillList(List<MouldInfoDTO.PagingViewDTO> records) {
        List<String> detailIdList = records.stream().map(MouldInfoDTO.PagingViewDTO::getDetailId).collect(Collectors.toList());
        List<MouldProductEntity> mouldProductList = mouldProductService.listByMouldDetailIdList(detailIdList);
        List<MouldStoreLocationEntity> storeLocationList = mouldStoreLocationService.listByMouldDetailIdList(detailIdList);
        List<String> warehouseIdList = storeLocationList.stream().map(MouldStoreLocationEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationFeign.listByWarehouseIds(warehouseIdList);
        for (MouldInfoDTO.PagingViewDTO record : records) {
            List<MouldProductDTO.ViewDTO> productList = mouldProductList.stream()
                    .filter(v -> v.getMouldDetailId().equals(record.getDetailId()))
                    .map(v -> {
                        MouldProductDTO.ViewDTO viewDTO = new MouldProductDTO.ViewDTO();
                        viewDTO.setId(v.getId());
                        viewDTO.setMouldDetailId(v.getMouldDetailId());
                        viewDTO.setImagesUrl(Arrays.asList(v.getImagesUrl().split(",")));
                        viewDTO.setProductName(v.getProductName());
                        return viewDTO;
                    })
                    .collect(Collectors.toList());
            record.setProductList(productList);
            MouldStoreLocationDTO.ViewDTO viewDTO = storeLocationList.stream()
                    .filter(v -> v.getMouldDetailId().equals(record.getDetailId()))
                    .map(v -> BeanMapperUtils.map(MouldStoreLocationDTO.ViewDTO.class, v))
                    .findFirst()
                    .orElse(new MouldStoreLocationDTO.ViewDTO());
            String warehouseLocationName = warehouseLocationList.stream()
                    .filter(v -> v.getWarehouseId().equals(viewDTO.getWarehouseId()))
                    .filter(v -> v.getCode().equals(viewDTO.getWarehouseLocation()))
                    .map(WarehouseLocationEntity::getName)
                    .findFirst()
                    .orElse(null);
            viewDTO.setWarehouseLocationName(warehouseLocationName);
            record.setStoreLocation(viewDTO);
        }
    }

    @Override
    public MouldInfoDTO.ViewDTO view(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        MouldInfoDTO.ViewDTO viewDTO = BeanMapperUtils.map(MouldInfoDTO.ViewDTO.class, entity);
        List<MouldDetailDTO.ViewDTO> mouldDetailList = mouldDetailService.listByMouldId(id);
        viewDTO.setMouldDetailList(mouldDetailList);
        List<MouldDocInfoDTO.ViewDTO> mouldDocInfoList = mouldDocInfoService.listByMouldId(id);
        viewDTO.setMouldDocInfoList(mouldDocInfoList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO draft(MouldInfoDTO.CommonDTO dto) {
        MouldInfoEntity mouldInfoEntity = new MouldInfoEntity();
        BeanMapperUtils.copy(dto, mouldInfoEntity);
        mouldInfoEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        BasicCategoryEntity category = basicCategoryService.getById(dto.getCategoryId());
        String code = docNoGenHelper.generateMouldCode(category.getCode());
        mouldInfoEntity.setMouldCategoryCode(code);
        boolean save = super.save(mouldInfoEntity);
        if (!save) {
            throw new ServiceException("模具主表保存失败");
        }
        String msg = CharSequenceUtil.format("用户【{}】暂存了单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), mouldInfoEntity.getMouldCategoryCode(), "模具");
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), mouldInfoEntity.getId(), "");
        return BatchResultDTO.success(mouldInfoEntity.getId(), mouldInfoEntity.getMouldCategoryCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO addAndSubmit(MouldInfoDTO.UpdateDTO dto) {
        MouldInfoEntity entity = getById(dto.getId());
        boolean isExit = ObjectUtils.isEmpty(entity);


        //保存基本信息
        MouldInfoEntity mouldInfoEntity = new MouldInfoEntity();
        BeanMapperUtils.copy(dto, mouldInfoEntity);
        BasicCategoryEntity category = basicCategoryService.getById(dto.getCategoryId());
        if (isExit) {
            mouldInfoEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            String code = docNoGenHelper.generateMouldCode(category.getCode());
            mouldInfoEntity.setMouldCategoryCode(code);
        }
        save(mouldInfoEntity);
        mouldDetailService.add(dto.getDetailList(), mouldInfoEntity);
        mouldDocInfoService.add(dto.getDocList(), mouldInfoEntity.getId());
        // 记录操作日志
        String msg = null;
        if (isExit) {
            msg = CharSequenceUtil.format("用户【{}】新增了单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), mouldInfoEntity.getMouldCategoryCode(), "模具");
        }
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), mouldInfoEntity.getId(), "");
        return submit(mouldInfoEntity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("提交 开始启动模具表流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 更新单据审核状态
        log.info("提交 开始修改模具表状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        // 记录操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getMouldCategoryCode(), "模具");
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 修改审核状态
     *
     * @param id     id
     * @param status 状态
     */
    private void updateApproveStatus(String id, String status) {
        lambdaUpdate().eq(MouldInfoEntity::getId, id)
                .set(MouldInfoEntity::getStatus, status)
                .update();
    }

    public void startProcess(MouldInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getMouldCategoryCode());
        startDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
        startDTO.setBusinessName(entity.getMouldCategoryCode());
        String userId = UserContext.getDefaultLoginUser().getUid();
        startDTO.setUserId(userId);
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 审核中的数据允许撤销
        if (!Objects.equals(ApproveStatusEnum.APPROVE_ING.getStatus(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(id);
        revokeDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
        revokeDTO.setUserId(userId);
        workflowFeign.revokeProcess(revokeDTO);
        this.updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        String msg = "模具【{}】撤销流程";
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), "撤销流程");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        MouldInfoEntity entity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getMouldCategoryCode(), "模具", approveType.getName(), dto.getComment());
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);

        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), OperationTypeEnum.approveStatus(approveStatus));

    }


    /**
     * 审核通过流程
     *
     * @param entity 实体
     * @param dto    参数
     */
    private void approveProcess(MouldInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
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


    /**
     * 审核结束处理
     *
     * @param dto    参数
     * @param entity 对象
     */
    public void approveEnd(ApproveOneDTO dto, MouldInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateApproveStatus(entity.getId(), approveStatus.getStatus());
        // 新增sku
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 已审核的数据才可以反审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        this.updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        String msg = "模具【{}】反审核流程";
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), "反审核流程");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 待提交或审核不通过并且未作废允许作废
        if (!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getStatus()))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //已作废数据不支持作废
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("作废 开始修改模具状态数据，id：【{}】", id);
        lambdaUpdate().eq(MouldInfoEntity::getId, id)
                .set(MouldInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(MouldInfoEntity::getInvalidRemark, remark)
                .set(MouldInfoEntity::getInvalidTime, LocalDateTime.now())
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getMouldCategoryCode(), "模具", remark);
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), OperationTypeEnum.INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        MouldDetailEntity detail = mouldDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具明细"));
        MouldInfoEntity entity = super.getByIdOpt(detail.getMainId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        //更新备注
        detail.setRemark(remark);
        mouldDetailService.updateById(detail);
        // 操作日志备注
        String msg = CharSequenceUtil.format("模具【{}】更新了备注，由【{}】更新为【{}】", detail.getMouldNo(), detail.getRemark(), remark);
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(detail.getId(), detail.getMouldNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStoreLocation(String id, MouldInfoDTO.StoreLocationDTO dto) {
        MouldDetailEntity detail = mouldDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具明细"));
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        MouldStoreLocationEntity old = mouldStoreLocationService.getByMouldDetailId(id);

        MouldStoreLocationEntity storeLocation = new MouldStoreLocationEntity();
        storeLocation.setId(old.getId());
        storeLocation.setMouldDetailId(id);
        storeLocation.setWarehouseLocation(dto.getWarehouseLocation());
        storeLocation.setWarehouseId(dto.getWarehouseId());
        mouldStoreLocationService.saveOrUpdate(storeLocation);
        //记录变更日志
        List<WarehouseLocationEntity> locationList = warehouseLocationFeign.listByWarehouseIds(Arrays.asList(dto.getWarehouseId(), old.getWarehouseId()));
        List<WarehouseDTO.UpdateDTO> dtos = wmsTaskFeign.listWarehouseByIds(Arrays.asList(dto.getWarehouseId(), old.getWarehouseId()));
        Map<String, String> warehouseMap = dtos.stream()
                .collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getId, WarehouseDTO.UpdateDTO::getName, (o1, o2) -> o1));
        MouldStoreLocationDTO.ChangeDTO newLocation = BeanMapperUtils.map(MouldStoreLocationDTO.ChangeDTO.class, dto);
        newLocation.setWarehouseName(warehouseMap.get(newLocation.getWarehouseId()));
        newLocation.setWarehouseLocationName(getLocationName(newLocation.getWarehouseId(), newLocation.getWarehouseLocation(), locationList));
        MouldStoreLocationDTO.ChangeDTO oldLocation = BeanMapperUtils.map(MouldStoreLocationDTO.ChangeDTO.class, old);
        oldLocation.setWarehouseName(warehouseMap.get(oldLocation.getWarehouseId()));
        oldLocation.setWarehouseLocationName(getLocationName(oldLocation.getWarehouseId(), oldLocation.getWarehouseLocation(), locationList));
        sysLogService.addSysLogByUpdate(oldLocation, newLocation, String.valueOf(MouldStoreLocationDTO.ChangeDTO.class), entity.getId(), "", CharSequenceUtil.format("模具【{}】的存放位置", detail.getMouldNo()));
        return BatchResultDTO.success(detail.getId(), detail.getMouldNo(), OperationTypeEnum.UPDATE);
    }

    // 获取仓库位置名称
    private String getLocationName(String warehouseId, String warehouseLocation, List<WarehouseLocationEntity> locationList) {
        return locationList.stream()
                .filter(v -> v.getWarehouseId().equals(warehouseId) && v.getCode().equals(warehouseLocation))
                .map(WarehouseLocationEntity::getName)
                .findFirst()
                .orElse("");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateEnableTime(String id, LocalDate enableTime) {
        MouldDetailEntity detail = mouldDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具明细"));
        MouldInfoEntity entity = super.getByIdOpt(detail.getMainId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        //更新启用时间
        detail.setEnableDate(enableTime);
        mouldDetailService.updateById(detail);
        // 操作日志备注
        String msg = CharSequenceUtil.format("模具【{}】更新了启用时间，由【{}】更新为【{}】", detail.getMouldNo(), detail.getEnableDate(), enableTime);
        sysLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(detail.getId(), detail.getMouldNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void export(MouldInfoDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("", null, dto);
    }

    @Override
    public PagingVO<MouldInfoDTO.OrderTrackingViewDTO> orderTracking(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        Page<MouldInfoDTO.OrderTrackingViewDTO> page = baseMapper.orderTracking(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        handlerList(page.getRecords());
        return new PagingVO<>(page);
    }

    private void handlerList(List<MouldInfoDTO.OrderTrackingViewDTO> records) {
        List<String> detailIds = records.stream().map(MouldInfoDTO.OrderTrackingViewDTO::getDetailId).collect(Collectors.toList());

    }

    @Override
    public List<MouldInfoDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<MouldRefundAgreementEntity> list = mouldRefundAgreementService.list();
        Map<String, List<MouldRefundAgreementEntity>> map = list.stream()
                .collect(Collectors.groupingBy(MouldRefundAgreementEntity::getRefundStatus));
        List<MouldInfoDTO.TabListDTO> tabListList = new ArrayList<>();
        //未达量
        tabListList.add(new MouldInfoDTO.TabListDTO(MouldRefundStatusEnum.NOT_REACHED.getCode(), MouldRefundStatusEnum.NOT_REACHED.getName(),
                Optional.ofNullable(map.get(MouldRefundStatusEnum.NOT_REACHED.getCode())).orElse(new ArrayList<>()).size()));
        //待返
        tabListList.add(new MouldInfoDTO.TabListDTO(MouldRefundStatusEnum.TO_BE_RETURNED.getCode(), MouldRefundStatusEnum.TO_BE_RETURNED.getName(),
                Optional.ofNullable(map.get(MouldRefundStatusEnum.TO_BE_RETURNED.getCode())).orElse(new ArrayList<>()).size()));
        //已返
        tabListList.add(new MouldInfoDTO.TabListDTO(MouldRefundStatusEnum.RETURNED.getCode(), MouldRefundStatusEnum.RETURNED.getName(),
                Optional.ofNullable(map.get(MouldRefundStatusEnum.RETURNED.getCode())).orElse(new ArrayList<>()).size()));
        return tabListList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnConfirm(MouldInfoDTO.ReturnConfirmDTO dto) {
        mouldRefundVoucherService.returnConfirm(dto);
        MouldRefundAgreementEntity agreement = Optional.ofNullable(mouldRefundAgreementService.getOne(Wrappers.<MouldRefundAgreementEntity>lambdaQuery()
                        .eq(MouldRefundAgreementEntity::getMouldDetailId, dto.getMouldDetailId())))
                .orElseThrow(() -> new ServiceException("未找到模具数据"));
        agreement.setRefundStatus(MouldRefundStatusEnum.RETURNED.getCode());
        mouldRefundAgreementService.updateById(agreement);
    }

    @Override
    public void refProduct(MouldInfoDTO.RefProductDTO dto) {
        List<MouldRefProductEntity> productList = dto.getRefProductList().stream()
                .map(v -> {
                    MouldRefProductEntity refProduct = BeanMapperUtils.map(MouldRefProductEntity.class, v);
                    refProduct.setMouldDetailId(dto.getMouldDetailId());
                    return refProduct;
                })
                .collect(Collectors.toList());
        mouldRefProductService.saveOrUpdateBatch(productList);
    }

    @Override
    public PagingVO<MouldInfoDTO.OrderTrackingDetailDTO> orderTrackingDetail(MouldInfoDTO.OrderTrackingDetailParamDTO dto) {
        return null;
    }
}
