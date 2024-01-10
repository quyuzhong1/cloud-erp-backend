package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import com.common.business.dto.base.BaseResultDTO;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.ProductDetailStateEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SubcontractIssueMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
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
 * 委外发料单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@Service
public class SubcontractIssueServiceImpl extends SuperServiceImpl<SubcontractIssueMapper, SubcontractIssueEntity> implements SubcontractIssueService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SubcontractIssueDetailService subcontractIssueDetailService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private InventoryService inventoryService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SubcontractIssueDTO.AddDTO addDTO) {
        SubcontractIssueEntity subcontractIssueEntity = new SubcontractIssueEntity();
        BeanMapperUtils.copy(addDTO, subcontractIssueEntity);

        // 数据处理
        handleData(subcontractIssueEntity);

        log.info("开始新增委外发料单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FLD);
        subcontractIssueEntity.setCode(code);
        boolean save = super.save(subcontractIssueEntity);
        if(!save) {
            throw new ServiceException("委外发料单保存失败");
        }
        //新增明细
        subcontractIssueDetailService.add(addDTO.getDetailList(),subcontractIssueEntity.getId());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "委外发料单" , subcontractIssueEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), subcontractIssueEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(subcontractIssueEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SubcontractIssueDTO.UpdateDTO updateDTO) {
        SubcontractIssueEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委外发料单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SubcontractIssueEntity subcontractIssueEntity =  BeanMapperUtils.map(SubcontractIssueEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractIssueEntity);
        log.info("编辑 开始修改委外发料单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractIssueEntity);
        if(!save) {
            throw new ServiceException("委外发料单保存失败");
        }
        //修改明细
        subcontractIssueDetailService.update(updateDTO.getDetailList(),subcontractIssueEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录委外发料单日志数据，单号：【{}】", subcontractIssueEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), subcontractIssueEntity.getCode(), "委外发料单");
        operateLogService.addModuleOperateLogByObj(old, subcontractIssueEntity, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), subcontractIssueEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SubcontractIssueDTO.ListDTO> paging(PagingDTO<SubcontractIssueDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SubcontractIssueDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SubcontractIssueDTO.TabListDTO> tabList(PermissionsDTO param) {
        SubcontractIssueDTO.PagingParamDTO searchParam = new SubcontractIssueDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SubcontractIssueDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SubcontractIssueDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SubcontractIssueDTO.TabListDTO(status, 0));
        }
        });
        list.add(new SubcontractIssueDTO.TabListDTO("all", list.stream().mapToInt(SubcontractIssueDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(SubcontractIssueDTO.ExportDTO param, HttpServletResponse response) {
        List<SubcontractIssueDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/subcontractIssue.xlsx";
        String name = "委外发料单导出";
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
        SubcontractIssueEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到委外发料单数据");
        }
        // 更新单据审核状态
        log.info("提交 开始修改委外发料单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动委外发料单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录委外发料单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "委外发料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SubcontractIssueEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "委外发料单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SubcontractIssueEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_ISSUE.getCode());
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
        SubcontractIssueEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外发料单单数据"));

        // 更新审核信息
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "委外发料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SubcontractIssueEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外发料单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除主单数据
        log.info("删除 开始删除委外发料单主单数据，id：【{}】", id);
        super.removeById(id);

        //删除明细
        subcontractIssueDetailService.deleteByMainId(id);

        // 删除日志数据
        log.info("删除 开始删除委外发料单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "委外发料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), entity.getCode(), "删除委外发料单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SubcontractIssueEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外发料单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改委外发料单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SubcontractIssueEntity::getId, id)
            .set(SubcontractIssueEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SubcontractIssueEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "委外发料单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SubcontractIssueEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外发料单数据"));
        // 只有审核中的单据允许撤销
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        log.info("撤销 开始修改委外发料单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "委外发料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_ISSUE.getCode());
        revokeDTO.setUserId(commonService.getUserInfo().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SubcontractIssueEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // 审核完成自动发料

        return Boolean.TRUE;
    }

    @Override
    public List<SubcontractIssueDTO.SubcontractDetailListDTO> listSubcontractDetail(SubcontractIssueDTO.DetailPagingParamDTO dto) {
        List<SubcontractIssueDTO.SubcontractDetailListDTO> resultList = new ArrayList<>();
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
        List<SubcontractOrderDetailEntity> parentList = subcontractOrderDetailList.stream().filter(obj -> StrUtil.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98082);
        }

        //bom信息
        List<String> parentSkuIdList = parentList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIdList);

        //委外子级SKU明细
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98082);
        }

        //已审核发料数量
        List<String> sourceDetailIdList = childList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());
        List<SubcontractIssueDetailEntity> hasDetailList = subcontractIssueDetailService.listBySourceDetailIdList(sourceDetailIdList);

        //sku信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(parentSkuIdList);

        //即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSubDetailSkuInventoryList(childList);

        for (SubcontractOrderDetailEntity parentDetailEntity : parentList) {
            SubcontractIssueDTO.SubcontractDetailListDTO detailListDTO = new SubcontractIssueDTO.SubcontractDetailListDTO();
            detailListDTO.setSkuId(parentDetailEntity.getSkuId());
            detailListDTO.setSkuNo(parentDetailEntity.getSkuNo());
            detailListDTO.setSourceId(subcontractOrderList.get(0).getId());
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

            List<SubcontractOrderDetailEntity> childEntityList = childList.stream().filter(obj -> obj.getParentId().equals(parentDetailEntity.getId())).collect(Collectors.toList());
            List<SubcontractIssueDetailDTO.ListSourceDetailDTO> detailList = new ArrayList<>();
            for (SubcontractOrderDetailEntity  childDetailEntity : childEntityList) {
                SubcontractIssueDetailDTO.ListSourceDetailDTO detailDTO = new SubcontractIssueDetailDTO.ListSourceDetailDTO();
                detailDTO.setSourceId(subcontractOrderList.get(0).getId());
                detailDTO.setSourceDetailId(childDetailEntity.getId());
                detailDTO.setParentSkuId(parentDetailEntity.getSkuId());
                detailDTO.setParentSkuNo(parentDetailEntity.getSkuNo());
                detailDTO.setSkuId(childDetailEntity.getSkuId());
                detailDTO.setSkuNo(childDetailEntity.getSkuNo());
                detailDTO.setWarehouseId(childDetailEntity.getWarehouseId());
                detailDTO.setWarehouseName(childDetailEntity.getWarehouseName());
                detailDTO.setReceiveQty(childDetailEntity.getDeliveryQty());
                detailDTO.setWarehouseLocation(childDetailEntity.getWarehouseLocation());
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
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(detailDTO.getSkuId()) && s.getWarehouseId().equals(detailDTO.getWarehouseId()))
                        .mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
                detailDTO.setCurInventoryQty(curInventoryQty);

                //已发料数量
                Integer hasIssueQty = hasDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detailDTO.getSourceDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                        .map(SubcontractIssueDetailEntity::getIssueQty).reduce(MathUtil.ZERO, Integer::sum);
                detailDTO.setHasIssueQty(hasIssueQty);
                detailList.add(detailDTO);
            }
            detailListDTO.setDetailList(detailList);
            resultList.add(detailListDTO);
        }
        return resultList;
    }

    @Override
    public SubcontractIssueDTO.ViewDTO view(String id) {
        SubcontractIssueEntity subcontractIssueEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外发料单数据"));
        SubcontractIssueDTO.ViewDTO data = BeanMapperUtils.map(SubcontractIssueDTO.ViewDTO.class, subcontractIssueEntity);
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

    public void startProcess(SubcontractIssueEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_ISSUE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * @description: 查询详情数据处理
     * @author Will
     * @date: 2024/1/9 17:11
     * @param data
     */
    private void fillOne(SubcontractIssueDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //状态名称
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));

        //发料类型
        List<DictBasicDTO.ListDTO> issueTypeList = dictBasicService.getByKey(DictBasicEnum.ISSUE_TYPE.getKey());
        String typeName = issueTypeList.stream().filter(obj -> obj.getValue().equals(data.getType())).map(DictBasicDTO.ListDTO::getName)
                .findFirst().orElse("");
        data.setTypeName(typeName);

        //委外发料明细
        List<SubcontractIssueDetailEntity> subcontractIssueDetailList = subcontractIssueDetailService.listByMainIds(Arrays.asList(data.getId()));
        if (CollectionUtil.isEmpty(subcontractIssueDetailList)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_ISSUE_DETAIL_NOT_EXIST);
        }
        //产品信息
        List<String> skuIdList = subcontractIssueDetailList.stream().map(SubcontractIssueDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = subcontractIssueDetailList.stream()
                .map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(paramList);

        //即时库存
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSubIssueSkuInventoryList(subcontractIssueDetailList);

        //已审核发料数量
        List<String> sourceDetailIdList = subcontractIssueDetailList.stream().map(SubcontractIssueDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SubcontractIssueDetailEntity> hasDetailList = subcontractIssueDetailService.listBySourceDetailIdList(sourceDetailIdList);

        List<SubcontractIssueDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(SubcontractIssueDetailDTO.ViewDTO.class, subcontractIssueDetailList);
        for (SubcontractIssueDetailDTO.ViewDTO viewDTO : detailList) {
            //仓位名称
            String warehouseLocationName = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(viewDTO.getWarehouseId()) && obj.getCode().equals(viewDTO.getWarehouseLocation()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            viewDTO.setWarehouseLocationName(warehouseLocationName);
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            viewDTO.setProductName(skuVO.getSkuName());
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(viewDTO.getSkuId()) && s.getWarehouseId().equals(viewDTO.getWarehouseId()))
                    .mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
            viewDTO.setCurInventoryQty(curInventoryQty);
            //已发料数量
            Integer hasIssueQty = hasDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(viewDTO.getSourceDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                    .map(SubcontractIssueDetailEntity::getIssueQty).reduce(MathUtil.ZERO, Integer::sum);
            viewDTO.setHasIssueQty(hasIssueQty);
        }
        data.setDetailList(detailList);
    }


    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().eq(SubcontractIssueEntity::getId, id)
            .set(SubcontractIssueEntity::getApproveUserId, userInfo.getUid())
            .set(SubcontractIssueEntity::getApproveUserName, userInfo.getUserName())
            .set(SubcontractIssueEntity::getApproveStatus, approveStatus)
            .set(SubcontractIssueEntity::getApproveTime, LocalDateTime.now())
            .update(new SubcontractIssueEntity());
     }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SubcontractIssueEntity::getId, id)
        .set(SubcontractIssueEntity::getApproveStatus, approveStatus)
        .set(SubcontractIssueEntity::getApproveUserId, "")
        .set(SubcontractIssueEntity::getApproveUserName, "")
        .set(SubcontractIssueEntity::getApproveTime, null)
        .update(new SubcontractIssueEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractIssueDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(SubcontractIssueDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = list.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(paramList);

        //发料类型
        List<DictBasicDTO.ListDTO> issueTypeList = dictBasicService.getByKey(DictBasicEnum.ISSUE_TYPE.getKey());

        // 属性赋值
        for(SubcontractIssueDTO.ListDTO data : list) {
            //状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //仓位信息
            String warehouseLocationName = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(data.getWarehouseId()) && obj.getCode().equals(data.getWarehouseLocation()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setWarehouseLocationName(warehouseLocationName);
            //产品信息
            String productName = skuVOList.stream().filter(obj -> obj.getSkuId().equals(data.getSkuId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            data.setProductName(productName);
            //发料类型名称
            String issueTypeName = issueTypeList.stream().filter(obj -> obj.getValue().equals(data.getType()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setTypeName(issueTypeName);
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractIssueEntity subcontractIssueEntity) {
        //暂时先默认来源类型
        subcontractIssueEntity.setSourceType(SourceTypeEnum.SUBCONTRACT_ISSUE.getCode());
        //来源单号
        List<SubcontractOrderEntity> subcontractOrderList = scmTaskFeign.listSubcontractOrderByIds(Arrays.asList(subcontractIssueEntity.getSourceId()));
        if (CollectionUtil.isEmpty(subcontractOrderList)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        subcontractIssueEntity.setSourceCode(subcontractOrderList.get(0).getCode());
    }


    /**
     * @description: 查询详情获取即时库存
     * @author Will
     * @date: 2024/1/9 16:46
     * @param subcontractIssueDetailList
     * @return List<SkuInventoryTotalDTO>
     */
    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSubIssueSkuInventoryList(List<SubcontractIssueDetailEntity> subcontractIssueDetailList) {
        //skuId集合
        List<String> skuIdList = subcontractIssueDetailList.stream().map(SubcontractIssueDetailEntity::getSkuId).distinct()
                .collect(Collectors.toList());
        //仓库Id集合
        List<String> warehouseIdList = subcontractIssueDetailList.stream().map(SubcontractIssueDetailEntity::getWarehouseId)
                .distinct().collect(Collectors.toList());
        //仓位集合
        List<String> warehouseLocationList = subcontractIssueDetailList.stream().map(SubcontractIssueDetailEntity::getWarehouseLocation)
                .distinct().collect(Collectors.toList());

        return listSkuInventoryTotalList(skuIdList,warehouseIdList,warehouseLocationList);
    }

    /**
     * @description: 根据委外订单明细获取即时库存
     * @author Will
     * @date: 2024/1/9 16:46
     * @param childList
     * @return List<SkuInventoryTotalDTO>
     */
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

    /**
     * @description: 获取即时库存
     * @author Will
     * @date: 2024/1/10 14:29
     * @param skuIdList
     * @param warehouseIdList
     * @param warehouseLocationList
     * @return List<SkuInventoryTotalDTO>
     */
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
