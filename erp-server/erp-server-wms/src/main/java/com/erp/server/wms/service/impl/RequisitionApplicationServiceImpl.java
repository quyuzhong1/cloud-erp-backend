package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailShowDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationTypeEnum;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
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
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHJH);
        requisitionApplicationEntity.setCode(code);
        boolean save = super.save(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "要货申请单" , requisitionApplicationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationEntity.getId(), "新增操作");
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

        // 记录主单操作日志
        log.info("编辑 开始记录要货申请单日志数据，单号：【{}】", requisitionApplicationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), requisitionApplicationEntity.getCode(), "要货申请单");
        operateLogService.addModuleOperateLogByObj(old, requisitionApplicationEntity, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<RequisitionApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        FbaDeliveryDTO.PagingParamDTO searchParam = new FbaDeliveryDTO.PagingParamDTO();
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
    public List<RequisitionApplicationDTO.handleListDTO> handleList(List<String> ids) {
        List<RequisitionApplicationDTO.handleListDTO> list = baseMapper.handleList(ids);
        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //字段映射处理
        for (RequisitionApplicationDTO.handleListDTO handleListDTO : list) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(handleListDTO.getSkuId())).findFirst().orElse(new SkuVO());
            handleListDTO.setProductName(skuVO.getSkuName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(handleListDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                handleListDTO.setIsCombination(Boolean.TRUE);
            } else {
                handleListDTO.setIsCombination(Boolean.FALSE);
            }
        }
        return list;
    }

    @Override
    public Boolean handleSave(List<RequisitionApplicationDTO.handleListDTO> list) {
        List<String> warehouseIds = list.stream().map(req -> req.getFromWarehouseId()).collect(Collectors.toList());
        List<String> toWarehouseIds = list.stream().map(req -> req.getToWarehouseId()).collect(Collectors.toList());
        warehouseIds.addAll(toWarehouseIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);


        //调出仓库和调入仓库不一致的单据
        Map<String, List<RequisitionApplicationDTO.handleListDTO>> map = list.stream().filter(req -> !req.getFromWarehouseId().equals(req.getToWarehouseId())).collect(Collectors.groupingBy(req -> req.getFromWarehouseId().concat(",").concat(req.getToWarehouseId())));
        for (Map.Entry<String, List<RequisitionApplicationDTO.handleListDTO>> dto : map.entrySet()) {

            List<RequisitionApplicationDTO.handleListDTO> value = dto.getValue();

            generateHandleToTransferInfo(value, warehouseList);

        }
        List<String> ids = new ArrayList<>();

        return null;
    }

    /**
     * 要货申请处理生成调拨单
     * @param detailEntityList
     * @return
     */
    private String generateHandleToTransferInfo(List<RequisitionApplicationDTO.handleListDTO> detailEntityList, List<WarehouseDTO.UpdateDTO> warehouseList) {
        //获取sku信息
        List<String> skuNoList = detailEntityList.stream().map(RequisitionApplicationDTO.handleListDTO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：FBA货件
        addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream().filter(req -> req.getId().equals(detailEntityList.get(MathUtil.ZERO).getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setInOrgId(toWarehouse.getOrgId());
        //调出组织
        WarehouseDTO.UpdateDTO fromWarehouse = warehouseList.stream().filter(req -> req.getId().equals(detailEntityList.get(MathUtil.ZERO).getFromWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(fromWarehouse.getOrgId());

        //调拨类型
        if (toWarehouse.getOrgId().equals(fromWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId("");
        addDTO.setSourceCode("");
        addDTO.setRemark("");

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (RequisitionApplicationDTO.handleListDTO detailEntity : detailEntityList) {

            // TODO 是否需要拆分为子件
            //映射产品信息
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
//            TransferOutDetailDTO.AddDTO detailAddDto = FbaShipmentConverter.INSTANCE.fbaDeliveryDetailEntityToTransferOutDetailAdd(detailEntity);
//            TransferInfoDetailDTO.AddDTO detailAddDto = FbaShipmentConverter.INSTANCE.fbaDeliveryDetailEntityToTransferInfoDetailAdd(detailEntity);
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(detailEntity.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
            detailAddDto.setSkuId(skuVO.getSkuId());
            detailAddDto.setSkuNo(skuVO.getSkuNo());
            detailAddDto.setQty(detailEntity.getApproveQty());
            detailAddDto.setOutWarehouseId(detailEntity.getFromWarehouseId());
            detailAddDto.setInWarehouseId(detailEntity.getToWarehouseId());
//            detailAddDto.setOutWarehouseLocation(detailEntity.getWarehouseLocation());
//            detailAddDto.setInWarehouseLocation(detailEntity.getWarehouseLocation());
//            detailAddDto.setSourceDetailId(detailEntity.getId());
            detailAddDtoList.add(detailAddDto);
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }


    @Override
    public List<RequisitionApplicationDTO.finishListDTO> finishList(List<String> ids) {
        return null;
    }

    @Override
    public Boolean finishSave(List<RequisitionApplicationDTO.finishListDTO> list) {
        return null;
    }

    @Override
    public List<RequisitionApplicationDTO.printPickingViewDTO> printPickingView(List<String> ids) {
        return null;
    }

    @Override
    public BatchResultDTO cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean exportExcel(RequisitionApplicationDTO.PagingParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public List<RequisitionApplicationEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(RequisitionApplicationEntity::getSourceId, sourceIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationEntity requisitionApplicationEntity) {

    }

    /**
     * 详情字段处理
     */
    private void fillOne(RequisitionApplicationDTO.ViewDTO data, List<RequisitionApplicationDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //来源类型中文
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        //要货类型中文
        data.setTypeName(RequisitionApplicationTypeEnum.getName(data.getType()));
        //单据状态中文
        data.setStatus(RequisitionApplicationStatusEnum.getName(data.getStatus()));

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
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
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
        for (RequisitionApplicationDTO.ListDTO listDTO : list) {
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
        if(entity.getStatus().equals(ApproveStatusEnum.WAIT_SUBMIT) ) {
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


}
