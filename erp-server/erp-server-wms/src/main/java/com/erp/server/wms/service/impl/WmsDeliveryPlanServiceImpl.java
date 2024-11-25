package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.DeliveryPlanDetailExportExcelDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseProductReq;
import com.erp.model.wms.dto.third.ThirdWarehouseSkuResp;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.convert.DeliveryPlanConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.listener.DeliveryPlanDetailExcelListener;
import com.erp.server.wms.mapper.WmsDeliveryPlanMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OVERSEAS_DELIVERY_PLAN;

/**
 * <p>
 * 发货计划 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsDeliveryPlanServiceImpl extends SuperServiceImpl<WmsDeliveryPlanMapper, WmsDeliveryPlanEntity> implements WmsDeliveryPlanService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private WmsDeliveryPlanDetailService wmsDeliveryPlanDetailService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private FbaInventoryService fbaInventoryService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsDeliveryPlanDTO.AddDTO addDTO) {
        WmsDeliveryPlanEntity wmsDeliveryPlanEntity = new WmsDeliveryPlanEntity();
        BeanMapperUtils.copy(addDTO, wmsDeliveryPlanEntity);

        // 数据处理
        handleData(wmsDeliveryPlanEntity,addDTO.getDetailList());

        log.info("开始新增发货计划");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHJH);
        wmsDeliveryPlanEntity.setCode(code);
        boolean save = super.save(wmsDeliveryPlanEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】",  UserContext.getDefaultLoginUser().getUserName(), "发货计划" , wmsDeliveryPlanEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), wmsDeliveryPlanEntity.getId(), "新增操作");
        // 新增明细
        wmsDeliveryPlanDetailService.add(addDTO, wmsDeliveryPlanEntity.getId());
        return new BaseResultDTO.AddDTO(wmsDeliveryPlanEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsDeliveryPlanDTO.UpdateDTO updateDTO) {
        WmsDeliveryPlanEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货计划");
        }
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        if (StrUtil.equals(old.getSourceType(),SourceTypeEnum.DELIVERY_SUGGESTION.getCode())) {
            throw new ServiceException("发货建议下推的发货计划不支持编辑!");
        }

        WmsDeliveryPlanEntity wmsDeliveryPlanEntity =  BeanMapperUtils.map(WmsDeliveryPlanEntity.class, updateDTO);

        // 数据处理
        handleData(wmsDeliveryPlanEntity,updateDTO.getDetailList());
        log.info("编辑 开始修改发货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(wmsDeliveryPlanEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }
        // 修改明细数据
        wmsDeliveryPlanDetailService.update(updateDTO, wmsDeliveryPlanEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录发货计划日志数据，单号：【{}】", wmsDeliveryPlanEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsDeliveryPlanEntity.getCode(), "发货计划");
        operateLogService.addModuleOperateLogByObj(old, wmsDeliveryPlanEntity, ModuleTypeEnum.DELIVERY_PLAN.getCode(), wmsDeliveryPlanEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<WmsDeliveryPlanDTO.ListDTO> paging(PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<WmsDeliveryPlanDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<WmsDeliveryPlanDTO.TabListDTO> tabList(PermissionsDTO param) {
        WmsDeliveryPlanDTO.PagingParamDTO searchParam = new WmsDeliveryPlanDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<WmsDeliveryPlanDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(WmsDeliveryPlanDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new WmsDeliveryPlanDTO.TabListDTO(status, 0));
            }
        });
        list.add(new WmsDeliveryPlanDTO.TabListDTO("all", list.stream().mapToInt(WmsDeliveryPlanDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(WmsDeliveryPlanDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("发货计划导出", EXPORT_WMS_OVERSEAS_DELIVERY_PLAN.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        WmsDeliveryPlanEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到发货计划数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改发货计划状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动发货计划流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录发货计划日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(WmsDeliveryPlanDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(WmsDeliveryPlanDTO.UpdateDTO dto) {
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
        WmsDeliveryPlanEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货计划", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(WmsDeliveryPlanEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.DELIVERY_PLAN.getCode());
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
        WmsDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 检查是否有下推单据
        List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryService.listBySourceIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(deliveryEntities)) {
            throw new ServiceException(ApiError.EXIST_FBA_DELIVERY_DETAIL_NOT_DISAPPROVE);
        }
        List<RequisitionApplicationEntity> requisitionApplicationEntities = requisitionApplicationService.listBySourceIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(requisitionApplicationEntities)) {
            throw new ServiceException(ApiError.EXIST_REQUISITION_APPLICATION_NOT_DISAPPROVE);
        }

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(WmsDeliveryPlanEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // 下游单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        WmsDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划数据"));
        // 只有待提交和审核不通过数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.SUBMIT_IS_DELETE);
        }
        // 删除明细数据
        wmsDeliveryPlanDetailService.removeByMainIds(Collections.singletonList(id));
        // 删除主单数据
        log.info("删除 开始删除发货计划主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货计划日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), entity.getCode(), "删除发货计划数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        WmsDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !String.valueOf(InvalidStatusEnum.NOT_VOIDED.getStatus()).equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改发货计划状态数据，id：【{}】", id);
        lambdaUpdate().eq(WmsDeliveryPlanEntity::getId, id)
            .set(WmsDeliveryPlanEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(WmsDeliveryPlanEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货计划", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        WmsDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改发货计划状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_PLAN.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.DELIVERY_PLAN.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, WmsDeliveryPlanEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public WmsDeliveryPlanDTO.ViewDTO view(String id) {
        WmsDeliveryPlanEntity wmsDeliveryPlanEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到发货计划数据"));
        WmsDeliveryPlanDTO.ViewDTO data = BeanMapperUtils.map(WmsDeliveryPlanDTO.ViewDTO.class, wmsDeliveryPlanEntity);

        //发货计划详情
        List<WmsDeliveryPlanDetailEntity> detailEntityList = wmsDeliveryPlanDetailService.listByMainIds(Collections.singletonList(id));

        // 数据填充处理
        fillOne(data, detailEntityList);
        return data;
    }

    /**
    * 启动流程
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(WmsDeliveryPlanEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.DELIVERY_PLAN.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(WmsDeliveryPlanDTO.ViewDTO data, List<WmsDeliveryPlanDetailEntity> detailEntityList) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //获取sku信息
        List<String> skuIdList = detailEntityList.stream().map(WmsDeliveryPlanDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //查询第三方仓SKU信息
//        List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOList = overseasProviderWarehouseService.listByWarehouseIdList(Collections.singletonList(data.getToWarehouseId()));
//        String provideCode;
//        if(CollectionUtils.isNotEmpty(viewDTOList)){
//            provideCode = viewDTOList.get(0).getProviderCode();
//        } else {
//            provideCode = "";
//        }
//        List<ListingInfoWithSkuMappingDTO> listingWithSkuMappingDTOList = skuMappingFeign.listByErpSkuIdAndType(skuIdList,"");
        //设置状态中文名称
        data.setApproveStatusName(data.getApproveStatus().getName());

        //明细信息
        List<WmsDeliveryPlanDetailDTO.ViewDTO> viewDTOS = BeanMapper.copyList(detailEntityList, WmsDeliveryPlanDetailDTO.ViewDTO.class);
        List<SkuMappingDTO.ListSkuParamDTO> skuParamDTOList = new ArrayList<>();
        for (WmsDeliveryPlanDetailDTO.ViewDTO viewDTO : viewDTOS) {
            SkuMappingDTO.ListSkuParamDTO paramDTO = new SkuMappingDTO.ListSkuParamDTO();
            paramDTO.setSkuNo(viewDTO.getSkuNo());
            paramDTO.setWarehouseId(data.getToWarehouseId());
            skuParamDTOList.add(paramDTO);
        }
        List<SkuMappingDTO.ListSkuDTO> listSkuDTOS = skuMappingFeign.listBySkuNoList(skuParamDTOList);

        for (WmsDeliveryPlanDetailDTO.ViewDTO viewDTO : viewDTOS) {

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setProductName(skuVO.getSkuName());
                viewDTO.setImageUrl(skuVO.getSkuImagesUrl());
            }

            //获取库存sku
            SkuMappingDTO.ListSkuDTO listSkuDTO = listSkuDTOS.stream()
                    .filter(req -> req.getProductSkuId().equals(viewDTO.getSkuId())
                        && req.getWarehouseId().equals(data.getToWarehouseId())
                    ).distinct()
                    .findFirst().orElse(null);

            if (ObjectUtil.isNotEmpty(listSkuDTO)) {
                viewDTO.setStockSku(listSkuDTO.getWarehouseSkuNo());
                viewDTO.setStockSkuName(listSkuDTO.getWarehouseProductName());
            }
            WmsDeliveryPlanDetailEntity wmsDeliveryPlanDetailEntity = detailEntityList.stream().filter(v->v.getId().equals(viewDTO.getId())).findFirst().orElse(new WmsDeliveryPlanDetailEntity());
            viewDTO.setMSKU(wmsDeliveryPlanDetailEntity.getPlatformSku());
            viewDTO.setFnSku(wmsDeliveryPlanDetailEntity.getPlatformFnSku());
            viewDTO.setAsin(wmsDeliveryPlanDetailEntity.getPlatformSpu());
        }
        data.setDetailList(viewDTOS);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(WmsDeliveryPlanEntity::getId, id)
            .set(WmsDeliveryPlanEntity::getApproveUserId, userInfo.getUid())
            .set(WmsDeliveryPlanEntity::getApproveUserName, userInfo.getUserName())
            .set(WmsDeliveryPlanEntity::getApproveStatus, approveStatus)
            .set(WmsDeliveryPlanEntity::getApproveTime, LocalDateTime.now())
            .update(new WmsDeliveryPlanEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(WmsDeliveryPlanEntity::getId, id)
            .set(WmsDeliveryPlanEntity::getApproveUserId, "")
            .set(WmsDeliveryPlanEntity::getApproveUserName, "")
            .set(WmsDeliveryPlanEntity::getApproveStatus, approveStatus)
            .set(WmsDeliveryPlanEntity::getApproveTime, null)
            .update(new WmsDeliveryPlanEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(WmsDeliveryPlanEntity::getId, id)
        .set(WmsDeliveryPlanEntity::getApproveStatus, approveStatus)
        .update(new WmsDeliveryPlanEntity());
    }

    @Override
    public List<FirstMileDeliveryDTO.DeliverRecordView> listDeliverRecord(String id) {
        List<FirstMileDeliveryDTO.DeliverRecordView> deliverRecordViews;
        List<String> ids = requisitionApplicationService.listBySourceIds(Collections.singletonList(id)).stream().map(v->v.getId()).collect(Collectors.toList());
        ids.add(id);
        deliverRecordViews = firstMileDeliveryService.listDeliveryRecordBySourceIds(ids, null);
        return deliverRecordViews;
    }

    @Override
    public List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> detailIds) {
        List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list = baseMapper.generateRequisitionApplicationView(detailIds);

        //审核通过才能下推
        long count = list.stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98063);
        }
        //已下推的要货申请， 已审核的要货申请，sku数量超过或等于未下推计划数量就不展示
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listBySourceDetailIds(detailIds);
        list = list.stream().filter(e -> hasQtyCanPush(e,requisitionApplicationDetailEntities)).collect(Collectors.toList());
        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        for (WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO viewDTO : list) {
            if(DeliveryPlanTypeEnum.FBA.getCode().equals(viewDTO.getDeliveryPlanType())){
                viewDTO.setType(RequisitionApplicationTypeEnum.FBA.getCode());
                viewDTO.setTypeName(RequisitionApplicationTypeEnum.FBA.getName());
                viewDTO.setChannelId(viewDTO.getShopId());
                viewDTO.setChannelName(viewDTO.getShopName());
            }else{
                viewDTO.setType(RequisitionApplicationTypeEnum.THIRD_WAREHOUSE.getCode());
                viewDTO.setTypeName(RequisitionApplicationTypeEnum.THIRD_WAREHOUSE.getName());
            }

            //来源类型
            viewDTO.setSourceType(SourceTypeEnum.DELIVERY_PLAN.getCode());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                viewDTO.setIsCombination(Boolean.TRUE);
                viewDTO.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
            } else {
                viewDTO.setIsCombination(Boolean.FALSE);
            }

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
            }
        }
        return list;
    }

    /**
     * 校验是否存在可以下推的数量
     * @param dto
     * @param requisitionApplicationDetailEntities
     * @return
     */
    private boolean hasQtyCanPush(WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto, List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities) {
        if (CollectionUtils.isEmpty(requisitionApplicationDetailEntities)){
            return Boolean.TRUE;
        }
        //已下推要货数量
        int requisitionQty = requisitionApplicationDetailEntities.stream().filter(e -> Objects.equals(dto.getSourceDetailId(), e.getSourceDetailId())).mapToInt(RequisitionApplicationDetailEntity::getRequisitionQty).reduce(MathUtil.ZERO, Integer::sum);
        if (dto.getPlanQty() > requisitionQty){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean generateRequisitionApplicationSave(List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list) {
        if (CollectionUtils.isEmpty(list)){
            throw new ServiceException("明细数据不能为空");
        }
        return generateRequisitionApplication(list, Boolean.FALSE);
    }

    @Override
    public Boolean generateRequisitionApplicationSaveAndSubmit(List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list) {
        return generateRequisitionApplication(list, Boolean.TRUE);
    }

    private Boolean generateRequisitionApplication(List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list, Boolean isSubmit) {
        //一个发货计划单，生成一个要货申请单
        Map<String, List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>> map = list.stream().collect(Collectors.groupingBy(WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO::getSourceId));

        //根据仓库id查询仓库信息
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getRequisitionWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(requisitionWarehouseIds);
        List<String> fromVirtualWarehouseIdList = list.stream().map(WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        Map<String,String> virtualWarehouseNameMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(fromVirtualWarehouseIdList)){
            List<VirtualWarehouseEntity> virtualWarehouseEntities = virtualWarehouseService.listByIds(fromVirtualWarehouseIdList);
            virtualWarehouseNameMap = virtualWarehouseEntities.stream().collect(Collectors.toMap(BaseEntity::getId, VirtualWarehouseEntity::getName));
        }
        for (Map.Entry<String, List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>> entry : map.entrySet()) {
            List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> value = entry.getValue();
            //映射主表信息
            RequisitionApplicationDTO.AddDTO addDTO = DeliveryPlanConverter.INSTANCE.DeliveryPlanGRA(value.get(MathUtil.ZERO));
            //要货仓库中文
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(value.get(MathUtil.ZERO).getRequisitionWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            addDTO.setRequisitionWarehouseName(updateDTO.getName());

            //映射详情信息
            List<RequisitionApplicationDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO viewDTO : value) {

                RequisitionApplicationDetailDTO.AddDTO detailAddDto = DeliveryPlanConverter.INSTANCE.DeliveryPlanDetailGRA(viewDTO);
                detailAddDto.setFromVirtualWarehouseName(virtualWarehouseNameMap.get(detailAddDto.getFromVirtualWarehouseId()));

                detailAddList.add(detailAddDto);
            }
            addDTO.setDetailList(detailAddList);

            // 检查和刷新fnSku
            if (RequisitionApplicationTypeEnum.FBA.getCode().equalsIgnoreCase(addDTO.getType())){
                fbaInventoryService.checkAndUpdateFnsku(addDTO);
            }

            BaseResultDTO.AddDTO add = requisitionApplicationService.add(addDTO);
            if (isSubmit) {
                requisitionApplicationService.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids) {
        List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> list = baseMapper.generateDeliverView(ids);

        List<String> fbaTypeCodes = list.stream().filter(v->v.getType().equals(DeliveryPlanTypeEnum.FBA.getCode())).map(WmsDeliveryPlanDTO.GenerateDeliverViewDTO::getSourceCode).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(fbaTypeCodes)){
            throw new ServiceException(CharSequenceUtil.format("【{}】为FBA发货计划，发货单需要从FBA货件下推",fbaTypeCodes));
        }

        //审核通过才能下推
        long count = list.stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98063);
        }

        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        for (WmsDeliveryPlanDTO.GenerateDeliverViewDTO viewDTO : list) {

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                viewDTO.setIsCombination(Boolean.TRUE);
            } else {
                viewDTO.setIsCombination(Boolean.FALSE);
            }

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
            }
        }
        return list;
    }

    @Override
    public Boolean generateDeliverSave(List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> list) {
        return generateDeliver(list, Boolean.FALSE);
    }

    @Override
    public Boolean generateDeliverSaveAndSubmit(List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> list) {
        return generateDeliver(list, Boolean.TRUE);
    }

    @Override
    public ListingInfoDTO.ImportDTO importFile(MultipartFile excelFile, List<String> thirdSkuNoList, String warehouseId, String shopId, HttpServletResponse response) {

        if(CharSequenceUtil.isBlank(warehouseId)&& CharSequenceUtil.isBlank(shopId)){
            throw new ServiceException("仓库id不能为空");
        }

        //查询第三方SKU信息
        List<ListingInfoWithSkuMappingDTO> listingWithSkuMappingDTOList;

        //店铺不为空代表是fba ，否则是第三方仓
        if(CharSequenceUtil.isNotBlank(shopId)){
            listingWithSkuMappingDTOList = skuMappingFeign.listByErpSkuIdAndType(new ArrayList<>(),"","",shopId);
        }else{
            List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOList = overseasProviderWarehouseService.listByWarehouseIdList(Collections.singletonList(warehouseId));
            String provideCode = "";
            if(CollectionUtils.isNotEmpty(viewDTOList)){
                provideCode = viewDTOList.get(0).getProviderCode();
            }
            listingWithSkuMappingDTOList = skuMappingFeign.listByErpSkuIdAndType(new ArrayList<>(),provideCode,warehouseId,"");
        }

        DeliveryPlanDetailExcelListener excelListenerUtil = new DeliveryPlanDetailExcelListener(thirdSkuNoList,listingWithSkuMappingDTOList,warehouseId,CharSequenceUtil.isNotBlank(shopId));
        try {
            EasyExcel.read(excelFile.getInputStream(), DeliveryPlanDetailExportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DeliveryPlanDetailExportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        ListingInfoDTO.ImportDTO importDTO = new ListingInfoDTO.ImportDTO();
        //导入数据处理
        List<ListingInfoDTO.PageDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DeliveryPlanDetailExportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "海外发货计划错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, DeliveryPlanDetailExportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        this.fillData(successList);
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public Boolean updateDeliveryStatus(List<String> ids, String deliveryStatus) {
        return lambdaUpdate().set(WmsDeliveryPlanEntity::getDeliveryStatus, deliveryStatus)
                .in(WmsDeliveryPlanEntity::getId, ids)
                .update();
    }

    @Override
    public PagingVO<WmsDeliveryPlanDTO.ListDTO> exportOverseasDeliveryPlan(PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> dto) {
        Page<WmsDeliveryPlanDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public WmsDeliveryPlanDTO.DeliverPlanViewDTO deliverPlanView(String id) {
        WmsDeliveryPlanDTO.DeliverPlanViewDTO deliverPlanViewDTO = new WmsDeliveryPlanDTO.DeliverPlanViewDTO();
        WmsDeliveryPlanEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            return deliverPlanViewDTO;
        }
        deliverPlanViewDTO.setDeliverPlanCode(entity.getCode());
        deliverPlanViewDTO.setApproveStatus(entity.getApproveStatus().getCode());
        deliverPlanViewDTO.setApproveStatusName(ApproveStatusEnum.getName(entity.getApproveStatus().getCode()));
        //明细
        List<WmsDeliveryPlanDetailEntity> wmsDeliveryPlanDetailList = wmsDeliveryPlanDetailService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isEmpty(wmsDeliveryPlanDetailList)) {
            return deliverPlanViewDTO;
        }
        //来源id集合
        List<String> sourceIdList = wmsDeliveryPlanDetailList.stream().flatMap(obj -> Stream.of(BeanUtil.copyToList(JSONUtil.parseArray(obj.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().map(WmsDeliveryPlanDetailDTO.SourceJsonDTO::getSourceId).toArray(String[]::new))).collect(Collectors.toList());
        List<DeliverySuggestEntity> deliverySuggestList = FeignQuery.create(DeliverySuggestEntity.class).in(DeliverySuggestEntity::getId, sourceIdList).list();

        //根据来源id查询发货单
        List<RequisitionApplicationEntity> requisitionApplicationEntityList = requisitionApplicationService.listBySourceIds(Collections.singletonList(id));
        List<String> requisitionIds = requisitionApplicationEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(requisitionIds);
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listBySourceIds(new ArrayList<>(CollectionUtil.union(Collections.singletonList(id),requisitionIds)));
        List<String> allDeliveryIds = firstMileDeliveryEntities.stream().map(v->v.getId()).collect(Collectors.toList());
        //根据来源详情id查询发货详情
        List<FirstMileDeliveryDetailEntity> fbaDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(allDeliveryIds);

        List<WmsDeliveryPlanDTO.DeliverPlanDetailViewDTO> detailList = new ArrayList<>();
        for (WmsDeliveryPlanDetailEntity detailEntity :wmsDeliveryPlanDetailList) {
            WmsDeliveryPlanDTO.DeliverPlanDetailViewDTO deliverPlanDetailViewDTO = new WmsDeliveryPlanDTO.DeliverPlanDetailViewDTO();
            deliverPlanDetailViewDTO.setMSku(detailEntity.getPlatformSku());
            deliverPlanDetailViewDTO.setFnSku(detailEntity.getPlatformFnSku());
            deliverPlanDetailViewDTO.setSkuNo(detailEntity.getSkuNo());
            deliverPlanDetailViewDTO.setDeliveryPlanQty(detailEntity.getQty());
            //来源json数据
            List<WmsDeliveryPlanDetailDTO.SourceJsonDTO> sourceJsonList = BeanUtil.copyToList(JSONUtil.parseArray(detailEntity.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class);
            //已发数量
            Integer hasDeliveryPlanQty = sourceJsonList.stream().map(WmsDeliveryPlanDetailDTO.SourceJsonDTO::getPlanDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            deliverPlanDetailViewDTO.setHasDeliveryPlanQty(hasDeliveryPlanQty);
            Integer deliveryQty = MathUtil.ZERO;
            //设置发货单号拿最新的一个发货单
            if(entity.getType().equals(DeliveryPlanTypeEnum.FBA.getCode())){
                List<RequisitionApplicationEntity> requisitionApplicationList = requisitionApplicationEntityList.stream().filter(req -> req.getSourceId().equals(entity.getId())).collect(Collectors.toList());
                List<String> requisitionIdList = requisitionApplicationList.stream().map(v->v.getId()).collect(Collectors.toList());
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(v->requisitionIdList.contains(v.getSourceId()) || v.getSourceId().equals(entity.getId())).sorted(Comparator.comparing(FirstMileDeliveryEntity::getCreateTime).reversed()).collect(Collectors.toList());
                List<String> deliveryIds = deliveryEntities.stream().filter(e ->Objects.equals(e.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())).map(BaseEntity::getId).collect(Collectors.toList());
                List<FirstMileDeliveryDetailEntity> deliveryDetailEntities = fbaDeliveryDetailEntities.stream().filter(req -> deliveryIds.contains(req.getMainId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(deliveryEntities)) {
                    //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
                    List<FirstMileDeliveryDetailEntity> detailDeliveryByFbaList = deliveryDetailEntities.stream().filter(v->v.getSkuId().equals(detailEntity.getSkuId()) && v.getFnSku().equals(detailEntity.getPlatformFnSku())).collect(Collectors.toList());
                     deliveryQty = detailDeliveryByFbaList.stream().mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty).sum();
                }
            }else{
                List<RequisitionApplicationEntity> requisitionApplicationList = requisitionApplicationEntityList.stream().filter(req -> req.getSourceId().equals(entity.getId())).collect(Collectors.toList());
                List<String> requisitionIdList = requisitionApplicationList.stream().map(BaseEntity::getId).collect(Collectors.toList());
                List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailEntities.stream()
                        .filter(e -> requisitionIdList.contains(e.getMainId()) && Objects.equals(detailEntity.getId(), e.getSourceDetailId())).collect(Collectors.toList());
                List<String> requisitionDetailIds = requisitionApplicationDetailEntityList.stream().map(RequisitionApplicationDetailEntity::getId).collect(Collectors.toList());
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(req -> req.getSourceId().equals(entity.getId()) || requisitionIdList.contains(req.getSourceId())).sorted(Comparator.comparing(FirstMileDeliveryEntity::getCreateTime).reversed()).collect(Collectors.toList());

                List<String> deliveryIds = deliveryEntities.stream().filter(e ->Objects.equals(e.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())).map(FirstMileDeliveryEntity::getId).collect(Collectors.toList());
                //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
                 deliveryQty = fbaDeliveryDetailEntities.stream().filter(req -> requisitionDetailIds.contains(req.getSourceDetailId()) && CollectionUtils.isNotEmpty(deliveryIds) && deliveryIds.contains(req.getMainId())).mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty).sum();
            }

            List<WmsDeliveryPlanDTO.DescriptionViewDTO> descriptionViewDTOList = new ArrayList<>();
            for (WmsDeliveryPlanDetailDTO.SourceJsonDTO sourceJsonDTO : sourceJsonList) {
                WmsDeliveryPlanDTO.DescriptionViewDTO descriptionViewDTO = new WmsDeliveryPlanDTO.DescriptionViewDTO();
                DeliverySuggestEntity suggestEntity = deliverySuggestList.stream().filter(obj -> StrUtil.equals(obj.getId(), sourceJsonDTO.getSourceId())).findFirst().orElse(new DeliverySuggestEntity());
                descriptionViewDTO.setDeliverySuggestCode(suggestEntity.getCode());
                descriptionViewDTO.setDeliverySuggestQty(suggestEntity.getDeliveryStockUpQty());
                if (deliveryQty > suggestEntity.getPlanDeliveryQty()) {
                    descriptionViewDTO.setHasDeliveryPlanQty(sourceJsonDTO.getPlanDeliveryQty());
                    deliveryQty = deliveryQty - suggestEntity.getPlanDeliveryQty();
                } else {
                    descriptionViewDTO.setHasDeliveryPlanQty(deliveryQty);
                }
                descriptionViewDTOList.add(descriptionViewDTO);
            }
            deliverPlanDetailViewDTO.setDescriptionList(descriptionViewDTOList);
            detailList.add(deliverPlanDetailViewDTO);
        }
        deliverPlanViewDTO.setDetailList(detailList);
        return deliverPlanViewDTO;
    }

    /**
     * 下推发货单处理
     * @Author Luo_WG
     * @Date 2023/11/23 16:33
     * @param list 数据集
     * @param isSubmit 是否提交
     * @return java.lang.Boolean
     **/
    private Boolean generateDeliver(List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> list, Boolean isSubmit) {
        //一个发货计划单，生成一个要发货单
        Map<String, List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO>> map = list.stream().collect(Collectors.groupingBy(WmsDeliveryPlanDTO.GenerateDeliverViewDTO::getSourceId));
        List<String> ids = new ArrayList<>();

        //查询仓库信息
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getDeliveryWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(requisitionWarehouseIds);

        //查询子件信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //获取sku信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);

        for (Map.Entry<String, List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO>> entry : map.entrySet()) {
            List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> value = entry.getValue();
            //映射主表信息
            FirstMileDeliveryDTO.AddDTO addDTO = DeliveryPlanConverter.INSTANCE.generateDeliverFDD(value.get(MathUtil.ZERO));

            //备货类型
            addDTO.setDemandType(FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode());
            //来源类型
            addDTO.setSourceType(SourceTypeEnum.DELIVERY_PLAN.getCode());

            //映射详情信息
            List<FirstMileDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (WmsDeliveryPlanDTO.GenerateDeliverViewDTO viewDTO : value) {
                if(DeliveryPlanTypeEnum.FBA.getCode().equals(viewDTO.getType())){
                    throw new ServiceException(CharSequenceUtil.format("【{}】为FBA发货计划，发货单需要从FBA货件下推",viewDTO.getSourceCode()));
                }
                //发货仓库中文
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(viewDTO.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                addDTO.setDeliveryWarehouseName(updateDTO.getName());
                addDTO.setInventoryOrgId(updateDTO.getOrgId());

                FirstMileDeliveryDetailDTO.AddDTO detailAddDto = DeliveryPlanConverter.INSTANCE.generateDeliverDetailFDD(viewDTO);

                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    detailAddDto.setIsCombination(Boolean.TRUE);
                } else {
                    detailAddDto.setIsCombination(Boolean.FALSE);
                }

                //映射产品信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).distinct().findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(skuVO)) {
                    detailAddDto.setSkuNo(skuVO.getSkuNo());
                    detailAddDto.setNetWeight(skuVO.getNetWeight());
                    detailAddDto.setProductSizeLength(skuVO.getProductLength());
                    detailAddDto.setProductSizeWidth(skuVO.getProductWidth());
                    detailAddDto.setProductSizeHeight(skuVO.getProductHeight());
                }
                //暂无仓位
                detailAddDto.setWarehouseLocation("");
                detailAddList.add(detailAddDto);
            }
            addDTO.setDetailList(detailAddList);

            BaseResultDTO.AddDTO add = firstMileDeliveryService.add(addDTO);
            if (isSubmit) {
                firstMileDeliveryService.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<WmsDeliveryPlanDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<String> detailIds = list.stream().map(req -> req.getDetailId()).distinct().collect(Collectors.toList());

        //根据来源id查询发货单
        List<RequisitionApplicationEntity> requisitionApplicationEntityList = requisitionApplicationService.listBySourceIds(ids);
        List<String> requisitionIds = requisitionApplicationEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(requisitionIds);
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listBySourceIds(new ArrayList<>(CollectionUtil.union(ids,requisitionIds)));
        List<String> allDeliveryIds = firstMileDeliveryEntities.stream().map(v->v.getId()).collect(Collectors.toList());
        //根据来源详情id查询发货详情
        List<FirstMileDeliveryDetailEntity> fbaDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(allDeliveryIds);

        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        //根据单据id查询审核流程
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(ids);


        // 属性赋值
        for(WmsDeliveryPlanDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(data.getDeliveryStatus()));
            //物流方式
            data.setExpectLogisticsMethodName(LogisticsMethodEnum.getName(data.getExpectLogisticsMethod()));
            //设置发货单号拿最新的一个发货单
            if(data.getType().equals(DeliveryPlanTypeEnum.FBA.getCode())){
                List<RequisitionApplicationEntity> requisitionApplicationList = requisitionApplicationEntityList.stream().filter(req -> req.getSourceId().equals(data.getId())).collect(Collectors.toList());
                List<String> requisitionIdList = requisitionApplicationList.stream().map(v->v.getId()).collect(Collectors.toList());
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(v->requisitionIdList.contains(v.getSourceId()) || v.getSourceId().equals(data.getId())).sorted(Comparator.comparing(FirstMileDeliveryEntity::getCreateTime).reversed()).collect(Collectors.toList());
                List<String> deliveryIds = deliveryEntities.stream().filter(e ->Objects.equals(e.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())).map(BaseEntity::getId).collect(Collectors.toList());
                List<FirstMileDeliveryDetailEntity> deliveryDetailEntities = fbaDeliveryDetailEntities.stream().filter(req -> deliveryIds.contains(req.getMainId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(deliveryEntities)) {
                    data.setDeliveryCode(deliveryEntities.get(MathUtil.ZERO).getCode());
                    //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
                    List<FirstMileDeliveryDetailEntity> detailDeliveryByFbaList = deliveryDetailEntities.stream().filter(v->v.getSkuId().equals(data.getSkuId()) && v.getFnSku().equals(data.getPlatformFnSku())).collect(Collectors.toList());
                    Integer deliveryQty = detailDeliveryByFbaList.stream().mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty).sum();
                    data.setDeliveryQty(deliveryQty);
                }
            }else{
                List<RequisitionApplicationEntity> requisitionApplicationList = requisitionApplicationEntityList.stream().filter(req -> req.getSourceId().equals(data.getId())).collect(Collectors.toList());
                List<String> requisitionIdList = requisitionApplicationList.stream().map(BaseEntity::getId).collect(Collectors.toList());
                List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailEntities.stream()
                        .filter(e -> requisitionIdList.contains(e.getMainId()) && Objects.equals(data.getDetailId(), e.getSourceDetailId())).collect(Collectors.toList());
                List<String> requisitionDetailIds = requisitionApplicationDetailEntityList.stream().map(RequisitionApplicationDetailEntity::getId).collect(Collectors.toList());
                List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryEntities.stream().filter(req -> req.getSourceId().equals(data.getId()) || requisitionIdList.contains(req.getSourceId())).sorted(Comparator.comparing(FirstMileDeliveryEntity::getCreateTime).reversed()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(deliveryEntities)) {
                    data.setDeliveryCode(deliveryEntities.get(MathUtil.ZERO).getCode());
                }
                List<String> deliveryIds = deliveryEntities.stream().filter(e ->Objects.equals(e.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())).map(FirstMileDeliveryEntity::getId).collect(Collectors.toList());
                //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
                Integer deliveryQty = fbaDeliveryDetailEntities.stream().filter(req -> requisitionDetailIds.contains(req.getSourceDetailId()) && CollectionUtils.isNotEmpty(deliveryIds) && deliveryIds.contains(req.getMainId())).mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty).sum();
                data.setDeliveryQty(deliveryQty);

            }
            //来源单号
            JSONArray sourceJson = data.getSourceJson();
            if (ObjectUtil.isNotEmpty(sourceJson)) {
                String sourceCodes = BeanUtil.copyToList(JSONUtil.parseArray(sourceJson), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class)
                        .stream().map(obj -> obj.getSourceCode()).distinct().collect(Collectors.joining(","));
                data.setSourceCodes(sourceCodes);
            }

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(data.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                data.setIsCombination(Boolean.TRUE);
            } else {
                data.setIsCombination(Boolean.FALSE);
            }
            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(data.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                data.setSkuNo(skuVO.getSkuNo());
                data.setProductName(skuVO.getSkuName());
            }
            //待审核人
            List<String> curApproveName = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(data.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String waitApproveUserName = StringUtils.join(curApproveName, ",");
            data.setWaitApproveUserName(waitApproveUserName);
            data.setTypeName(EnumMessage.getNameByCode(DeliveryPlanTypeEnum.class, data.getType()));
        }
    }
    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(WmsDeliveryPlanEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(WmsDeliveryPlanEntity wmsDeliveryPlanEntity, List<? extends WmsDeliveryPlanDetailDTO.CommonDTO> detailList) {

        //设置仓库中文名
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Collections.singletonList(wmsDeliveryPlanEntity.getToWarehouseId()));
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(wmsDeliveryPlanEntity.getToWarehouseId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(updateDTO)) {
            wmsDeliveryPlanEntity.setToWarehouseName(updateDTO.getName());
        }

        if(DeliveryPlanTypeEnum.FBA.getCode().equals(wmsDeliveryPlanEntity.getType())){
            if(CharSequenceUtil.isBlank(wmsDeliveryPlanEntity.getShopId())){
                throw new ServiceException("店铺不能为空");
            }
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(wmsDeliveryPlanEntity.getShopId());
            wmsDeliveryPlanEntity.setShopName(shopInfoEntity.getName());
            wmsDeliveryPlanEntity.setCountry(shopInfoEntity.getDictCountryCode());
            wmsDeliveryPlanEntity.setCountryName(shopInfoEntity.getCountryName());
        }else if (DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode().equals(wmsDeliveryPlanEntity.getType())){
            //根据仓库id查询和第三方仓绑定关系，并设置国家字段值
            OverseasProviderWarehouseEntity warehouseEntity = overseasProviderWarehouseService.getByWarehouseId(wmsDeliveryPlanEntity.getToWarehouseId());
            if (ObjectUtil.isNotEmpty(warehouseEntity)) {
                wmsDeliveryPlanEntity.setCountry(warehouseEntity.getCountry());
                wmsDeliveryPlanEntity.setCountryName(warehouseEntity.getCountryName());
            }
            //如果是谷仓，校验商品能不能发该仓库
            OverseasProviderEntity overseasProviderEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(wmsDeliveryPlanEntity.getToWarehouseId());
            if(Objects.nonNull(overseasProviderEntity) && overseasProviderEntity.getCode().equals(OmsPlatformEnum.OMS_GOOD_CANG.getCode())){
                List<String> platformSkuList = detailList.stream().map(WmsDeliveryPlanDetailDTO.CommonDTO::getPlatformSku).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(platformSkuList)){
                    return;
                }
                ThirdWarehouseService handlerService = thirdWarehouseRegistry.getHandlerByAuthId(overseasProviderEntity.getId());
                ApiResult<List<ThirdWarehouseSkuResp>> skuResult = handlerService.getSkuList(ThirdWarehouseProductReq.builder().skuNoList(platformSkuList).build(),overseasProviderEntity.getId());
                if(!skuResult.isSuccess()){
                    throw new ServiceException("查询谷仓商品失败:"+skuResult.getMsg());
                }
                String country = warehouseEntity.getCountry();
                List<ThirdWarehouseSkuResp> thirdWarehouseSkuList = skuResult.getData();
                List<String> errorSkuList = new ArrayList<>();
                for(String platformSku : platformSkuList){
                    ThirdWarehouseSkuResp thirdWarehouseSku = thirdWarehouseSkuList.stream().filter(v->v.getProductSku().equals(platformSku)).findFirst().orElse(null);
                    if(Objects.nonNull(thirdWarehouseSku)){
                        if(thirdWarehouseSku.getImportCountryList().stream().noneMatch(v->v.getCountryCode().equals(country))){
                            errorSkuList.add(platformSku);
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(errorSkuList)){
                    throw new ServiceException(CharSequenceUtil.format("{}不可出口到{}所在的国家,请先在第三方系统维护商品进口国清关信息",errorSkuList,Objects.isNull(updateDTO)?"":updateDTO.getName()));
                }
            }
        }else{
            throw new ServiceException("非法发货计划类型");
        }


    }
    private void fillData(List<ListingInfoDTO.PageDTO> records) {
        List<String> skuNo = records.stream().map(ListingInfoDTO.PageDTO::getSkuNo).collect(Collectors.toList());

        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNo);

        records.forEach(v->{
            SkuVO skuVO = skuVOList.stream().filter(t->t.getSkuNo().equals(v.getSkuNo())).findFirst().orElse(null);
            if(Objects.nonNull(skuVO)){
                v.setImagesUrl(skuVO.getSkuImagesUrl());
                v.setProductName(skuVO.getSkuName());
            }
        });
    }
}
