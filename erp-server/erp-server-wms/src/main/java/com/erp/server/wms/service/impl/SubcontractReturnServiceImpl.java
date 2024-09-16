package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.wms.dto.SubcontractReturnDTO;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;
import com.erp.model.wms.dto.SubcontractReturnDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.model.wms.entity.SubcontractReturnDetailEntity;
import com.erp.model.wms.entity.SubcontractReturnEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SubcontractReturnMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 委外退料单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@Service
public class SubcontractReturnServiceImpl extends SuperServiceImpl<SubcontractReturnMapper, SubcontractReturnEntity> implements SubcontractReturnService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SubcontractReturnDetailService subcontractReturnDetailService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SubcontractReturnDTO.AddDTO addDTO) {
        SubcontractReturnEntity subcontractReturnEntity = new SubcontractReturnEntity();
        BeanMapperUtils.copy(addDTO, subcontractReturnEntity);

        // 数据处理
        handleData(subcontractReturnEntity);

        log.info("开始新增委外退料单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        subcontractReturnEntity.setCode(code);
        boolean save = super.save(subcontractReturnEntity);
        if(!save) {
            throw new ServiceException("委外退料单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外退料单" , subcontractReturnEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, subcontractReturnEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(subcontractReturnEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SubcontractReturnDTO.UpdateDTO updateDTO) {
        SubcontractReturnEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委外退料单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SubcontractReturnEntity subcontractReturnEntity =  BeanMapperUtils.map(SubcontractReturnEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractReturnEntity);
        log.info("编辑 开始修改委外退料单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractReturnEntity);
        if(!save) {
            throw new ServiceException("委外退料单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录委外退料单日志数据，单号：【{}】", subcontractReturnEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), subcontractReturnEntity.getCode(), "委外退料单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, subcontractReturnEntity, null, subcontractReturnEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SubcontractReturnDTO.ListDTO> paging(PagingDTO<SubcontractReturnDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SubcontractReturnDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SubcontractReturnDTO.TabListDTO> tabList(PermissionsDTO param) {
        SubcontractReturnDTO.PagingParamDTO searchParam = new SubcontractReturnDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SubcontractReturnDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SubcontractReturnDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SubcontractReturnDTO.TabListDTO(status, 0));
        }
        });
        list.add(new SubcontractReturnDTO.TabListDTO("all", list.stream().mapToInt(SubcontractReturnDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(SubcontractReturnDTO.PagingParamDTO param, HttpServletResponse response) {
        List<SubcontractReturnDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/subcontractReturn.xlsx";
        String name = "委外退料单导出";
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
        SubcontractReturnEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到委外退料单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改委外退料单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动委外退料单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录委外退料单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SubcontractReturnDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SubcontractReturnDTO.UpdateDTO dto) {
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
        SubcontractReturnEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SubcontractReturnEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(null);
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
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SubcontractReturnEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除委外退料单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除委外退料单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除委外退料单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改委外退料单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
            .set(SubcontractReturnEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SubcontractReturnEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单", remark);
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改委外退料单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SubcontractReturnEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public List<SubcontractReturnDTO.SubcontractDetailListDTO> listSubcontractDetail(SubcontractReturnDTO.DetailPagingParamDTO dto) {
        List<SubcontractReturnDTO.SubcontractDetailListDTO> resultList = new ArrayList<>();
        //委外订单id
        String sourceId = dto.getSourceId();
        List<SubcontractOrderEntity> subcontractOrderList = scmTaskFeign.listSubcontractOrderByIds(Arrays.asList(sourceId));
        if (CollectionUtil.isEmpty(subcontractOrderList)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        //委外明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByMainIds(Arrays.asList(sourceId));
        if (CollectionUtil.isEmpty(subcontractOrderDetailList)) {
            throw  new ServiceException(ApiError.ERROR_98070);
        }
        //委外父级SKU明细
        List<SubcontractOrderDetailEntity> parentList = subcontractOrderDetailList.stream().filter(obj -> StrUtil.isBlank(obj.getParentId()) && (CollectionUtil.isEmpty(dto.getSkuNoList()) ? Boolean.TRUE : dto.getSkuNoList().contains(obj.getSkuNo()))).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98071);
        }

        //bom信息
        List<String> parentSkuIdList = parentList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIdList);

        //委外子级SKU明细
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98072);
        }

        //已审核发料数量
        List<String> subcontractOrderDetailIdList = childList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());
        List<SubcontractReturnDetailEntity> hasDetailList = subcontractReturnDetailService.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);

        //sku信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuCategoryByIds(parentSkuIdList);

        //即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSubDetailSkuInventoryList(childList);

        for (SubcontractOrderDetailEntity parentDetailEntity : parentList) {
            SubcontractReturnDTO.SubcontractDetailListDTO detailListDTO = new SubcontractReturnDTO.SubcontractDetailListDTO();
            detailListDTO.setSkuId(parentDetailEntity.getSkuId());
            detailListDTO.setSkuNo(parentDetailEntity.getSkuNo());
            detailListDTO.setSourceId(subcontractOrderList.get(0).getId());
            detailListDTO.setSubcontractOrderId(subcontractOrderList.get(0).getId());
            detailListDTO.setParentSourceDetailId(parentDetailEntity.getId());
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(obj -> obj.getSkuId().equals(parentDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95010);
            }
            detailListDTO.setCategoryName(skuVO.getCategoryName());
            detailListDTO.setBrandName(skuVO.getBrandName());
            detailListDTO.setProductName(skuVO.getSkuName());
            detailListDTO.setSpuNo(skuVO.getSpuNo());
            detailListDTO.setStatusName(ProductDetailStatusEnum.getName(skuVO.getStatus()));
            detailListDTO.setImagesUrl(skuVO.getSkuImagesUrl());
            detailListDTO.setSupplierId(parentDetailEntity.getSupplierId());
            detailListDTO.setSupplierName(parentDetailEntity.getSupplierName());
            List<SubcontractOrderDetailEntity> childEntityList = childList.stream().filter(obj -> obj.getParentId().equals(parentDetailEntity.getId())).collect(Collectors.toList());
            List<SubcontractReturnDetailDTO.ListSourceDetailDTO> detailList = new ArrayList<>();
            for (SubcontractOrderDetailEntity  childDetailEntity : childEntityList) {
                SubcontractReturnDetailDTO.ListSourceDetailDTO detailDTO = new SubcontractReturnDetailDTO.ListSourceDetailDTO();
                detailDTO.setSourceId(subcontractOrderList.get(0).getId());
                detailDTO.setSubcontractOrderId(subcontractOrderList.get(0).getId());
                detailDTO.setSourceDetailId(childDetailEntity.getId());
                detailDTO.setSubcontractOrderDetailId(childDetailEntity.getId());
                detailDTO.setParentSourceDetailId(parentDetailEntity.getId());
                detailDTO.setParentSkuId(parentDetailEntity.getSkuId());
                detailDTO.setParentSkuNo(parentDetailEntity.getSkuNo());
                detailDTO.setSkuId(childDetailEntity.getSkuId());
                detailDTO.setSkuNo(childDetailEntity.getSkuNo());
                detailDTO.setWarehouseId(childDetailEntity.getWarehouseId());
                detailDTO.setWarehouseName(childDetailEntity.getWarehouseName());
                detailDTO.setReceiveQty(childDetailEntity.getDeliveryQty());
                detailDTO.setWarehouseLocation(childDetailEntity.getWarehouseLocation());
                WarehouseLocationEntity warehouseLocation = warehouseLocationService.findByWarehouseIdAndCode(childDetailEntity.getWarehouseId(), childDetailEntity.getWarehouseLocation());
                if (Objects.nonNull(warehouseLocation)){
                    detailDTO.setWarehouseLocationName(warehouseLocation.getName());
                }
                //bom信息
                BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream().filter(obj -> obj.getParentSkuId().equals(parentDetailEntity.getSkuId()) && obj.getSkuId().equals(childDetailEntity.getSkuId()))
                        .findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                    throw new ServiceException(ApiError.ERROR_95163);
                }
                detailDTO.setProductName(bomChildrenSkuDTO.getSkuName());
                detailDTO.setBomVersion(bomChildrenSkuDTO.getBomVersion());
                detailDTO.setQuantity(bomChildrenSkuDTO.getQuantity());

                //即时库存
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(detailDTO.getSkuId())
                                && s.getWarehouseId().equals(detailDTO.getWarehouseId())
                                && (StrUtil.isBlank(childDetailEntity.getWarehouseLocation()) ? Boolean.TRUE : childDetailEntity.getWarehouseLocation().equals(s.getWarehouseLocationId())))
                        .mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
                detailDTO.setCurInventoryQty(curInventoryQty);

                //已退料数量
                Integer hasIssueQty = hasDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(detailDTO.getSubcontractOrderDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                        .map(SubcontractReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                detailDTO.setHasIssueQty(hasIssueQty);
                detailList.add(detailDTO);
            }
            detailListDTO.setDetailList(detailList);
            resultList.add(detailListDTO);
        }
        return resultList;
    }

    @Override
    public SubcontractReturnDTO.ViewDTO view(String id) {
        SubcontractReturnEntity subcontractReturnEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外退料单数据"));
        SubcontractReturnDTO.ViewDTO data = BeanMapperUtils.map(SubcontractReturnDTO.ViewDTO.class, subcontractReturnEntity);
        // 数据填充处理
        fillOne(data);
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

    public void startProcess(SubcontractReturnEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(null);
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SubcontractReturnDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
            .set(SubcontractReturnEntity::getApproveUserId, userInfo.getUid())
            .set(SubcontractReturnEntity::getApproveUserName, userInfo.getUserName())
            .set(SubcontractReturnEntity::getApproveStatus, approveStatus)
            .set(SubcontractReturnEntity::getApproveTime, LocalDateTime.now())
            .update(new SubcontractReturnEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
            .set(SubcontractReturnEntity::getApproveUserId, "")
            .set(SubcontractReturnEntity::getApproveUserName, "")
            .set(SubcontractReturnEntity::getApproveStatus, approveStatus)
            .set(SubcontractReturnEntity::getApproveTime, null)
            .update(new SubcontractReturnEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
        .set(SubcontractReturnEntity::getApproveStatus, approveStatus)
        .update(new SubcontractReturnEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractReturnDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(SubcontractReturnDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SubcontractReturnEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractReturnEntity subcontractReturnEntity) {
    // TODO 验证数据 & 数据赋值
    }

    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSubDetailSkuInventoryList(List<SubcontractOrderDetailEntity> childList) {
        //skuId集合
        List<String> skuIdList = childList.stream().map(SubcontractOrderDetailEntity::getSkuId)
                .distinct().collect(Collectors.toList());
        //仓库Id集合
        List<String> warehouseIdList = childList.stream().map(SubcontractOrderDetailEntity::getWarehouseId)
                .distinct().collect(Collectors.toList());
        //仓位集合
        List<String> warehouseLocationList = childList.stream().map(SubcontractOrderDetailEntity::getWarehouseLocation)
                .distinct().collect(Collectors.toList());
        return listSkuInventoryTotalList(skuIdList,warehouseIdList,warehouseLocationList);
    }
    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryTotalList(List<String> skuIdList,List<String> warehouseIdList
            ,List<String> warehouseLocationList) {
        InventoryQtyDTO.SkuInventoryParamDTO paramDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        paramDTO.setWarehouseLocationIdList(warehouseLocationList);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryService.listSkuInventory(paramDTO);
        return skuInventoryTotalList;
    }
}
