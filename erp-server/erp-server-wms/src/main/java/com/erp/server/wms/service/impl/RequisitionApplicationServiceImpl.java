package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailShowDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.RequisitionApplicationConverter;
import com.erp.server.wms.mapper.RequisitionApplicationMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 要货申请单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationServiceImpl extends SuperServiceImpl<RequisitionApplicationMapper, RequisitionApplicationEntity> implements RequisitionApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private TransferInfoService transferInfoService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO addDTO) {
        RequisitionApplicationEntity requisitionApplicationEntity = new RequisitionApplicationEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationEntity);

        // 数据处理
        handleData(requisitionApplicationEntity);

        log.info("开始新增要货申请单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YHSQ);
        requisitionApplicationEntity.setCode(code);
        boolean save = super.save(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }
        // 新增明细
        requisitionApplicationDetailService.add(addDTO, requisitionApplicationEntity.getId());
        return new BaseResultDTO.AddDTO(requisitionApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationDTO.UpdateDTO updateDTO) {
        RequisitionApplicationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请单"));
        RequisitionApplicationEntity requisitionApplicationEntity =  BeanMapperUtils.map(RequisitionApplicationEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationEntity);
        log.info("编辑 开始修改要货申请单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }
        // 修改明细数据（包含增删改）
        requisitionApplicationDetailService.update(updateDTO, requisitionApplicationEntity.getId());
        return Boolean.TRUE;
    }

    @Override
    public List<RequisitionApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        FirstMileDeliveryDTO.PagingParamDTO searchParam = new FirstMileDeliveryDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RequisitionApplicationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = RequisitionApplicationStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RequisitionApplicationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new RequisitionApplicationDTO.TabListDTO(status, 0));
            }
        });
        list.add(new RequisitionApplicationDTO.TabListDTO("all", list.stream().mapToInt(RequisitionApplicationDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public PagingVO<RequisitionApplicationDTO.ListDTO> paging(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RequisitionApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public RequisitionApplicationDTO.ViewDTO view(String id) {
        //发货单主信息
        RequisitionApplicationEntity applicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到要货申请单数据"));
        RequisitionApplicationDTO.ViewDTO data = BeanMapperUtils.map(RequisitionApplicationDTO.ViewDTO.class, applicationEntity);
        //发货单详情
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, requisitionApplicationDetailEntities);
        return data;
    }

    @Override
    public BatchResultDTO submit(String id) {
        RequisitionApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到要货申请数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改要货申请状态数据，id：【{}】", id);

        this.updateApproveStatus(id, RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus());

        // 记录操作日志
        log.info("提交 开始记录要货申请日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "要货申请");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public List<RequisitionApplicationDTO.HandleListDTO> handleList(List<String> ids) {
        List<RequisitionApplicationDTO.HandleListDTO> list = baseMapper.handleList(ids);
        long count = list.stream().filter(req -> !RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus().equals(req.getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.WAIT_HANDLE_HANDLE);
        }

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //字段映射处理
        for (RequisitionApplicationDTO.HandleListDTO handleListDTO : list) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(handleListDTO.getSkuId())).findFirst().orElse(new SkuVO());
            handleListDTO.setProductName(skuVO.getSkuName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(handleListDTO.getSkuId())
                            && req.getBomVersion().equals(handleListDTO.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                handleListDTO.setIsCombination(Boolean.TRUE);
            } else {
                handleListDTO.setIsCombination(Boolean.FALSE);
            }

            //如果没有仓位用空仓位
            handleListDTO.setFromWarehouseLocation(StringUtils.isBlank(handleListDTO.getFromWarehouseLocation()) ? "" : handleListDTO.getFromWarehouseLocation());
            handleListDTO.setToWarehouseLocation(StringUtils.isBlank(handleListDTO.getToWarehouseLocation()) ? "" : handleListDTO.getToWarehouseLocation());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleSave(List<RequisitionApplicationDTO.HandleListDTO> list) {
        //根据调出调入仓id查询仓库信息
        List<String> warehouseIds = list.stream().map(req -> req.getFromWarehouseId()).collect(Collectors.toList());
        List<String> toWarehouseIds = list.stream().map(req -> req.getToWarehouseId()).collect(Collectors.toList());
        warehouseIds.addAll(toWarehouseIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);

        //查询产品信息
        List<String> skuIdList = list.stream().map(RequisitionApplicationDTO.HandleListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //调出仓库和调入仓库不一致的单据
        Map<String, List<RequisitionApplicationDTO.HandleListDTO>> map = list.stream().filter(req -> !req.getFromWarehouseId().equals(req.getToWarehouseId())).collect(Collectors.groupingBy(req -> req.getFromWarehouseId().concat(",").concat(req.getToWarehouseId())));
        for (Map.Entry<String, List<RequisitionApplicationDTO.HandleListDTO>> dto : map.entrySet()) {
            List<RequisitionApplicationDTO.HandleListDTO> value = dto.getValue();

            //生成调拨单
            generateHandleToTransferInfo(value, skuVOList, warehouseList, bomChildrenSkuList);
        }

        //修改处理信息
        List<String> raIds = list.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        Boolean flag = updateHandleDate(raIds, RequisitionApplicationStatusEnum.HANDLE_ING.getStatus());

        //保存处理选择的调出,调入,批准数量等信息
        updateHandleDetailDate(list, warehouseList);

        //新增日志
        List<RequisitionApplicationEntity> requisitionApplicationEntities = this.listByIds(raIds);
        for (RequisitionApplicationEntity entity : requisitionApplicationEntities) {
            String msg = StrUtil.format("用户【{}】处理了一个单号为【{}】的【{}】单", commonService.getUserInfo().getUserName(), entity.getCode(), "要货申请");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "处理保存");
        }
        return flag;
    }


    @Override
    public List<RequisitionApplicationDTO.FinishListDTO> finishList(List<String> ids) {
        List<RequisitionApplicationDTO.FinishListDTO> list = baseMapper.finishList(ids);

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //字段映射处理
        for (RequisitionApplicationDTO.FinishListDTO finishListDTO : list) {
            if (!RequisitionApplicationStatusEnum.HANDLE_ING.getStatus().equals(finishListDTO.getStatus())) {
                throw new ServiceException(ApiError.HANDLE_ING_FINISH, finishListDTO.getSourceCode());
            }

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(finishListDTO.getSkuId())).findFirst().orElse(new SkuVO());
            finishListDTO.setProductName(skuVO.getSkuName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(finishListDTO.getSkuId())
                            && req.getBomVersion().equals(finishListDTO.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                finishListDTO.setIsCombination(Boolean.TRUE);
            } else {
                finishListDTO.setIsCombination(Boolean.FALSE);
            }

            //仓位是推荐仓位，与库存无关
            finishListDTO.setPickingWarehouseLocation(skuVO.getWarehouseLocation());

            finishListDTO.setPickingQty(finishListDTO.getPickingQty());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishSave(List<RequisitionApplicationDTO.FinishListDTO> list) {
        //根据调出调入仓id查询仓库信息
        List<String> toWarehouseIds = list.stream().map(req -> req.getToWarehouseId()).collect(Collectors.toList());
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getRequisitionWarehouseId()).collect(Collectors.toList());
        toWarehouseIds.addAll(requisitionWarehouseIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(toWarehouseIds);

        //查询产品信息
        List<String> skuIdList = list.stream().map(RequisitionApplicationDTO.FinishListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //调出仓库和调入仓库不一致的单据
        Map<String, List<RequisitionApplicationDTO.FinishListDTO>> map = list.stream().filter(req -> !req.getRequisitionWarehouseId().equals(req.getPickingWarehouseId())).collect(Collectors.groupingBy(req -> req.getRequisitionWarehouseId().concat(",").concat(req.getPickingWarehouseId())));
        for (Map.Entry<String, List<RequisitionApplicationDTO.FinishListDTO>> dto : map.entrySet()) {
            List<RequisitionApplicationDTO.FinishListDTO> value = dto.getValue();

            //完成要货单生成调拨单
            generateFinishToTransferInfo(value, skuVOList, warehouseList, bomChildrenSkuList);
        }

        //修改处理信息
        List<String> raIds = list.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        Boolean flag = updateHandleDate(raIds, RequisitionApplicationStatusEnum.HANDLE.getStatus());

        //保存完成输入的拣货数量
        updateFinishDetailPickingQty(list);

        //新增日志
        List<RequisitionApplicationEntity> requisitionApplicationEntities = this.listByIds(raIds);
        for (RequisitionApplicationEntity entity : requisitionApplicationEntities) {
            String msg = StrUtil.format("用户【{}】完成了一个单号为【{}】的【{}】单", commonService.getUserInfo().getUserName(), entity.getCode(), "要货申请");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "完成保存");
        }
        return flag;
    }

    @Override
    public List<RequisitionApplicationDTO.printPickingViewDTO> printPickingView(List<String> ids) {
        List<RequisitionApplicationEntity> list = this.listByIds(ids);
        long count = list.stream()
                .filter(req -> !RequisitionApplicationStatusEnum.HANDLE_ING.getStatus().equals(req.getStatus())
                        && !RequisitionApplicationStatusEnum.HANDLE.getStatus().equals(req.getStatus()))
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.HANDLE_ING_OR_HANDLE_IS_PRINT_PICKING);
        }

        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(ids);

        //查询产品信息
        List<String> skuIds = requisitionApplicationDetailEntities.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);

        List<String> childSkuIds = bomChildrenSkuList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        skuIds.addAll(childSkuIds);
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        List<RequisitionApplicationDTO.printPickingViewDTO> printPickingViewList = new ArrayList<>();
        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : requisitionApplicationDetailEntities) {

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(requisitionApplicationDetailEntity.getSkuId())
                            && req.getBomVersion().equals(requisitionApplicationDetailEntity.getBomVersion()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    RequisitionApplicationDTO.printPickingViewDTO viewDTO = new RequisitionApplicationDTO.printPickingViewDTO();
                    BeanMapper.copy(requisitionApplicationDetailEntity, viewDTO);
                    viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                    viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    if (viewDTO.getPickingQty() == null || viewDTO.getPickingQty() == 0) {
                        viewDTO.setPickingQty(requisitionApplicationDetailEntity.getApproveQty());
                    }

                    //匹配sku信息
                    SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(bomChildrenSkuDTO.getParentSkuId())).distinct().findFirst().orElse(new SkuVO());
                    viewDTO.setProductName(skuVO.getSkuName());
                    viewDTO.setWarehouseLocation(StringUtils.isBlank(skuVO.getWarehouseLocation()) ? "" : skuVO.getWarehouseLocation());
                    printPickingViewList.add(viewDTO);
                }
            } else {
                RequisitionApplicationDTO.printPickingViewDTO viewDTO = new RequisitionApplicationDTO.printPickingViewDTO();
                BeanMapper.copy(requisitionApplicationDetailEntity, viewDTO);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationDetailEntity.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                viewDTO.setProductName(skuVO.getSkuName());
                viewDTO.setWarehouseLocation(StringUtils.isBlank(skuVO.getWarehouseLocation()) ? "" : skuVO.getWarehouseLocation());
                if (viewDTO.getPickingQty() == null || viewDTO.getPickingQty() == 0) {
                    viewDTO.setPickingQty(requisitionApplicationDetailEntity.getApproveQty());
                }
                printPickingViewList.add(viewDTO);
            }
        }

        List<RequisitionApplicationDTO.printPickingViewDTO> resultList = printPickingViewList.stream()
                .sorted(Comparator.comparing(RequisitionApplicationDTO.printPickingViewDTO::getSkuNo).reversed()
                    .thenComparing(RequisitionApplicationDTO.printPickingViewDTO::getFromWarehouseName).reversed()
                    .thenComparing(RequisitionApplicationDTO.printPickingViewDTO::getWarehouseLocation).reversed()
                ).collect(Collectors.toList());
        return resultList;
    }

    @Override
    public BatchResultDTO cancelProcess(String id) {
        RequisitionApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
        // 只有待处理的单据允许撤销
        if (!Objects.equals(entity.getStatus(), RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus())) {
            throw new ServiceException(ApiError.WAIT_HANDLE_IS_CANCEL_PROCESS);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改要货申请状态，id：【{}】", id);
        updateApproveStatus(id, RequisitionApplicationStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "头程发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "取消流程操作");
/*        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.FBA_DELIVERY.getCode());
        revokeDTO.setUserId(commonService.getUserInfo().getUid());
        workflowFeign.revokeProcess(revokeDTO);*/
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    public void exportExcel(RequisitionApplicationDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<RequisitionApplicationDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/requisitionApplicationExport.xlsx";
        String name = "要货申请单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public BatchResultDTO delete(String id) {
        RequisitionApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(RequisitionApplicationStatusEnum.WAIT_SUBMIT.getCode(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_1043);
        }

        // 删除明细数据
        requisitionApplicationDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除要货申请单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除要货申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "要货申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "删除要货申请单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public List<RequisitionApplicationEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(RequisitionApplicationEntity::getSourceId, sourceIds).list();
    }

    @Override
    public List<RequisitionApplicationDTO.ChildViewDTO> listChildBySku(RequisitionApplicationDTO.ChildParamDTO dto) {
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(dto.getSkuId()));
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }

        //查询历史子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(Arrays.asList(dto.getSkuId()));
        //查询最新版本的sku子件信息
        List<BomChildrenSkuDTO> bomSonItemList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuNo().equals(skuVOList.get(0).getSkuNo()) && req.getBomVersion().equals(dto.getBomVersion())).collect(Collectors.toList());

        List<RequisitionApplicationDTO.ChildViewDTO> list = new ArrayList<>();
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomSonItemList) {
            RequisitionApplicationDTO.ChildViewDTO childViewDTO = new RequisitionApplicationDTO.ChildViewDTO();
            childViewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
            childViewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
            childViewDTO.setQuantity(bomChildrenSkuDTO.getQuantity());
            childViewDTO.setUsableQty(inventoryService.getUsableInventoryTotal(dto.getWarehouseId(), bomChildrenSkuDTO.getSkuId()));
            list.add(childViewDTO);
        }
        return list;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationEntity requisitionApplicationEntity) {
        String requisitionWarehouseId = requisitionApplicationEntity.getRequisitionWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouse = warehouseService.listWarehouseByIds(Arrays.asList(requisitionWarehouseId));
        requisitionApplicationEntity.setRequisitionWarehouseName(warehouse.get(MathUtil.ZERO).getName());
    }

    /**
     * 详情字段处理
     */
    private void fillOne(RequisitionApplicationDTO.ViewDTO data, List<RequisitionApplicationDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //来源类型中文
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        //要货类型中文
        data.setTypeName(RequisitionApplicationTypeEnum.getName(data.getType()));
        //单据状态中文
        data.setStatusName(RequisitionApplicationStatusEnum.getName(data.getStatus()));

        //详情字段设置
        List<RequisitionApplicationDetailDTO.ViewDTO> viewDetailList = new ArrayList<>();
        for (RequisitionApplicationDetailEntity detailEntity : detailList) {
            RequisitionApplicationDetailDTO.ViewDTO detailView = RequisitionApplicationConverter.INSTANCE.radEntityToRadDto(detailEntity);

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailView.setProductName(skuVO.getSkuName());
            detailView.setImageUrl(skuVO.getSkuImagesUrl());
            detailView.setUsableQty(inventoryService.getUsableInventoryTotal(data.getRequisitionWarehouseId(), skuVO.getSkuId()));

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(finishListDTO.getSkuId())
                            && req.getBomVersion().equals(finishListDTO.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                detailView.setIsCombination(Boolean.TRUE);
            } else {
                detailView.setIsCombination(Boolean.FALSE);
            }

            viewDetailList.add(detailView);
        }

        data.setDetailList(viewDetailList);
    }

    /**
     * 分页查询数据处理
     * @param list
     */
    private void fillList(List<RequisitionApplicationDTO.ListDTO> list) {
        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);
        for (RequisitionApplicationDTO.ListDTO listDTO : list) {
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(listDTO.getSkuId())
                            && req.getBomVersion().equals(listDTO.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                listDTO.setIsCombination(Boolean.TRUE);
            } else {
                listDTO.setIsCombination(Boolean.FALSE);
            }
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(listDTO.getSkuId())).findFirst().orElse(new SkuVO());
            listDTO.setProductName(skuVO.getSkuName());
            //状态中文
            listDTO.setStatusName(RequisitionApplicationStatusEnum.getName(listDTO.getStatus()));
            //要货类型中文
            listDTO.setTypeName(RequisitionApplicationTypeEnum.getName(listDTO.getType()));
        }
    }

    /**
     * 提交状态校验
     */
    private void validateSubmit(RequisitionApplicationEntity entity) {
        // 待提交允许提交
        if(!entity.getStatus().equals(RequisitionApplicationStatusEnum.WAIT_SUBMIT.getStatus()) ) {
            throw new ServiceException(ApiError.IS_SUBMIT_IN_SUBMIT);
        }
        return;
    }

    /**
     * 更新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String status) {
        lambdaUpdate().eq(RequisitionApplicationEntity::getId, id)
                .set(RequisitionApplicationEntity::getStatus, status)
                .update(new RequisitionApplicationEntity());
    }

    /**
     * 修改处理信息
     * @Author Luo_WG
     * @Date 2023/11/24 10:52
     * @param raIds 要货申请表id
     * @return java.lang.Boolean
     **/

    private Boolean updateHandleDate(List<String> raIds, String status) {
        LoginUser userInfo = commonService.getUserInfo();
        return lambdaUpdate().set(RequisitionApplicationEntity::getHandleUserId, userInfo.getUid())
                .set(RequisitionApplicationEntity::getHandleUserName, userInfo.getUserName())
                .set(RequisitionApplicationEntity::getHandleTime, LocalDateTime.now())
                .set(RequisitionApplicationEntity::getStatus, status)
                .in(RequisitionApplicationEntity::getId, raIds)
                .update();
    }

    /**
     * 修改处理详情信息
     * @Author Luo_WG
     * @Date 2023/11/24 10:52
     * @param list 要货申请表id
     * @param warehouseList 要货申请表id
     * @return java.lang.Boolean
     **/
    private void updateHandleDetailDate(List<RequisitionApplicationDTO.HandleListDTO> list, List<WarehouseDTO.UpdateDTO> warehouseList) {
        for (RequisitionApplicationDTO.HandleListDTO handleListDTO : list) {
            //调入仓
            WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream()
                    .filter(req -> req.getId().equals(handleListDTO.getToWarehouseId()))
                    .findFirst()
                    .orElse(new WarehouseDTO.UpdateDTO());

            //调出仓
            WarehouseDTO.UpdateDTO fromWarehouse = warehouseList.stream()
                    .filter(req -> req.getId().equals(handleListDTO.getFromWarehouseId()))
                    .findFirst()
                    .orElse(new WarehouseDTO.UpdateDTO());
            requisitionApplicationDetailService.updateTransferWarehouse(handleListDTO.getFromWarehouseId(), fromWarehouse.getName(),
                    handleListDTO.getToWarehouseId(), toWarehouse.getName(), handleListDTO.getApproveQty(), handleListDTO.getSourceDetailId());
        }
    }

    /**
     * 修改完成详情的拣货数量信息
     * @Author Luo_WG
     * @Date 2023/11/24 10:52
     * @param list 要货申请表id
     * @return java.lang.Boolean
     **/
    private void updateFinishDetailPickingQty(List<RequisitionApplicationDTO.FinishListDTO> list) {
        for (RequisitionApplicationDTO.FinishListDTO handleListDTO : list) {
            requisitionApplicationDetailService.updateFinishDetailPickingQty(handleListDTO.getPickingQty(), handleListDTO.getSourceDetailId());
        }
    }

    /**
     * 要货申请处理生成调拨单
     * @Author Luo_WG
     * @Date 2023/11/24 10:35
     * @param detailEntityList 调出仓库和调入仓库不一致的单据详情
     * @param skuVOList 单据的产品信息
     * @param warehouseList 仓库信息（包含调出仓和调入仓）
     * @param bomChildrenSkuList 子件信息
     * @return java.lang.String
     **/
    private String generateHandleToTransferInfo(List<RequisitionApplicationDTO.HandleListDTO> detailEntityList,
                                                List<SkuVO> skuVOList,
                                                List<WarehouseDTO.UpdateDTO> warehouseList,
                                                List<BomChildrenSkuDTO> bomChildrenSkuList) {
        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();

        //默认来源类型：海外发货计划
        addDTO.setSourceType(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调出仓
        WarehouseDTO.UpdateDTO fromWarehouse = warehouseList.stream().filter(req -> req.getId().equals(detailEntityList.get(MathUtil.ZERO).getFromWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(fromWarehouse.getOrgId());
        //调入仓
        WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream().filter(req -> req.getId().equals(detailEntityList.get(MathUtil.ZERO).getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setInOrgId(toWarehouse.getOrgId());
        //调拨类型
        if (toWarehouse.getOrgId().equals(fromWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        //根据调出仓库和调入仓库的库存组织分组，相同组的SKU合并生成一张调拨单。合并以后无法设置来源单号和id
        addDTO.setSourceId("");
        addDTO.setSourceCode("");
        addDTO.setRemark("");
        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        //相同sku汇总
        Map<String, RequisitionApplicationDTO.HandleListDTO> handleSkuList = detailEntityList.stream().collect(Collectors.groupingBy(n -> n.getSkuId(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            int approveQty = m.stream().mapToInt(RequisitionApplicationDTO.HandleListDTO::getApproveQty).sum();
            RequisitionApplicationDTO.HandleListDTO updateDTO = new RequisitionApplicationDTO.HandleListDTO();
            BeanMapper.copy(m.get(MathUtil.ZERO), updateDTO);
            updateDTO.setApproveQty(approveQty);
            return updateDTO;
        })));

        for (Map.Entry<String, RequisitionApplicationDTO.HandleListDTO> stringhandleListDTOEntry : handleSkuList.entrySet()) {
            RequisitionApplicationDTO.HandleListDTO detailEntity = stringhandleListDTOEntry.getValue();
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(detailEntity.getBomVersion()))
                    .collect(Collectors.toList());

            // 子件需要拆分
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSku : sonSkuList) {
                    //映射信息
                    TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radHandleListToTransferInfoDetail(detailEntity);
                    detailAddDto.setSkuId(bomChildrenSku.getSkuId());
                    detailAddDto.setSkuNo(bomChildrenSku.getSkuNo());
                    detailAddDto.setQty(detailEntity.getApproveQty() * bomChildrenSku.getQuantity());
                    detailAddDtoList.add(detailAddDto);
                }
            } else {
                //映射信息
                TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radHandleListToTransferInfoDetail(detailEntity);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                detailAddDto.setSkuId(skuVO.getSkuId());
                detailAddDto.setSkuNo(skuVO.getSkuNo());
                detailAddDto.setQty(detailEntity.getApproveQty());
                detailAddDtoList.add(detailAddDto);
            }
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }

    /**
     * 要货申请完成生成调拨单
     * @Author Luo_WG
     * @Date 2023/11/24 10:35
     * @param detailEntityList 调出仓库和调入仓库不一致的单据详情
     * @param skuVOList 单据的产品信息
     * @param warehouseList 仓库信息（包含调出仓和调入仓）
     * @param bomChildrenSkuList 子件信息
     * @return java.lang.String
     **/
    private String generateFinishToTransferInfo(List<RequisitionApplicationDTO.FinishListDTO> detailEntityList,
                                                List<SkuVO> skuVOList,
                                                List<WarehouseDTO.UpdateDTO> warehouseList,
                                                List<BomChildrenSkuDTO> bomChildrenSkuList
    ) {
        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();

        //默认来源类型：海外发货计划
        addDTO.setSourceType(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调出仓
        WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream().filter(req -> req.getId().equals(detailEntityList.get(MathUtil.ZERO).getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(toWarehouse.getOrgId());
        //调入仓
        WarehouseDTO.UpdateDTO requisitionWarehouse = warehouseList.stream().filter(req -> req.getId().equals(detailEntityList.get(MathUtil.ZERO).getRequisitionWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setInOrgId(requisitionWarehouse.getOrgId());

        //调拨类型
        if (toWarehouse.getOrgId().equals(requisitionWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }

        //根据调出仓库和调入仓库的库存组织分组，相同组的SKU合并生成一张调拨单。合并以后无法设置来源单号和id
        addDTO.setSourceId("");
        addDTO.setSourceCode("");
        addDTO.setRemark("");

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        //相同sku汇总
        Map<String, RequisitionApplicationDTO.FinishListDTO> handleSkuList = detailEntityList.stream().collect(Collectors.groupingBy(n -> n.getSkuId(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            int pickingQty = m.stream().mapToInt(RequisitionApplicationDTO.FinishListDTO::getPickingQty).sum();
            RequisitionApplicationDTO.FinishListDTO updateDTO = new RequisitionApplicationDTO.FinishListDTO();
            BeanMapper.copy(m.get(MathUtil.ZERO), updateDTO);
            updateDTO.setPickingQty(pickingQty);
            return updateDTO;
        })));

        for (Map.Entry<String, RequisitionApplicationDTO.FinishListDTO> stringhandleListDTOEntry : handleSkuList.entrySet()) {
            RequisitionApplicationDTO.FinishListDTO detailEntity = stringhandleListDTOEntry.getValue();
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(detailEntity.getBomVersion()))
                    .collect(Collectors.toList());

            // 子件需要拆分
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSku : sonSkuList) {
                    //映射信息
                    TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radFinishListToTransferInfoDetail(detailEntity);
                    detailAddDto.setSkuId(bomChildrenSku.getSkuId());
                    detailAddDto.setSkuNo(bomChildrenSku.getSkuNo());
                    detailAddDto.setQty(detailEntity.getPickingQty() * bomChildrenSku.getQuantity());

                    //虚拟仓暂无仓位
                    detailAddDto.setOutWarehouseLocation("");
                    detailAddDtoList.add(detailAddDto);
                }
            } else {
                //映射信息
                TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radFinishListToTransferInfoDetail(detailEntity);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                detailAddDto.setSkuId(skuVO.getSkuId());
                detailAddDto.setSkuNo(skuVO.getSkuNo());
                detailAddDto.setQty(detailEntity.getPickingQty());
                //虚拟仓暂无仓位
                detailAddDto.setOutWarehouseLocation("");
                detailAddDtoList.add(detailAddDto);
            }
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }

}
