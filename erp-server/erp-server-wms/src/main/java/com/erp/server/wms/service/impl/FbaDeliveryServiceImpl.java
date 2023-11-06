package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;
import com.erp.model.wms.dto.FbaDeliveryLogisticsDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.model.wms.entity.FbaDeliveryEntity;
import com.erp.model.wms.entity.FbaDeliveryLogisticsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.FbaDeliveryMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaDeliveryServiceImpl extends SuperServiceImpl<FbaDeliveryMapper, FbaDeliveryEntity> implements FbaDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private FbaDeliveryLogisticsService fbaDeliveryLogisticsService;
    @Autowired
    private FbaDeliveryDetailService fbaDeliveryDetailService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private OmsListingInfoFeign omsListingInfoFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FbaDeliveryDTO.AddDTO addDTO) {
        FbaDeliveryEntity fbaDeliveryEntity = new FbaDeliveryEntity();
        BeanMapperUtils.copy(addDTO, fbaDeliveryEntity);

        // 数据处理
        handleData(fbaDeliveryEntity);

        log.info("开始新增FBA发货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHD);
        fbaDeliveryEntity.setCode(code);
        boolean save = super.save(fbaDeliveryEntity);
        if(!save) {
            throw new ServiceException("FBA发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "FBA发货单" , fbaDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), fbaDeliveryEntity.getId(), "新增操作");
        //新增物流信息
        fbaDeliveryLogisticsService.add(addDTO.getLogisticsObj(), fbaDeliveryEntity.getId(), code);
        //新增详情信息
        fbaDeliveryDetailService.add(addDTO, fbaDeliveryEntity.getId());
        return new BaseResultDTO.AddDTO(fbaDeliveryEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaDeliveryDTO.UpdateDTO updateDTO) {
        FbaDeliveryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA发货单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        FbaDeliveryEntity fbaDeliveryEntity =  BeanMapperUtils.map(FbaDeliveryEntity.class, updateDTO);

        // 数据处理
        handleData(fbaDeliveryEntity);
        log.info("编辑 开始修改FBA发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(fbaDeliveryEntity);
        if(!save) {
            throw new ServiceException("FBA发货单保存失败");
        }
        //新增物流信息
        fbaDeliveryLogisticsService.update(updateDTO.getLogisticsObj(), fbaDeliveryEntity.getId());
        //修改明细数据
        fbaDeliveryDetailService.update(updateDTO, fbaDeliveryEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录FBA发货单日志数据，单号：【{}】", fbaDeliveryEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), fbaDeliveryEntity.getCode(), "FBA发货单");
        operateLogService.addModuleOperateLogByObj(old, fbaDeliveryEntity, ModuleTypeEnum.FBA_DELIVERY.getCode(), fbaDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<FbaDeliveryDTO.ListDTO> paging(PagingDTO<FbaDeliveryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FbaDeliveryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<FbaDeliveryDTO.TabListDTO> tabList(PermissionsDTO param) {
        FbaDeliveryDTO.PagingParamDTO searchParam = new FbaDeliveryDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<FbaDeliveryDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(FbaDeliveryDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new FbaDeliveryDTO.TabListDTO(status, 0));
            }
        });
        list.add(new FbaDeliveryDTO.TabListDTO("all", list.stream().mapToInt(FbaDeliveryDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(FbaDeliveryDTO.ExportDTO param, HttpServletResponse response) {
        List<FbaDeliveryDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/fbaDelivery.xlsx";
        String name = "FBA发货单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        FbaDeliveryEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到FBA发货单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改FBA发货单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动FBA发货单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录FBA发货单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(FbaDeliveryDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO resultAdd = this.add(dto);
        // 提交
        this.submit(resultAdd.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(FbaDeliveryDTO.UpdateDTO dto) {
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
        FbaDeliveryEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA发货单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(FbaDeliveryEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.FBA_DELIVERY.getCode());
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
        FbaDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA发货单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(FbaDeliveryEntity entity) {
        // 已审核支持反审核
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        FbaDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA发货单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除物流信息
        fbaDeliveryLogisticsService.removeByMainIds(Arrays.asList(id));
        // 删除明细数据
        fbaDeliveryDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除FBA发货单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除FBA发货单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), entity.getCode(), "删除FBA发货单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        FbaDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA发货单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改FBA发货单状态数据，id：【{}】", id);
        lambdaUpdate().eq(FbaDeliveryEntity::getId, id)
            .set(FbaDeliveryEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(FbaDeliveryEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA发货单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        FbaDeliveryEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA发货单数据"));
        // 只有审核中的单据允许撤销
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改FBA发货单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.FBA_DELIVERY.getCode());
        revokeDTO.setUserId(commonService.getUserInfo().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, FbaDeliveryEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public FbaDeliveryDTO.ViewDTO view(String id) {
        //发货单主信息
        FbaDeliveryEntity fbaDeliveryEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到FBA发货单数据"));
        FbaDeliveryDTO.ViewDTO data = BeanMapperUtils.map(FbaDeliveryDTO.ViewDTO.class, fbaDeliveryEntity);
        //物流信息
        FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity = fbaDeliveryLogisticsService.listByMainId(id);
        //发货单详情
        List<FbaDeliveryDetailEntity> detailEntityList = fbaDeliveryDetailService.listByMainId(id);
        // 数据填充处理
        fillOne(data, fbaDeliveryLogisticsEntity, detailEntityList);
        // TODO 查询明细数据（如果有的话）
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(FbaDeliveryEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.FBA_DELIVERY.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 处理详情字段
     * @Author Luo_WG
     * @Date 2023/11/3 16:05
     * @param data 返回的界面需要的查询列表数据（已映射主表信息）
     * @param logisticsEntity 物流信息
     * @param detailEntityList 产品详情信息
     * @return void
     **/
    private void fillOne(FbaDeliveryDTO.ViewDTO data, FbaDeliveryLogisticsEntity logisticsEntity, List<FbaDeliveryDetailEntity> detailEntityList) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //获取sku信息
        List<String> skuNoList = detailEntityList.stream().map(FbaDeliveryDetailEntity::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);

        //设置状态中文名称
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

        //映射物流信息
        FbaDeliveryLogisticsDTO.ViewDTO logisticsViewDTO = new FbaDeliveryLogisticsDTO.ViewDTO();
        BeanMapper.copy(logisticsEntity, logisticsViewDTO);
        logisticsViewDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(logisticsViewDTO.getLogisticsMethod()));
        data.setLogisticsView(logisticsViewDTO);

        //查询已发货的货件信息
        List<String> sourceDetailIdList = detailEntityList.stream().map(FbaDeliveryDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<FbaDeliveryDetailEntity> entities = fbaDeliveryDetailService.listBySourceDetailIds(sourceDetailIdList);
        //明细信息
        List<FbaDeliveryDetailDTO.ViewDTO> detailViews = new ArrayList<>();
        for (FbaDeliveryDetailEntity fbaDeliveryDetailEntity : detailEntityList) {
            FbaDeliveryDetailDTO.ViewDTO detailVie = BeanMapperUtils.map(FbaDeliveryDetailDTO.ViewDTO.class, fbaDeliveryDetailEntity);
            //映射产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(fbaDeliveryDetailEntity.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
            detailVie.setProductName(skuVO.getSkuName());

            //获取已出库数量（排除此单出库数量）
            Integer useDeliveryQty = entities.stream()
                    .filter(req -> req.getSourceDetailId().equals(fbaDeliveryDetailEntity.getSourceDetailId()) && !req.getId().equals(fbaDeliveryDetailEntity.getId()))
                    .mapToInt(FbaDeliveryDetailEntity::getDeliveryQty)
                    .sum();
            detailVie.setUseDeliveryQty(useDeliveryQty);
            detailViews.add(detailVie);
        }
        data.setDetailList(detailViews);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().eq(FbaDeliveryEntity::getId, id)
            .set(FbaDeliveryEntity::getApproveUserId, userInfo.getUid())
            .set(FbaDeliveryEntity::getApproveUserName, userInfo.getUserName())
            .set(FbaDeliveryEntity::getApproveStatus, approveStatus)
            .set(FbaDeliveryEntity::getApproveTime, LocalDateTime.now())
            .update(new FbaDeliveryEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(FbaDeliveryEntity::getId, id)
            .set(FbaDeliveryEntity::getApproveUserId, "")
            .set(FbaDeliveryEntity::getApproveUserName, "")
            .set(FbaDeliveryEntity::getApproveStatus, approveStatus)
            .set(FbaDeliveryEntity::getApproveTime, null)
            .update(new FbaDeliveryEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(FbaDeliveryEntity::getId, id)
        .set(FbaDeliveryEntity::getApproveStatus, approveStatus)
        .update(new FbaDeliveryEntity());
    }

    @Override
    public List<FbaDeliveryDTO.GenerateMachineView> generateMachineView(BaseIdsDTO.IdsDTO ids) {
        return null;
    }

    @Override
    public Boolean fbaDeliveryGenerateMachineSave(List<FbaDeliveryDTO.GenerateMachineView> list) {
        return null;
    }

    @Override
    public List<FbaDeliveryDTO.PrintSonItem> printSonItemDetail(BaseIdsDTO.IdsDTO ids) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.DeliverRecordView> listDeliveryRecordBySourceIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeliveryRecordBySourceIds(ids);
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<FbaDeliveryDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(FbaDeliveryDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(FbaDeliveryEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FbaDeliveryEntity fbaDeliveryEntity) {
        //根据仓库id查询仓库信息
        List<String> warehouseIds = new ArrayList<>();
        warehouseIds.add(fbaDeliveryEntity.getDeliveryWarehouseId());
        warehouseIds.add(fbaDeliveryEntity.getDestWarehouseId());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);

        //根据仓库信息获取核算公司
        List<String> orgIds = warehouseEntities.stream().map(req -> req.getOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);


        //设置仓库名称
        String destWarehouseName = warehouseEntities.stream().filter(req -> req.getId().equals(fbaDeliveryEntity.getDestWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        fbaDeliveryEntity.setDestWarehouseName(destWarehouseName);
        WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(fbaDeliveryEntity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
        fbaDeliveryEntity.setDestWarehouseName(warehouseEntity.getName());

        //设置库存组织
        String orgName = accountingCompanyList.stream().filter(d -> d.getId().equals(warehouseEntity.getOrgId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        fbaDeliveryEntity.setInventoryOrgId(warehouseEntity.getOrgId());
        fbaDeliveryEntity.setInventoryOrgName(orgName);
    }
}
