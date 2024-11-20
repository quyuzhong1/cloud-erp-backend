package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.MachineSourceTypeEnum;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeMachineInfoService;
import com.erp.server.wms.mabang.SyncMabangMachineService;
import com.erp.server.wms.mapper.MachineInfoMapper;
import com.erp.server.wms.query.MachineInfoQueryHandler;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_MACHINE_INFO;

/**
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class MachineInfoServiceImpl extends SuperServiceImpl<MachineInfoMapper, MachineInfoEntity> implements MachineInfoService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private CommonService commonService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SyncKingdeeMachineInfoService syncKingdeeMachineInfoService;

    @Resource
    private SyncMabangMachineService syncMabangMachineService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private MachineRefSoService machineRefSoService;

    @Resource
    private MachineInfoQueryHandler machineInfoQueryHandler;

    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;

    @Override
    public PagingVO<MachineInfoDTO.ListDTO> paging(PagingDTO<MachineInfoDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<MachineInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<MachineInfoDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records,false);
        return new PagingVO(pageData);
    }

    @Override
    public List<MachineInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<MachineInfoDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            MachineInfoDTO.SearchParamDTO searchParamDTO = new MachineInfoDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            MachineInfoDTO.ListStatusCountDTO resultDTO = new MachineInfoDTO.ListStatusCountDTO();
            String tabSql = machineInfoQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(MachineInfoDTO.AddDTO dto) {
        MachineInfoEntity entity = new MachineInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(), entity);
        log.info("加工单新增");
        List<String> checkSkuIdList=dto.getDetailList().stream().
                map(MachineDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        checkSkuIsCombination(checkSkuIdList);
        //生成单号
        String code =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZZCX);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个加工单【%s】", code), ModuleTypeEnum.MACHINE_INFO.getCode(), entity.getId(), "新增操作");
            //新增明细
            machineDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }


    /**
     * @description
     * @param checkSkuIdList 检查 的sku
     * @return
     * @date 2024-02-28 11:58
     * @author Lambda
     */
    private void checkSkuIsCombination(List<String> checkSkuIdList) {
        List<BomChildrenSkuDTO> bomSkuList = plmTaskFeign.listBomChildBySkuIds(checkSkuIdList);
        //套装
        String combinationType = BomTypeEnum.COMBINATION.getType();
        //套装bom的父级sku
        List<String> parentSkuIdList = bomSkuList.stream().filter(b-> combinationType.equals(b.getType())).
                map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());

        List<String> notExistSkuIdList = checkSkuIdList.stream().filter(c -> !parentSkuIdList.contains(c)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(notExistSkuIdList)){
            throw new ServiceException("存在非销售套装的sku");
        }

    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(MachineInfoDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Collections.singletonList(id));
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(MachineInfoDTO.UpdateDTO dto) {

        MachineInfoEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        List<String> checkSkuIdList=dto.getDetailList().stream().
                map(MachineDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        checkSkuIsCombination(checkSkuIdList);

        MachineInfoEntity entity = new MachineInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        List<MachineDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(), entity);

        // 此处增加限制，如果是从FBA发货单同步下来生成的加工单不允许新增或移除SKU
        List<MachineDetailEntity> originMachineDetailList = machineDetailService.listByMainId(dto.getId());
        checkFbaMachine(old, originMachineDetailList, dto.getDetailList());

        log.info("加工单修改，id=【{}】", dto.getId());

        //添加日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.MACHINE_INFO.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        machineDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(MachineInfoDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Collections.singletonList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("加工单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个加工单【%s】", ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public MachineInfoDTO.ViewDTO view(String id) {
        MachineInfoDTO.ViewDTO viewDTO = new MachineInfoDTO.ViewDTO();
        //主表信息
        MachineInfoEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<MachineDetailEntity> detailList = machineDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        List<MachineDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(MachineDetailDTO.ViewDTO.class, detailList);
        //查询明细子件
        List<String> detailIdList = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailIds(detailIdList);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            throw new ServiceException(ApiError.ERROR_99056);
        }
        //库位信息查询
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList1 = detailList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(entity.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList2 = machineSubComponentsList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = Stream.concat(paramList1.stream(), paramList2.stream())
                .collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        //子件仓库id集合
        List<String> warehouseIdList = machineSubComponentsList.stream().map(MachineSubComponentsEntity::getWarehouseId).distinct().collect(Collectors.toList());
        warehouseIdList.add(viewDTO.getWarehouseId());

        //明细skuId集合
        List<String> skuIds = detailList.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //查询BOM中SKU子集
        List<BomChildrenSkuDTO> childrenList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);

        //子件skuId集合
        List<String> subComponentsSkuIdList = machineSubComponentsList.stream().map(MachineSubComponentsEntity::getSkuId).collect(Collectors.toList());
        skuIds.addAll(subComponentsSkuIdList);
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        //组织
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIds);
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //可用数量
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

        for (MachineDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDetailDTO.setProductName(productName);
            }
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = skuInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(),viewDetailDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(),viewDTO.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getWarehouseLocationId(),viewDetailDTO.getWarehouseLocation()))
                    .map(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO, Integer::sum);
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
            //仓位信息
            WarehouseLocationEntity warehouseLocationEntity1 = warehouseLocationEntityList.stream().filter(e -> Objects.nonNull(e) && e.getWarehouseId().equals(entity.getWarehouseId())
                    && e.getCode().equals(viewDetailDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDetailDTO.setWarehouseLocationName(warehouseLocationEntity1.getName());
            //明细子件
            List<MachineSubComponentsEntity> subComponentsList = machineSubComponentsList.stream().filter(obj -> CharSequenceUtil.equals(viewDetailDTO.getId(), obj.getDetailId())).collect(Collectors.toList());
            List<MachineSubComponentsDTO.ViewDTO> subComponentsDTOList = BeanMapperUtils.copyList(MachineSubComponentsDTO.ViewDTO.class, subComponentsList);

            for (MachineSubComponentsDTO.ViewDTO subComponentsDTO : subComponentsDTOList) {
                //产品信息
                SkuVO skuVO = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), subComponentsDTO.getSkuId())).findFirst().orElse(new SkuVO());
                subComponentsDTO.setSkuNo(skuVO.getSkuNo());
                subComponentsDTO.setProductName(skuVO.getSkuName());

                //根据组织、仓库、sku查询可用库存
                Integer  subComponentsQty = skuInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(),subComponentsDTO.getSkuId())
                                && CharSequenceUtil.equals(obj.getWarehouseId(),subComponentsDTO.getWarehouseId())
                                && CharSequenceUtil.equals(obj.getWarehouseLocationId(),subComponentsDTO.getWarehouseLocation()))
                        .map(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO, Integer::sum);
                subComponentsDTO.setCurInventoryQty(subComponentsQty);

                //bom用量
                Integer quantity = childrenList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), viewDetailDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getSkuId(), subComponentsDTO.getSkuId())
                ).map(BomChildrenSkuDTO::getQuantity).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(quantity)) {
                    throw new ServiceException(ApiError.ERROR_95173,viewDetailDTO.getSkuNo());
                }
                subComponentsDTO.setItemQty(quantity);
                //仓位信息
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> Objects.nonNull(e) && e.getWarehouseId().equals(subComponentsDTO.getWarehouseId())
                        && e.getCode().equals(subComponentsDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                subComponentsDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
            }
            viewDetailDTO.setSubComponentsList(subComponentsDTOList);
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    public List<MachineSubComponentsDTO.ViewDTO> viewBomSubComponents(MachineSubComponentsDTO.ViewBomParamDTO dto) {
        List<MachineSubComponentsDTO.ViewDTO> resultList = new ArrayList<>();

        //查询BOM中SKU子集
        List<BomChildrenSkuDTO> childrenList = plmTaskFeign.listHistoryBomChildBySkuIds(Collections.singletonList(dto.getSkuId()));
        if (CollectionUtils.isEmpty(childrenList)) {
            return resultList;
        }
        //查询sku
        List<String> skuIdList = childrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuPurchaseByIds(skuIdList);
        //bom版本取最新
        String bomVersion = childrenList.stream().max(Comparator.comparingDouble(obj -> Double.valueOf(obj.getBomVersion()))).map(BomChildrenSkuDTO::getBomVersion).get();
        dto.setBomVersion(MathUtil.compareTo(dto.getBomVersion(),MathUtil.ZERO) == MathUtil.ZERO ? bomVersion : dto.getBomVersion());
        String combinationType = BomTypeEnum.COMBINATION.getType();
        List<BomChildrenSkuDTO> versionChildList = childrenList.stream().
                filter(obj -> obj.getBomVersion().equals(dto.getBomVersion()) &&( combinationType.equals(obj.getType()) || BomTypeEnum.SINGLE.getType().equals(obj.getType()))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(versionChildList)) {
            return resultList;
        }
        Boolean childHidden = Boolean.FALSE;
        for (BomChildrenSkuDTO bomChildrenSkuDTO : versionChildList) {
            //sku信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            MachineSubComponentsDTO.ViewDTO viewDTO = new MachineSubComponentsDTO.ViewDTO();
            if (!childHidden) {
                childHidden = Boolean.TRUE;
                viewDTO.setChildHidden(Boolean.TRUE);
            }
            viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
            viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
            viewDTO.setProductName(bomChildrenSkuDTO.getSkuName());
            viewDTO.setChildSupplierId(skuVO.getSupplierId());
            viewDTO.setUnit(bomChildrenSkuDTO.getUnitName());
            viewDTO.setItemQty(bomChildrenSkuDTO.getQuantity());
            viewDTO.setQty(bomChildrenSkuDTO.getQuantity());
            viewDTO.setBomVersion(bomChildrenSkuDTO.getBomVersion());
            viewDTO.setChildLength(versionChildList.size());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(MachineInfoEntity::getId,id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId),MachineInfoEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    @Override
    public List<MachineInfoEntity> findBySourceTypeAndSourceCode(String sourceType, String sourceCode) {
        return lambdaQuery().eq(MachineInfoEntity::getSourceType, sourceType).eq(MachineInfoEntity::getSourceCode, sourceCode).list();
    }

    @Override
    public List<MachineSubComponentsDTO.ViewDTO> viewSubComponents(String detailId) {
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailId(detailId);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            return Collections.emptyList();
        }
        List<MachineSubComponentsDTO.ViewDTO> resultList = BeanMapperUtils.copyList(MachineSubComponentsDTO.ViewDTO.class, machineSubComponentsList);

        List<String> skuIds = resultList.stream().map(MachineSubComponentsDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //仓库信息
        List<String> warehouseIds = machineSubComponentsList.stream().map(MachineSubComponentsEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        for (MachineSubComponentsDTO.ViewDTO viewDTO : resultList) {
            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            viewDTO.setSkuNo(skuVO.getSkuNo());
            viewDTO.setProductName(skuVO.getSkuName());
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getWarehouseId(), viewDTO.getSkuId(), viewDTO.getWarehouseLocation());
            viewDTO.setCurInventoryQty(curInventoryQty);
        }

        return  resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("加工单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除子件明细
        machineSubComponentsService.removeByMainIds(ids);
        //删除明细数据
        machineDetailService.removeByMainIds(ids);
        //删除关联关系数据
        machineRefSoService.removeByMachineIdList(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的加工单", commonService.getUserInfo().getUserName(), list.stream().map(MachineInfoEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "删除操作");
        //删除发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("加工单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(MachineInfoEntity::getInvalidRemark, reason)
                .update();
        //删除关联关系表数据
        machineRefSoService.removeByMachineIdList(ids);
        //作废发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个加工单【%s】，作废原因：".concat(reason), ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(MachineInfoEntity entity, String type, String comment, Boolean isNeedProcess) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        log.info("加工单【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("加工单【{}】审核通过，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE.getStatus());
            //更新库存
            updateInventoryTransCore(entity);
            //审核发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
            // 发送马帮
            syncMabangMachineService.syncDataToMabang(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("加工单【{}】审核不通过，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个加工单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.MACHINE_INFO.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(MachineInfoEntity entity) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        //已存在直接调拨单
        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            List<String> codes = transferInfoList.stream().map(TransferInfoEntity::getSourceCode).collect(Collectors.toList());
            throw new ServiceException(ApiError.ERROR_MACHINE_EXIST_TRANSFER_INFO,codes);
        }
        //已存在采购退货单
        List<PoReturnEntity> purchaseReturnOrderList = poReturnService.listBySourceIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(purchaseReturnOrderList)) {
            List<String> codes = transferInfoList.stream().map(TransferInfoEntity::getSourceCode).collect(Collectors.toList());
            throw new ServiceException(ApiError.ERROR_MACHINE_EXIST_PURCHASE_RETURN,codes);
        }

        log.info("加工单反审核，id=【{}】", entity.getId());

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO(InventorySourceTypeEnum.MACHINE_INFO,entity.getId());
        inventoryTransCoreService.unApprove(inventoryUnApproveDTO);

        //虚拟仓回退库存
        virtualInventoryTransCoreService.unApprove(inventoryUnApproveDTO);

        //反审核发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        // 发送马帮
        // TODO 此处可能存在一个加工单有些是从FBA发货单同步过来的父子级，需要判断过滤
        syncMabangMachineService.syncDataToMabang(entity, SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个加工单【%s】", entity.getCode()), ModuleTypeEnum.MACHINE_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("加工单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("加工单【%s】取消流程", ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("加工单导出", EXPORT_WMS_MACHINE_INFO.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
     * @description: 更新加工单库存
     * @author Will
     * @date: 2023/5/18 12:11
     * @param entity
     */
    private void updateInventoryTransCore (MachineInfoEntity entity) {
        //加工明细
        List<MachineDetailEntity> detailList = machineDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        //加工子件明细
        List<String> detailIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailIds(detailIds);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            throw new ServiceException(ApiError.ERROR_99056);
        }
        //父级SKU库存更新
        updateInventoryForMachineDetail(entity,detailList);

        //子件SKU库存更新
        updateInventoryForMachineSubComponents(entity,machineSubComponentsList);

        //头程发货单下推子件虚拟仓出库
        updateFirstMileVirtualInventory(entity,detailList,machineSubComponentsList);
    }

    /**
     * 头程发货单下推子件虚拟仓出库
     * @author will
     * @date 2024/11/18 14:11
     * @param entity
     * @param detailList
     * @param machineSubComponentsList
     */
    private void updateFirstMileVirtualInventory (MachineInfoEntity entity,List<MachineDetailEntity> detailList,List<MachineSubComponentsEntity> machineSubComponentsList) {
        //头程发货单下推数据扣减虚拟仓库存
        if (!CharSequenceUtil.equals(entity.getSourceType(),SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())) {
            return;
        }
        List<String> detailIdList = detailList.stream().map(MachineDetailEntity::getId).distinct().collect(Collectors.toList());
        List<MachineRefSoEntity> machineList = machineRefSoService.listByMachineDetailIdList(detailIdList);
        if (CollectionUtils.isEmpty(machineList)) {
            return;
        }
        //头程单据
        List<String> sourceDetailIds = machineList.stream().map(MachineRefSoEntity::getSoDetailId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailList = firstMileDeliveryDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryDetailList)) {
            return;
        }
        //要货申请
        List<String> applicationDetailIdList = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailList = requisitionApplicationDetailService.listByIds(applicationDetailIdList);
        if (CollectionUtils.isEmpty(requisitionApplicationDetailList)) {
            return;
        }
        List<VirtualInventoryStockDTO.OutInStockDTO>  outInStockList = new ArrayList<>();
        for (MachineSubComponentsEntity detailEntity : machineSubComponentsList) {
            //直接调拨单明细
            String detailId = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getDetailId()))
                    .map(MachineDetailEntity::getId).findFirst().orElse("");
            //头程发货单明细id
            String refDetailId = machineList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMachineDetailId(), detailId))
                    .map(MachineRefSoEntity::getSoDetailId).findFirst().orElse("");
            //要货申请明细id
            String applicationDetailId = firstMileDeliveryDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), refDetailId))
                    .map(FirstMileDeliveryDetailEntity::getSourceDetailId).findFirst().orElse("");

            //要货申请明细虚拟仓id
            String virtualWarehouseId = requisitionApplicationDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), applicationDetailId))
                    .map(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId).findFirst().orElse("");
            if (CharSequenceUtil.isBlank(virtualWarehouseId)) {
                continue;
            }
            //操作请求实体
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(detailEntity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(virtualWarehouseId);
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setQty(detailEntity.getQty());
            outInStockDTO.setSourceId(entity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.MACHINE_INFO);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockList.add(outInStockDTO);
        }
        //库存扣减
        VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
        stockParamDTO.setParamList(outInStockList);
        stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.MACHINE_INFO_CHILD_OUT.getCode());
        virtualInventoryTransCoreService.approve(stockParamDTO);
    }

    /**
     * @description: 父级SKU库存更新
     * @author Will
     * @date: 2023/5/18 12:10
     * @param entity
     * @param resultDetails
     */
    private void updateInventoryForMachineDetail (MachineInfoEntity entity ,List<MachineDetailEntity> resultDetails) {
        List<InOutStockDTO>  inOutStockList = new ArrayList<>();
        for (MachineDetailEntity detailEntity : resultDetails) {
            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.MACHINE_INFO);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(entity.getBillDate());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getQty());
            inOutStockDTO.setWarehouseId(entity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //组装父SKU增加库存，拆卸父SKU减少库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        if (WorkTypeEnum.ASSEMBLE.getCode().equals(entity.getWorkType())) {
            SoDeliveryNoticeEntity notice = soDeliveryNoticeService.getDeliveryNoticeBySourceId(entity.getSourceId());
            if ((SourceTypeEnum.SO_INFO.getCode().equals(entity.getSourceType()) && ObjectUtils.isNotEmpty(notice))|| SourceTypeEnum.FIRST_MILE_DELIVERY.getCode().equals(entity.getSourceType())) {
                inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.ASSEMBLE_IN_PARENT_FREEZE.getCode());
            }else {
                inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.ASSEMBLE_IN_PARENT.getCode());
            }
        } else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.DISASSEMBLE_IN_PARENT.getCode());
        }
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * @description: 子件SKU库存更新
     * @author Will
     * @date: 2023/5/18 12:10
     * @param entity
     * @param machineSubComponentsList
     */
    private void updateInventoryForMachineSubComponents (MachineInfoEntity entity ,List<MachineSubComponentsEntity> machineSubComponentsList) {

        //父级SKU库存更新
        List<InOutStockDTO>  inOutStockList = new ArrayList<>();
        for (MachineSubComponentsEntity detailEntity : machineSubComponentsList) {
            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.MACHINE_INFO);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(entity.getBillDate());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getQty());
            inOutStockDTO.setWarehouseId(detailEntity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //组装父SKU增加库存，拆卸父SKU减少库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        if (WorkTypeEnum.ASSEMBLE.getCode().equals(entity.getWorkType())) {
            SoDeliveryNoticeEntity notice = soDeliveryNoticeService.getDeliveryNoticeBySourceId(entity.getSourceId());
            if ((SourceTypeEnum.SO_INFO.getCode().equals(entity.getSourceType()) && ObjectUtils.isNotEmpty(notice))|| SourceTypeEnum.FIRST_MILE_DELIVERY.getCode().equals(entity.getSourceType())) {
                inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.ASSEMBLE_IN_CHILD_FREEZE.getCode());
            }else {
                inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.ASSEMBLE_IN_CHILDD.getCode());
            }
        } else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.DISASSEMBLE_IN_CHILD.getCode());
        }
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<MachineInfoDTO.ListDTO> records,boolean isExport) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(MachineInfoDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (MachineInfoDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (CharSequenceUtil.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);

            obj.setWorkTypeName(WorkTypeEnum.getByCode(obj.getWorkType()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

            if(isExport){
                obj.setSourceType(EnumMessage.getNameByCode(MachineSourceTypeEnum.class,obj.getSourceType()));
            }
        }
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String warehouseId, String receiveOrgId, String warehouseKeeperId,String receiverId, MachineInfoEntity entity) {

        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId,receiverId));
        if (CollectionUtils.isNotEmpty(userList)) {
            //仓管员
            String warehouseKeeperName = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).map(FindUserDTO::getUserName).findFirst().orElse("");
            entity.setWarehouseKeeperName(warehouseKeeperName);
            //领料员
            String receiverName = userList.stream().filter(obj -> obj.getUserId().equals(receiverId)).map(FindUserDTO::getUserName).findFirst().orElse("");
            entity.setReceiverName(receiverName);
        }
        //仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if  (ObjectUtils.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        entity.setWarehouseName(warehouseEntity.getName());
        //库存组织
        String inventoryOrgId = warehouseEntity.getOrgId();
        entity.setInventoryOrgId(inventoryOrgId);
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(inventoryOrgId, receiveOrgId));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //库存组织名称
        String inventoryOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(inventoryOrgId)).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setInventoryOrgName(inventoryOrgName);
        //收料组织名称
        String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(receiveOrgId)).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setReceiveOrgName(receiveOrgName);
    }

    /**
     * 根据ids查询数据
     */
    private List<MachineInfoEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<MachineInfoEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getApproveUserId, userInfo.getUid())
                .set(MachineInfoEntity::getApproveUserName, userInfo.getUserName())
                .set(MachineInfoEntity::getApproveStatus, approveStatus)
                .set(MachineInfoEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getApproveStatus, approveStatus)
                .set(MachineInfoEntity::getApproveUserId, "")
                .set(MachineInfoEntity::getApproveUserName, "")
                .set(MachineInfoEntity::getApproveTime, null)
                .update();
    }

    /**
     * 检查FBA发货单生成的加工单修改是否新增或移除了SKU
     * @param machineInfoEntity
     * @param originDetailList
     * @param updateDetailList
     */
    private void checkFbaMachine(MachineInfoEntity machineInfoEntity, List<MachineDetailEntity> originDetailList, List<MachineDetailDTO.UpdateDTO> updateDetailList) {
        if(!Objects.equals(machineInfoEntity.getSourceType(), SourceTypeEnum.MABANG_FBA_DELIVERY.getCode())) {
            return;
        }
        // 原加工单父级SKU集合
        List<String> originParentSkuList = originDetailList.stream().map(MachineDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        // 提交的加工单父级SKU集合
        List<String> updateParentSkuList = updateDetailList.stream().map(MachineDetailDTO.UpdateDTO::getSkuNo).distinct().collect(Collectors.toList());
        if(originParentSkuList.size() != updateParentSkuList.size()) {
            throw new ServiceException("FBA发货单同步生成的加工单不允许新增或移除SKU");
        }
        updateDetailList.stream().forEach(updateSku->{
            if(!originParentSkuList.contains(updateSku.getSkuNo())) {
                throw new ServiceException("FBA发货单同步生成的加工单不允许新增或移除SKU");
            }
        });
    }

    @Override
    public List<MachineInfoEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery()
                .in(MachineInfoEntity::getSourceId,ids)
                .eq(MachineInfoEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    @Override
    public List<MachineInfoDTO.ListDTO> listBySku(MachineInfoDTO.FindInfoBySkuDTO dto) {
        return baseMapper.listBySku(dto);
    }

    @Override
    public PagingVO<MachineInfoDTO.ListDTO> exportMachineInfo(PagingDTO<MachineInfoDTO.SearchParamDTO> dto) {
        Page<MachineInfoDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            doOpHandleData(page.getRecords(),true);
        }
        return new PagingVO<>(page);
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<MachineInfoEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeMachineInfoService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }
}
