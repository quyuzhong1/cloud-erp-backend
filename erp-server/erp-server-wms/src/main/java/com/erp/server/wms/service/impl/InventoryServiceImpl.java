package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.ExportInventoryExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.inventory.InventorySaveDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  12:17
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryServiceImpl extends SuperServiceImpl<InventoryMapper, InventoryEntity> implements InventoryService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public InventoryEntity findInventory(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(status);
        String qWarehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);
        /**
         * 不控制库位把库位条件置位空字符串（从空库位查询）；
         * 其他控制库位的如果传了则从指定库位出，没传则从空库位出
         */
        if (Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
            qWarehouseLocationId = "";
        }
        queryWrapper.eq(InventoryEntity::getWarehouseLocation, qWarehouseLocationId).last("limit 1");
        InventoryEntity inventory = baseMapper.selectOne(queryWrapper);
        return inventory;
    }

    @Override
    public InventoryEntity findInventoryLock(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        // 此处使用读写锁，避免并发情况下读取的数据不一致，读跟读之间不冲突，读写或写写冲突，暂不考虑库位
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(status);
        String lockKey = StrUtil.format("{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, skuId);
        RReadWriteLock rwLock = redisson.getReadWriteLock(lockKey);
        RLock rlock = rwLock.readLock();
        boolean isLock;
        try {
            isLock = rlock.tryLock(8, TimeUnit.SECONDS);// 防止一直等待，加最大等待时间
            log.info("仓库：【{}】，SKU ID：【{}】，是否获取到锁: {}", warehouseId, skuId, isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            String qWarehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
            LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, orgId)
                    .eq(InventoryEntity::getSkuId, skuId)
                    .eq(InventoryEntity::getDictInventoryStatus, status);
            /**
             * 不控制库位把库位条件置位空字符串（从空库位查询）；
             * 其他控制库位的如果传了则从指定库位出，没传则从空库位出
             */
            if (Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
                log.info("库存状态：【{}】不控制库位", inventoryStatus.getName());
                qWarehouseLocationId = "";
            }
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, qWarehouseLocationId).last("limit 1");
            InventoryEntity inventory = baseMapper.selectOne(queryWrapper);
            return inventory;
        } catch (InterruptedException e) {
            log.error("仓库id：【{}】，SKU编号：【{}】，获取锁异常", warehouseId, skuId, e);
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            //释放锁
            if (rlock.isLocked() && rlock.isHeldByCurrentThread()) { // 锁是否存在，是当前执行线程的锁
                rlock.unlock(); // 释放锁
            }
        }
    }

    /**
     * 特别注意：库位为空，则赋值空库位查询
     *
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    @Override
    public InventoryEntity findInventoryIncLocation(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);

        warehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
        queryWrapper.eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId)).last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 特别注意：库位没传，不带库位条件查询
     *
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    @Override
    public List<InventoryEntity> findInventoryCheckLocation(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);

        if (StrUtils.isNotEmpty(warehouseLocationId)) {
            warehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId));
        }
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Integer getUsableInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId) {
        return this.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, InventoryStatusEnum.USABLE.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InventorySaveDTO addOrUpdate(String warehouseId, String orgId, String warehouseLocation, String skuId, String skuNo, String inventoryStatus, Integer qty) {
        warehouseLocation = StrUtils.null2EmptyWithTrim(warehouseLocation);
        // 此处注意，入库传不传仓位都带仓位条件查询
        InventoryEntity inventory = this.findInventory(orgId, warehouseId, skuId, warehouseLocation, inventoryStatus);
        Integer originInventoryQty = 0; // 库存原数量
        if (Objects.isNull(inventory)) {
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中不存在数据，新增数据", inventoryStatus, warehouseId, orgId, warehouseLocation, skuId, skuNo);
            inventory = new InventoryEntity();
            inventory.setWarehouseId(warehouseId);
            inventory.setOrgId(orgId);
            inventory.setWarehouseLocation(warehouseLocation);
            inventory.setSkuId(skuId);
            inventory.setSkuNo(skuNo);
            inventory.setDictInventoryStatus(inventoryStatus);
            inventory.setQty(qty);
            inventory.setVersion(1);
            boolean save = super.save(inventory);
            ValidatorUtil.isTrue(save, () -> new ServiceException("库存数据保存失败"));
        } else {
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中存在数据，修改数据", inventoryStatus, warehouseId, orgId, warehouseLocation, skuId, skuNo);
            originInventoryQty = inventory.getQty();
            // 更新实时库存表数量
            int updateCnt = this.updateQtyById(inventory.getId(), qty, inventory.getVersion());
            if (updateCnt != 1) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
        }
        InventorySaveDTO inventorySaveDTO = new InventorySaveDTO(inventory.getId(), originInventoryQty);
        return inventorySaveDTO;
    }

    /**
     * 根据skuIds 仓库 ，组织 仓位 获取到 sku即时库存（特别注意：没有传库位，则库位为空）
     *
     * @param skuIds
     * @param warehouseId
     * @param warehouseLocationId
     * @return java.util.List<com.erp.model.wms.dto.InventoryDTO.SkuInventoryTotalDTO>
     * @author yl
     * @date 2023-05-16 17:06
     */
    @Override
    public List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(List<String> skuIds, String warehouseId, String warehouseLocationId, String status) {
        InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.of(status);
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException("库存状态错误"));
        WarehouseDTO.UpdateDTO warehouse = warehouseService.detailWithCache(warehouseId);
        ValidatorUtil.isTrue(Objects.nonNull(warehouse) && StrUtils.isNotEmpty(warehouse.getId()), () -> new ServiceException(ApiError.ERROR_99002));
        // sku id去重
        skuIds = skuIds.stream().distinct().collect(Collectors.toList());
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId)
                .eq(InventoryEntity::getDictInventoryStatus, status)
                .eq(InventoryEntity::getOrgId, warehouse.getOrgId())
                .eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId))
                .in(InventoryEntity::getSkuId, skuIds);

        List<InventoryEntity> inventoryEntities = baseMapper.selectList(queryWrapper);
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = Lists.newArrayList();
        Map<String, InventoryEntity> queryInventoryMap = inventoryEntities.stream().collect(Collectors.toMap(InventoryEntity::getSkuId, Function.identity()));
        for (String skuId : skuIds) {
            InventoryQtyDTO.SkuInventoryTotalDTO skuInventoryTotalDTO = new InventoryQtyDTO.SkuInventoryTotalDTO();
            skuInventoryTotalDTO.setSkuId(skuId);
            if (queryInventoryMap.containsKey(skuId)) {
                skuInventoryTotalDTO.setInventoryTotal(queryInventoryMap.get(skuId).getQty());
            } else {
                skuInventoryTotalDTO.setInventoryTotal(0);
            }
            skuInventoryList.add(skuInventoryTotalDTO);
        }
        return skuInventoryList;
    }


    /**
     * 根据仓库列表 库位 获取到对应数据
     *
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.inventory.InventoryQtyDTO.SkuInventoryTotalDTO>
     * @author yl
     * @date 2023-05-22 12:27
     */
    @Override
    public List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryParamDTO dto) {
        String status = dto.getInventoryStatus();
        InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.of(status);
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException("库存状态错误"));
        List<String> skuIds = dto.getSkuIdList();
        // sku id去重
        skuIds = skuIds.stream().distinct().collect(Collectors.toList());
        List<String> warehouseIdList = dto.getWarehouseIdList();
        List<String> warehouseLocationIdList = dto.getWarehouseLocationIdList();
        if (CollectionUtils.isEmpty(skuIds) || CollectionUtils.isEmpty(warehouseIdList) || CollectionUtils.isEmpty(warehouseLocationIdList)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(InventoryEntity::getWarehouseId, warehouseIdList)
                .eq(InventoryEntity::getDictInventoryStatus, status)
                .in(InventoryEntity::getWarehouseLocation, warehouseLocationIdList)
                .in(InventoryEntity::getSkuId, skuIds);

        List<InventoryEntity> inventoryEntities = baseMapper.selectList(queryWrapper);
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = Lists.newArrayList();
        for (InventoryEntity item : inventoryEntities) {
            InventoryQtyDTO.SkuInventoryTotalDTO result = new InventoryQtyDTO.SkuInventoryTotalDTO();
            result.setInventoryTotal(item.getQty());
            result.setSkuId(item.getSkuId());
            result.setWarehouseId(item.getWarehouseId());
            result.setWarehouseLocationId(item.getWarehouseLocation());
            skuInventoryList.add(result);
        }
        return skuInventoryList;

    }

    @Override
    public Integer getInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        InventoryEntity inventory = this.findInventoryIncLocation(orgId, warehouseId, skuId, warehouseLocationId, status);
        return Objects.isNull(inventory) ? 0 : inventory.getQty();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateQtyById(String id, Integer qty, Integer version) {
        LoginUser loginUser = commonService.getUserInfo();
        return inventoryMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Override
    public List<InventoryEntity> listPickingDetailInventory(PickingDetailDTO.InventoryParamDTO dto) {
        //根据组织、仓库、sku查询可用库存（没有传库位，则不带库位查询条件，主要是用于选择出库位）
        List<InventoryEntity> inventoryList = this.findInventoryCheckLocation(dto.getOrgId(), dto.getWarehouseId(), dto.getSkuId(), null, InventoryStatusEnum.USABLE.getCode());

        log.info("组织【{}】、仓库【{}】、SKU【{}】查询可用库存", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo());

        if (CollectionUtils.isEmpty(inventoryList)) {
            throw new ServiceException(new ApiResult(1, String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo())));
        }

        Integer inventoryQty = inventoryList.stream().map(InventoryEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
        if (MathUtil.compareTo(dto.getQty(), inventoryQty) > 0) {
            throw new ServiceException(new ApiResult(1, String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo())));
        }

        /**
         * 拣货规则：
         * 1、如果可用库存存在超过拣货数量则直接顺序取
         * 2、如果可用库存不存在超过拣货数量则倒序取（如果剩余拣货数量与库存数量匹配则直接取）
         */

        List<InventoryEntity> resultList = new ArrayList<>();

        //拣货数量
        Integer qty = dto.getQty();

        //1、如果可用库存存在超过拣货数量则直接顺序取
        InventoryEntity inventoryEntity = inventoryList.stream().filter(obj -> obj.getQty().intValue() >= dto.getQty().intValue()).sorted(Comparator.comparing(InventoryEntity::getQty)).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(inventoryEntity)) {
            inventoryEntity.setQty(qty);
            resultList.add(inventoryEntity);
            return resultList;
        }
        //2、如果可用库存不存在超过拣货数量则倒序取（如果剩余拣货数量与库存数量匹配则直接取）
        List<InventoryEntity> sortList = inventoryList.stream().filter(obj -> dto.getQty().intValue() > obj.getQty().intValue()).sorted(Comparator.comparing(InventoryEntity::getQty).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sortList)) {
            throw new ServiceException(new ApiResult(1, String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo())));
        }
        for (InventoryEntity inventory : sortList) {
            //如果拣货数量为0则跳出循环
            if (MathUtil.compareTo(qty, MathUtil.ZERO) == MathUtil.ZERO) {
                break;
            }
            //剩余拣货数量
            qty = qty - inventory.getQty();

            //添加拣货明细
            resultList.add(inventory);

            //判断剩余数量是否存在相同库存数量，如果存在则直接匹配
            Integer finalQty = qty;
            List<String> inventoryIds = resultList.stream().map(InventoryEntity::getId).collect(Collectors.toList());
            InventoryEntity matches = inventoryList.stream().filter(obj -> obj.getQty().intValue() == finalQty.intValue() && !inventoryIds.contains(obj.getId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(matches)) {
                //添加拣货明细
                resultList.add(matches);
                break;
            }

        }
        return resultList;
    }

    @Override
    public PagingVO<InventoryDTO.PagingViewDTO> paging(PagingDTO<InventoryDTO.SearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.PagingViewDTO> pageData = this.baseMapper.page(query, pagingParamDTO.getParams());
        // 填充名称
        fillInventoryPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportExcel(InventoryDTO.ExportSearchParamDTO param, HttpServletResponse response) {
        // 如果是否选导出处理
        if (CollUtil.isNotEmpty(param.getCheckData())) {
            List<InventoryDTO.ExportInvParamDTO> checkData = param.getCheckData();
            List<String> warehouseIds = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getWarehouseId).distinct().collect(Collectors.toList());
            List<String> orgIds = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getOrgId).distinct().collect(Collectors.toList());
            List<String> skuIds = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getSkuId).distinct().collect(Collectors.toList());
            param.setWarehouseIdList(warehouseIds);
            param.setOrgIdList(orgIds);
            param.setSkuIdList(skuIds);
        }
        List<InventoryDTO.PagingViewDTO> dataList = inventoryMapper.exportInv(param);
        if (CollUtil.isEmpty(dataList)) {
            return;
        }
        // 填充名称
        fillInventoryPageData(dataList);
        List<ExportInventoryExcelDTO> resultList = BeanMapperUtils.copyList(ExportInventoryExcelDTO.class, dataList);
        String fileName = StrUtil.format("即时库存数据{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        try {
            ExcelUtil.exportAdapt(fileName, "即时库存数据", resultList, ExportInventoryExcelDTO.class, response, null);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }


    private void fillInventoryPageData(List<InventoryDTO.PagingViewDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 此处优化，取最新的产品名称和产品图片，防止数据没同步过来，销售状态和SPU则不取最新的，防止查询和显示不一样
        List<String> skuIds = list.stream().map(InventoryDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();
        list.stream().forEach(data -> {
            // 仓库名称赋值
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(data.getWarehouseId(), (v) -> warehouseService.detailWithCache(v));
            if (Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
                data.setWarehouseName(warehouseDetail.getName());
            }
            // 仓库组织
            SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(data.getOrgId(), (v) -> sysUserFeign.getCompanyById(v));
            if (Objects.nonNull(sysAccountingCompanyEntity)) {
                data.setOrgName(sysAccountingCompanyEntity.getCompanyName());
            }
            if (skuMap.containsKey(data.getSkuId())) {
                // 产品名称
                data.setProductName(skuMap.getOrDefault(data.getSkuId(), new SkuVO()).getSkuName());
                // 产品图片
                data.setProductImgUrl(skuMap.getOrDefault(data.getSkuId(), new SkuVO()).getSkuImagesUrl());
            }
            // 销售状态名称
            data.setSaleStateName(SaleStateEnum.getNameByCode(data.getSaleState()));
        });
    }

}