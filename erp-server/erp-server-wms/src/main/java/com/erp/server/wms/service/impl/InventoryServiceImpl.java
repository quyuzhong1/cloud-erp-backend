package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.FieldConstant;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.DmpSyncKingdeeDTO;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CfgUserRangeDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.enums.UserRangeTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryAgeTitleEnum;
import com.erp.model.wms.enums.inventory.InventorySearchDimensionEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpSyncFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY_AGE;

/**
 * @Classname: InventoryServiceImpl
 * @CreateTime: 2023-04-25  12:17
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryServiceImpl extends SuperServiceImpl<InventoryMapper, InventoryEntity> implements InventoryService {

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpSyncFeign dmpSyncFeign;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private AuthDataFeign authDataFeign;

    @Override
    public InventoryEntity findInventory(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.getByCode(status);

        // 1,空库位时，用空字符串 作为库位；
        // 2,不控制库位时，用空字符串作为库位 查询；
        String qWarehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
        if (Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
            qWarehouseLocationId = "";
        }

        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId)
                .eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status)
                .eq(InventoryEntity::getWarehouseLocation, qWarehouseLocationId)
                .last("limit 1")
        ;

        // 查询库存
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public InventoryEntity findInventoryLock(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        // 去掉分布式读锁
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.getByCode(status);
        String qWarehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);

        // 不控制库位把库位条件置位空字符串（从空库位查询）；其他控制库位的如果传了则从指定库位出，没传则从空库位出
        if (Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
            log.info("库存状态：【{}】不控制库位", inventoryStatus.getName());
            qWarehouseLocationId = "";
        }
        queryWrapper.eq(InventoryEntity::getWarehouseLocation, qWarehouseLocationId).last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

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
    public Integer getUsableInventoryTotal(String warehouseId, String skuId, String warehouseLocationId) {
        // 查询仓库下面的SKU的可用库存
        if (Objects.isNull(warehouseLocationId)) {
            return this.getUsableInventoryTotal(warehouseId, skuId);
        } else {
            // 查询仓库下面仓位的SKU可用库存
            WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
            if (Objects.isNull(warehouseEntity)){
                throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
            }
            return this.getInventoryTotal(warehouseEntity.getOrgId(), warehouseId, skuId, warehouseLocationId, InventoryStatusEnum.USABLE.getCode());
        }
    }

    @Override
    public Integer getUsableInventoryTotal(String warehouseId, String skuId) {
        // 查询仓库组织
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouseEntity)){
            throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
        }
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, warehouseEntity.getOrgId())
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode());

        List<InventoryEntity> list = baseMapper.selectList(queryWrapper);
        return CollUtil.isEmpty(list) ? 0 : list.stream().mapToInt(InventoryEntity::getQty).sum();
    }

    @Override
    public Integer getRealInventoryTotal(String warehouseId, String skuId) {
        // 查询仓库组织
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouseEntity)){
            throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
        }
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, warehouseEntity.getOrgId())
                .eq(InventoryEntity::getSkuId, skuId)
                .in(InventoryEntity::getDictInventoryStatus,Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));

        List<InventoryEntity> list = baseMapper.selectList(queryWrapper);
        return CollUtil.isEmpty(list) ? 0 : list.stream().mapToInt(InventoryEntity::getQty).sum();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public InventorySaveDTO addOrUpdate(String warehouseId, String orgId, String warehouseLocation, String skuId, String skuNo, String inventoryStatus, Integer qty) {
        warehouseLocation = StrUtils.null2EmptyWithTrim(warehouseLocation);
        // 此处注意，入库传不传仓位都带仓位条件查询
        InventoryEntity inventory = this.findInventory(orgId, warehouseId, skuId, warehouseLocation, inventoryStatus);
        // 库存原数量
        InventorySaveDTO inventorySaveDTO = new InventorySaveDTO("", 0, 0);
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
            inventorySaveDTO.setAfterQty(qty);
            inventory.setVersion(1);
            boolean save = super.save(inventory);
            ValidatorUtil.isTrue(save, () -> new ServiceException("库存数据保存失败"));
            inventorySaveDTO.setInventoryId(inventory.getId());
        } else {
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中存在数据，修改数据", inventoryStatus, warehouseId, orgId, warehouseLocation, skuId, skuNo);
            inventorySaveDTO.setBeforeQty(inventory.getQty());
            inventorySaveDTO.setAfterQty(inventory.getQty() + qty);
            // 更新实时库存表数量
            boolean updateFlag = this.updateQtyById(inventory.getId(), qty);
            if (!updateFlag) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
            inventorySaveDTO.setInventoryId(inventory.getId());
        }

        return inventorySaveDTO;
    }

    @Override
    public List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(List<String> skuIds, String warehouseId, String warehouseLocationId, String status) {
        InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.getByCode(status);
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException("库存状态错误"));
        WarehouseDTO.UpdateDTO warehouse = warehouseService.detailWithCache(warehouseId);
        ValidatorUtil.isTrue(Objects.nonNull(warehouse) && StrUtils.isNotEmpty(warehouse.getId()), () -> new ServiceException(ApiError.ERROR_99002));
        // sku id去重
        skuIds = skuIds.stream().distinct().collect(Collectors.toList());
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId);
        queryWrapper.eq(InventoryEntity::getDictInventoryStatus, status);
        queryWrapper.eq(InventoryEntity::getOrgId, warehouse.getOrgId());
        if (warehouseLocationId != null) {
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId));
        }
        queryWrapper.in(InventoryEntity::getSkuId, skuIds);

        List<InventoryEntity> inventoryEntities = baseMapper.selectList(queryWrapper);
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = Lists.newArrayList();
        Map<String, Integer> queryInventoryMap = inventoryEntities.stream().collect(Collectors.groupingBy(InventoryEntity::getSkuId, Collectors.summingInt(InventoryEntity::getQty)));
        for (String skuId : skuIds) {
            InventoryQtyDTO.SkuInventoryTotalDTO skuInventoryTotalDTO = new InventoryQtyDTO.SkuInventoryTotalDTO();
            skuInventoryTotalDTO.setSkuId(skuId);
            skuInventoryTotalDTO.setInventoryTotal(queryInventoryMap.getOrDefault(skuId, 0));
            skuInventoryList.add(skuInventoryTotalDTO);
        }
        return skuInventoryList;
    }

    @Override
    public List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryParamDTO dto) {
        String status = dto.getInventoryStatus();
        InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.getByCode(status);
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException("库存状态错误"));
        List<String> skuIds = dto.getSkuIdList();
        // sku id去重
        skuIds = skuIds.stream().distinct().collect(Collectors.toList());
        List<String> warehouseIdList = dto.getWarehouseIdList();
        List<String> warehouseLocationIdList = dto.getWarehouseLocationIdList();
        if (CollectionUtils.isEmpty(skuIds) || CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }

        boolean isExist = CollectionUtils.isNotEmpty(warehouseLocationIdList);
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(InventoryEntity::getWarehouseId, warehouseIdList);
        queryWrapper.eq(InventoryEntity::getDictInventoryStatus, status);
        queryWrapper.in(isExist, InventoryEntity::getWarehouseLocation, warehouseLocationIdList);
        queryWrapper.in(InventoryEntity::getSkuId, skuIds);

        List<InventoryEntity> inventoryEntities = this.list(queryWrapper);
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
    public List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryStatusParamDTO dto) {
        List<String> inventoryStatusList = dto.getInventoryStatusList();
        inventoryStatusList.forEach(obj -> {
            InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.getByCode(obj);
            ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException("库存状态错误"));
        });
        List<String> skuIds = dto.getSkuIdList();
        // sku id去重
        skuIds = skuIds.stream().distinct().collect(Collectors.toList());
        List<String> warehouseIdList = dto.getWarehouseIdList();
        List<String> warehouseLocationIdList = dto.getWarehouseLocationIdList();
        if (CollectionUtils.isEmpty(skuIds) || CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }

        Boolean isExist = CollectionUtils.isNotEmpty(warehouseLocationIdList);
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(InventoryEntity::getWarehouseId, warehouseIdList);
        queryWrapper.in(InventoryEntity::getDictInventoryStatus, inventoryStatusList);
        queryWrapper.in(isExist, InventoryEntity::getWarehouseLocation, warehouseLocationIdList);
        queryWrapper.in(InventoryEntity::getSkuId, skuIds);

        List<InventoryEntity> inventoryEntities = this.list(queryWrapper);
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryList = Lists.newArrayList();
        for (InventoryEntity item : inventoryEntities) {
            InventoryQtyDTO.SkuInventoryStatusTotalDTO result = new InventoryQtyDTO.SkuInventoryStatusTotalDTO();
            result.setInventoryTotal(item.getQty());
            result.setInventoryStatus(item.getDictInventoryStatus());
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
    public boolean updateQtyById(String id, Integer qty) {
//        LoginUser loginUser = UserContext.getDefaultLoginUser();
        boolean flag = lambdaUpdate()
                .setSql(CharSequenceUtil.format("{}={}+{}", "qty", "qty", qty))
//                .setSql(CharSequenceUtil.format("{}={}+{}", "version","version", 1))
//                .setSql(StrUtils.isNotEmpty(loginUser.getUid()), CharSequenceUtil.format("update_user_id='{}'", loginUser.getUid()))
//                .setSql(StrUtils.isNotEmpty(loginUser.getUserName()), CharSequenceUtil.format("update_user_name='{}'", loginUser.getUserName()))
                .eq(InventoryEntity::getId, id)
                .update(new InventoryEntity());

        if (!flag) {
            throw new ServiceException(ApiError.ERROR_1027);
        }

        return true;
    }

    @Override
    public List<InventoryEntity> listPickingDetailInventory(PickingDetailDTO.InventoryParamDTO dto) {

        //根据组织、仓库、sku查询可用库存（没有传库位，则不带库位查询条件，主要是用于选择出库位）
        List<InventoryEntity> inventoryList = this.findInventoryCheckLocation(dto.getOrgId(), dto.getWarehouseId(), dto.getSkuId(), null, InventoryStatusEnum.USABLE.getCode());

        log.info("组织【{}】、仓库【{}】、SKU【{}】查询可用库存", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo());

        if (CollectionUtils.isEmpty(inventoryList)) {
            ServiceException.runError(999, String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo()));
        }

        Integer inventoryQty = inventoryList.stream().map(InventoryEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
        if (MathUtil.compareTo(dto.getQty(), inventoryQty) > 0) {
            ServiceException.runError(999, String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo()));
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
        InventoryEntity inventoryEntity = inventoryList.stream().filter(obj -> obj.getQty() >= dto.getQty()).min(Comparator.comparing(InventoryEntity::getQty)).orElse(null);
        if (ObjectUtils.isNotEmpty(inventoryEntity)) {
            inventoryEntity.setQty(qty);
            resultList.add(inventoryEntity);
            return resultList;
        }
        //2、如果可用库存不存在超过拣货数量则倒序取（如果剩余拣货数量与库存数量匹配则直接取）
        List<InventoryEntity> sortList = inventoryList.stream().filter(obj -> dto.getQty() > obj.getQty()).sorted(Comparator.comparing(InventoryEntity::getQty).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sortList)) {
            ServiceException.runError(999, String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足", dto.getOrgName(), dto.getWarehouseName(), dto.getSkuNo()));
        }
        for (InventoryEntity inventory : sortList) {

            //如果拣货数量等于0则跳出循环
            if (MathUtil.compareTo(qty, MathUtil.ZERO) == MathUtil.ZERO) {
                break;
            }

            //本次拣货数量
            Integer thisQty;
            if (qty > inventory.getQty()) {
                thisQty = inventory.getQty();
            } else {
                thisQty = qty;
            }

            inventory.setQty(thisQty);
            //添加拣货明细
            resultList.add(inventory);

            //剩余拣货数量
            qty = qty - inventory.getQty();

            //判断剩余数量是否存在相同库存数量，如果存在则直接匹配
            Integer finalQty = qty;
            List<String> inventoryIds = resultList.stream().map(InventoryEntity::getId).collect(Collectors.toList());
            InventoryEntity matches = inventoryList.stream().filter(obj -> finalQty.intValue() != MathUtil.ZERO.intValue() && obj.getQty().intValue() == finalQty.intValue() && !inventoryIds.contains(obj.getId())).findFirst().orElse(null);
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
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.PagingViewDTO> pageData = new Page<>();
        AdvanceQueryDTO advanceQueryDTO = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().filter(e -> "dimension".equalsIgnoreCase(e.getField())).findFirst().orElse(null);
        if (null == advanceQueryDTO){
            ServiceException.runError("advanceQueryDTOList.field=dimension不能为空");
        }
        pagingParamDTO.getParams().setDimension(advanceQueryDTO.getValue().toString());
        if (InventorySearchDimensionEnum.WAREHOUSE.getCode().equalsIgnoreCase(pagingParamDTO.getParams().getDimension())) {
            // 检查不支持库区
            boolean hasAreaName = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().anyMatch(e -> "wl.area_name".equalsIgnoreCase(e.getField()));
            if (hasAreaName){
                ServiceException.runError("【按仓库】不支持库区查询");
            }
            List<AdvanceQueryDTO> newQuery = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().filter(e -> "dimension".equalsIgnoreCase(e.getField())).collect(Collectors.toList());
            pagingParamDTO.getParams().setAdvanceQueryDTOList(newQuery);
            pageData = this.baseMapper.page(query, pagingParamDTO.getParams());
        }
        if (InventorySearchDimensionEnum.WAREHOUSE_AREA.getCode().equalsIgnoreCase(pagingParamDTO.getParams().getDimension())) {
            List<AdvanceQueryDTO> newQuery = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().filter(e -> "dimension".equalsIgnoreCase(e.getField())).collect(Collectors.toList());
            pagingParamDTO.getParams().setAdvanceQueryDTOList(newQuery);
            pageData = this.baseMapper.pageByArea(query, pagingParamDTO.getParams());
        }
        if (InventorySearchDimensionEnum.WAREHOUSE_LOCATION.getCode().equalsIgnoreCase(pagingParamDTO.getParams().getDimension())) {
            // 检查不支持库区
            boolean hasAreaName = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().anyMatch(e -> "wl.area_name".equalsIgnoreCase(e.getField()));
            if (hasAreaName){
                ServiceException.runError("【按仓位】不支持库区查询");
            }
            List<AdvanceQueryDTO> newQuery = pagingParamDTO.getParams().getAdvanceQueryDTOList().stream().filter(e -> "dimension".equalsIgnoreCase(e.getField())).collect(Collectors.toList());
            pagingParamDTO.getParams().setAdvanceQueryDTOList(newQuery);
            if(CharSequenceUtil.isNotBlank(pagingParamDTO.getParams().getWarehouseLocationName())){
                List<WarehouseLocationEntity> list = warehouseLocationService.listByLocationName(pagingParamDTO.getParams().getWarehouseLocationName());
                if(!list.isEmpty()){
                    List<String> codeList = list.stream().map(WarehouseLocationEntity::getCode).distinct().collect(Collectors.toList());
                    pageData = this.baseMapper.pageByLocation(query, pagingParamDTO.getParams(), codeList);
                }
            }else {
                pageData = this.baseMapper.pageByLocation(query, pagingParamDTO.getParams(), null);
            }
        }
        // 填充名称
        fillInventoryPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportExcel(InventoryDTO.ExportSearchParamDTO param) {
        downloadTaskFeign.saveDownloadTask("即时库存导出", EXPORT_WMS_INVENTORY.getCode(), param);
    }

    private static void dealExportParams(InventoryDTO.ExportSearchParamDTO param) {
        if (CollUtil.isNotEmpty(param.getCheckData())) {
            List<InventoryDTO.ExportInvParamDTO> checkData = param.getCheckData();
            List<String> warehouseIds = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getWarehouseId).distinct().collect(Collectors.toList());
            List<String> orgIds = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getOrgId).distinct().collect(Collectors.toList());
            List<String> skuIds = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getSkuId).distinct().collect(Collectors.toList());
            List<String> areaNameList = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getWarehouseAreaCode).filter(item -> !CharSequenceUtil.isBlank(item)).distinct().collect(Collectors.toList());
            List<String> locationNameList = checkData.stream().map(InventoryDTO.ExportInvParamDTO::getWarehouseLocationCode).filter(item -> !CharSequenceUtil.isBlank(item)).distinct().collect(Collectors.toList());
            param.setWarehouseIdList(warehouseIds);
            param.setOrgIdList(orgIds);
            param.setSkuIdList(skuIds);
            param.setWarehouseAreaCodeList(areaNameList);
            param.setWarehouseLocationCodeList(locationNameList);
        }
    }

    @Override
    public PagingVO<LinkedHashMap> inventoryAgePaging(PagingDTO<InventoryReportDTO.InventoryAgeSearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());

        // 获取用户区间配置
        List<CfgUserRangeDTO.UserRangeDataDTO> userRanges = sysUserFeign.getUserRangeByType(UserRangeTypeEnum.INVENTORY_AGE.getCode(), Boolean.TRUE);
        List<InventoryReportDTO.InventoryAgeRangeDTO> userRangeList = BeanMapperUtils.copyList(InventoryReportDTO.InventoryAgeRangeDTO.class, userRanges);
        pagingParamDTO.getParams().setUserRangeList(userRangeList);

        // 此处注意，分页列表展示的是实时的实际库存，不是按区间设置的
        /**
         Integer startValue = userRangeList.get(0).getStartValue();
         LocalDate endDate = LocalDate.now().plusDays(startValue * -1);
         pagingParamDTO.getParams().setEndDate(endDate);
         */
        IPage<LinkedHashMap> pageData = this.baseMapper.inventoryAgePage(query, pagingParamDTO.getParams());
        // 标题及值赋值
        List<LinkedHashMap> dataList = fillInventoryAgePageData(pageData.getRecords(), userRangeList);
        pageData.setRecords(dataList);
        return new PagingVO(pageData);
    }

    @Override
    public void exportInventoryAge(InventoryReportDTO.ExportInventoryAgeSearchParamDTO paramDTO) {
        downloadTaskFeign.saveDownloadTask("库龄计算表数据", EXPORT_WMS_INVENTORY_AGE.getCode(), paramDTO);
    }

    @Override
    public List<InventoryEntity> listByStocktakingType(StocktakingPlanEntity entity, List<StocktakingPlanDetailEntity> detailEntityList) {
        StocktakingTypeEnum stocktakingType = entity.getType();
        LocalDateTime startTime = entity.getStartTime();
        LocalDateTime endTime = entity.getEndTime();
        if (!ObjectUtil.equals(stocktakingType, StocktakingTypeEnum.BY_SKU)) {
            if (ObjectUtil.isEmpty(startTime) || ObjectUtil.isEmpty(endTime)) {
                throw new ServiceException(CharSequenceUtil.format("盘点计划单 【{}】盘点时间不能为空", entity.getCode()));
            }
        }
        return baseMapper.listByStocktakingType(stocktakingType.getCode(), startTime, endTime, detailEntityList);
    }

    private void exportInventoryAgeExcel(List<LinkedHashMap> resultList, HttpServletResponse response) {
        LinkedHashMap headMap = (LinkedHashMap) resultList.get(0).get("head");
        List<LinkedHashMap> convertDataList = (List<LinkedHashMap>) resultList.get(0).get("data");

        OutputStream outputStream = null;
        // 声明一个工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFCellStyle contentCellStyle = wb.createCellStyle();
        // 水平居左
        contentCellStyle.setAlignment(HorizontalAlignment.LEFT);
        //垂直居中
        contentCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //自动换行
        contentCellStyle.setWrapText(true);
        //下边框
        contentCellStyle.setBorderBottom(BorderStyle.THIN);
        //左边框
        contentCellStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        contentCellStyle.setBorderTop(BorderStyle.THIN);
        //右边框
        contentCellStyle.setBorderRight(BorderStyle.THIN);

        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 13);
        CellStyle titleStyle = wb.createCellStyle();
        // 设置水平居中
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        // 设置垂直对齐的样式为居中对齐;
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        // 下边框
        titleStyle.setBorderBottom(BorderStyle.THIN);
        // 左边框
        titleStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        titleStyle.setBorderTop(BorderStyle.THIN);
        // 右边框
        titleStyle.setBorderRight(BorderStyle.THIN);

        CellStyle titleNoBorderStyle = wb.createCellStyle();
        //设置水平居中
        titleNoBorderStyle.setAlignment(HorizontalAlignment.LEFT);
        //设置垂直对齐的样式为居中对齐;
        titleNoBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleNoBorderStyle.setFont(titleFont);

        // 创建sheet页
        XSSFSheet sheet = wb.createSheet("库龄计算表");
        sheet.setDefaultColumnWidth(1 * 256);
        sheet.setColumnWidth(0, 25 * 256);
        sheet.setColumnWidth(3, 30 * 256);

        int rowNo = 0;
        // 第一行标题
        XSSFRow rowTitle0 = sheet.createRow(rowNo);

        // 标题
        int columnIndex = 0;
        for (Object key : headMap.keySet()) {
            Cell cell = rowTitle0.createCell(columnIndex);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(headMap.get(key)));
            ++columnIndex;
        }

        // 内容
        for (int i = 0, size = convertDataList.size(); i < size; i++) {
            LinkedHashMap dataMap = convertDataList.get(i);
            ++rowNo;

            XSSFRow rowContent = sheet.createRow(rowNo);
            columnIndex = 0;
            for (Object key : headMap.keySet()) {
                Cell cell = rowContent.createCell(columnIndex);
                cell.setCellStyle(contentCellStyle);
                // 产品信息处理
                if (Objects.equals(key, InventoryAgeTitleEnum.SKU_INFO.getCode())) {
                    cell.setCellValue(CharSequenceUtil.format("{}\n{}", StrUtils.null2EmptyWithTrim(dataMap.get("skuNo")),
                            StrUtils.null2EmptyWithTrim(dataMap.get("productName"))));
                } else {
                    cell.setCellValue(StrUtils.null2EmptyWithTrim(dataMap.get(key)));
                }
                ++columnIndex;
            }
        }

        String fileName = CharSequenceUtil.format("库龄计算表数据{}.xlsx", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            outputStream = response.getOutputStream();
            wb.write(outputStream);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

    }


    private void fillInventoryPageData(List<InventoryDTO.PagingViewDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 此处优化，取最新的产品名称和产品图片，防止数据没同步过来，销售状态和SPU则不取最新的，防止查询和显示不一样
        List<String> skuIds = list.stream().map(InventoryDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        //仓库
        List<String> warehouseIdList = list.stream().map(InventoryDTO.PagingViewDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        //组织
        List<String> orgIdList = list.stream().map(InventoryDTO.PagingViewDTO::getOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));

        List<String> warehouseIdIds = list.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIdIds);


        list.stream().parallel().forEach(data -> {
            // 仓库名称赋值
            WarehouseEntity warehouseEntity = warehouseList.stream().filter(obj -> obj.getId().equals(data.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(warehouseEntity)) {
                data.setWarehouseName(warehouseEntity.getName());
            }
            // 仓库组织
            BaseIdDTO.CodeDTO orgDTO = orgList.stream().filter(obj -> obj.getId().equals(data.getOrgId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(orgDTO)) {
                data.setOrgName(orgDTO.getName());
            }
            if (skuMap.containsKey(data.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(data.getSkuId()))) {
                SkuVO skuVO = skuMap.get(data.getSkuId()).get(0);
                // 产品名称
                data.setProductName(skuVO.getSkuName());
                // 产品图片
                data.setProductImgUrl(skuVO.getSkuImagesUrl());
                //产品id
                data.setProductId(skuVO.getProductId());
                //规格类型
                data.setSpecType(skuVO.getSpecType());
            }

            //仓位名称
            if (CharSequenceUtil.isNotBlank(data.getWarehouseLocation())) {
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream()
                        .filter(req -> req.getCode().equals(data.getWarehouseLocation())
                                && req.getWarehouseId().equals(data.getWarehouseId())
                        ).findFirst().orElse(new WarehouseLocationEntity());
                data.setWarehouseLocationName(warehouseLocationEntity.getName());
            }

            //库区名称赋值
            if (CharSequenceUtil.isNotBlank(data.getWarehouseArea())) {
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream()
                        .filter(req -> req.getCode().equals(data.getWarehouseArea()) && req.getWarehouseId().equals(data.getWarehouseId()) && "area".equals(req.getType()))
                        .findFirst().orElse(new WarehouseLocationEntity());
                data.setWarehouseAreaName(warehouseLocationEntity.getName());
            }

            // 销售状态名称
            data.setSaleStateName(SaleStateEnum.getNameByCode(data.getSaleState()));
        });
    }

    /**
     * @param skuNoList
     * @param warehouseCodeList
     * @param orgCodeList
     * @return List<Map < Object>>
     * @description: 获取金蝶库存数据
     * @author Will
     * @date: 2023/10/17 18:24
     */
    private List<Map<String, Object>> listKingdeeInventory (List<String> skuNoList,List<String> warehouseCodeList,List<String> orgCodeList) {
        DmpSyncKingdeeDTO.ParamDTO paramDTO = new DmpSyncKingdeeDTO.ParamDTO();
        paramDTO.setFormId("STK_Inventory");
        paramDTO.setFieldKeys("FMaterialId.FNumber,FStockId.FNumber,FStockOrgId.FNumber,FBASEQTY,FMaterialid.FSTOREURNOM,FMaterialid.FSTOREURNUM");
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(CharSequenceUtil.format(" FMaterialId.FNumber in ({})", skuNoList.stream().map(obj -> "'" + obj + "'").collect(Collectors.joining(","))));
        queryFilters.add(CharSequenceUtil.format(" FStockId.FNumber in ({})", warehouseCodeList.stream().map(obj -> "'" + obj + "'").collect(Collectors.joining(","))));
        queryFilters.add(CharSequenceUtil.format(" FStockOrgId.FNumber in ({})", orgCodeList.stream().map(obj -> "'" + obj + "'").collect(Collectors.joining(","))));
        String filterStr = String.join(" and ", queryFilters);
        paramDTO.setFilterString(filterStr);

        boolean dataSign = true;
        //当前页数
        Integer pageIndex = 1;
        //每次最多获取100条
        Integer pageSize = 10000;
        List<Map<String, Object>> resultAll = new ArrayList<>();
        while (dataSign) {
            paramDTO.setLimit(pageSize);
            paramDTO.setStartRow(pageIndex);
            paramDTO.setTopRowCount(MathUtil.ZERO);
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
            List<Map<String, Object>> result = dmpSyncFeign.listKingdeeData(paramDTO);
            log.info("获取金蝶直接调拨订单数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            if (CollUtil.isEmpty(result)) {
                break;
            }
            resultAll.addAll(result);
            pageIndex++;
        }
        return resultAll;
    }


    private List<LinkedHashMap> fillInventoryAgePageData(List<LinkedHashMap> dataList, List<InventoryReportDTO.InventoryAgeRangeDTO> userRangeList) {
        List<LinkedHashMap> resultList = Lists.newArrayList();
        LinkedHashMap<String, Object> resultMap = Maps.newLinkedHashMap();
        // 标题
        LinkedHashMap headMap = Maps.newLinkedHashMap();
        // 结果集
        List<LinkedHashMap> convertDataList = Lists.newArrayListWithExpectedSize(dataList.size());

        // 公共标题字段
        Arrays.asList(InventoryAgeTitleEnum.values()).forEach(inventoryAgeTitleEnum -> {
            headMap.put(inventoryAgeTitleEnum.getCode(), inventoryAgeTitleEnum.getName());
        });

        // 动态字段标题
        if (CollUtil.isNotEmpty(userRangeList)) {
            userRangeList.forEach(userRange -> headMap.put(userRange.getName(), userRange.getName()));
        }

        List<String> skuIds = Lists.newArrayList();
        dataList.forEach(data -> {
            skuIds.add(StrUtils.null2EmptyWithTrim(data.get("sku_id")));
        });
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();

        // 结果集字段转驼峰
        if (CollUtil.isNotEmpty(dataList)) {
            dataList.forEach(record -> {
                LinkedHashMap<String, Object> convertMap = new LinkedHashMap<>();
                convertMap.put("productName", null);
                convertMap.put("productImgUrl", null);
                convertMap.put("saleStateName", null);
                convertMap.put("warehouseName", null);
                convertMap.put("orgName", null);
                record.forEach((fieldKey, fieldVal) -> {
                    String camelKey = StrUtil.toCamelCase(StrUtils.null2EmptyWithTrim(fieldKey));
                    if (Objects.equals(fieldKey, FieldConstant.SKU_ID) && skuMap.containsKey(fieldVal)) {
                        SkuVO skuVO = skuMap.get(fieldVal).get(0);
                        convertMap.put("productName", skuVO.getSkuName());
                        convertMap.put("productImgUrl", skuVO.getSkuImagesUrl());
                    }
                    // 销售状态
                    if (Objects.equals(fieldKey, FieldConstant.SALE_STATE) && Objects.nonNull(fieldVal) && StrUtils.isInteger(fieldVal)) {
                        convertMap.put("saleStateName", SaleStateEnum.getNameByCode(Integer.parseInt(StrUtils.null2EmptyWithTrim(fieldVal))));
                    }
                    // 仓库名称
                    if (Objects.equals(fieldKey, FieldConstant.WAREHOUSE_ID) && Objects.nonNull(fieldVal)) {
                        WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(StrUtils.null2EmptyWithTrim(fieldVal), warehouseId -> warehouseService.detailWithCache(warehouseId));
                        if (Objects.nonNull(warehouseDetail) && CharSequenceUtil.isNotEmpty(warehouseDetail.getId())) {
                            convertMap.put("warehouseName", warehouseDetail.getName());
                        }
                    }
                    // 组织名称
                    if (Objects.equals(fieldKey, FieldConstant.ORG_ID) && Objects.nonNull(fieldVal)) {
                        SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(StrUtils.null2EmptyWithTrim(fieldVal), orgId -> sysUserFeign.getCompanyById(orgId));
                        if (Objects.nonNull(sysAccountingCompanyEntity)) {
                            convertMap.put("orgName", sysAccountingCompanyEntity.getCompanyName());
                        }
                    }
                    convertMap.put(camelKey, fieldVal);
                });
                convertDataList.add(convertMap);
            });
        }
        resultMap.put("head", headMap);
        resultMap.put("data", convertDataList);
        resultList.add(resultMap);
        return resultList;
    }

    @Override
    public InventoryDTO.PdaHomeInventoryBalanceDTO getInventoryByWarehouseId(String warehouseId) {
        InventoryDTO.PdaHomeInventoryBalanceDTO pdaHomeInventoryBalanceDTO = new InventoryDTO.PdaHomeInventoryBalanceDTO();
        List<InventoryDTO.PdaHomeInventoryBalanceDTO> inventory = baseMapper.getInventoryByWarehouseId(warehouseId, InventoryStatusEnum.USABLE.getCode());
        if (CharSequenceUtil.isBlank(warehouseId)) {
            long usableQty = 0L;
            long todayDeliveryQty = 0L;
            long todayStockInQty = 0L;

            for (InventoryDTO.PdaHomeInventoryBalanceDTO homeInventoryBalanceDTO : inventory) {
                if (homeInventoryBalanceDTO.getUsableQty() != null) {
                    usableQty = usableQty + homeInventoryBalanceDTO.getUsableQty();
                }
                if (homeInventoryBalanceDTO.getTodayDeliveryQty() != null) {
                    todayDeliveryQty = todayDeliveryQty + homeInventoryBalanceDTO.getTodayDeliveryQty();
                }
                if (homeInventoryBalanceDTO.getTodayStockInQty() != null) {
                    todayStockInQty = todayStockInQty + homeInventoryBalanceDTO.getTodayStockInQty();
                }
            }
            pdaHomeInventoryBalanceDTO.setUsableQty(usableQty);
            pdaHomeInventoryBalanceDTO.setTodayDeliveryQty(todayDeliveryQty);
            pdaHomeInventoryBalanceDTO.setTodayStockInQty(todayStockInQty);
        } else {
            if (CollectionUtils.isNotEmpty(inventory)) {
                //获取仓库信息
                List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Collections.singletonList(warehouseId));
                if (CollectionUtils.isNotEmpty(warehouseList)) {
                    pdaHomeInventoryBalanceDTO.setWarehouseName(warehouseList.get(MathUtil.ZERO).getName());
                }
                pdaHomeInventoryBalanceDTO.setUsableQty(inventory.get(MathUtil.ZERO).getUsableQty());
                pdaHomeInventoryBalanceDTO.setTodayDeliveryQty(inventory.get(MathUtil.ZERO).getTodayDeliveryQty());
                pdaHomeInventoryBalanceDTO.setTodayStockInQty(inventory.get(MathUtil.ZERO).getTodayStockInQty());
            }
        }
        return pdaHomeInventoryBalanceDTO;
    }

    @Override
    public List<InventoryDTO.PdaInventoryDTO> getInventoryByParam(InventoryDTO.PdaSearchParamDTO dto) {
        if (Objects.isNull(dto.getWarehouseId())) {
            return new ArrayList<>();
        }
        String warehouseId = dto.getWarehouseId();
        List<String> warehouseLocations = dto.getWarehouseLocations();
        List<String> skuIds = dto.getSkuIds();
        if (CollectionUtils.isEmpty(warehouseLocations) || CollectionUtils.isEmpty(skuIds)) {
            return baseMapper.getInventoryByParam(dto);
        }

        List<InventoryDTO.PdaInventoryDTO> inventoryDTOList = new ArrayList<>();
        for (int i = 0; i < skuIds.size(); i++) {
            InventoryDTO.PdaSearchParamDTO pdaSearchParamDTO = new InventoryDTO.PdaSearchParamDTO();
            pdaSearchParamDTO.setSkuIds(Collections.singletonList(skuIds.get(i)));
            pdaSearchParamDTO.setWarehouseId(warehouseId);
            pdaSearchParamDTO.setWarehouseLocations(Collections.singletonList(warehouseLocations.get(i)));
            List<InventoryDTO.PdaInventoryDTO> inventoryByParam = baseMapper.getInventoryByParam(pdaSearchParamDTO);
            if (CollectionUtils.isNotEmpty(inventoryByParam)) {
                inventoryDTOList.add(inventoryByParam.get(0));
            }
        }
        return inventoryDTOList;
    }

    @Override
    public List<InventoryDTO.InventoryViewQtyDTO> getInventoryQty(List<InventoryDTO.InventoryBySkuIdAndWarehouseDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        List<InventoryDTO.InventoryViewQtyDTO> list = new ArrayList<>();
        dtos.forEach(dto -> {
            InventoryDTO.InventoryViewQtyDTO inventoryQtyDTO = new InventoryDTO.InventoryViewQtyDTO();
            if (CharSequenceUtil.isNotBlank(dto.getWarehouseId()) && CharSequenceUtil.isNotBlank(dto.getSkuId())) {
                inventoryQtyDTO = baseMapper.getInventoryInfoByParam(dto);
                if (Objects.isNull(inventoryQtyDTO)) {
                    inventoryQtyDTO = new InventoryDTO.InventoryViewQtyDTO();
                }
            }
            setExtData(dto, inventoryQtyDTO);
            list.add(inventoryQtyDTO);
        });
        return list;
    }

    private static void setExtData(InventoryDTO.InventoryBySkuIdAndWarehouseDTO dto, InventoryDTO.InventoryViewQtyDTO inventoryQtyDTO) {
        inventoryQtyDTO.setSkuId(dto.getSkuId());
        inventoryQtyDTO.setWarehouseId(dto.getWarehouseId());
        inventoryQtyDTO.setWarehouseLocation(dto.getWarehouseLocation());
    }

    @Override
    public InventoryDTO.PdaInventorySearch getInventoryBySkuNo(PagingDTO<InventoryDTO.PdaSearchParamDTO> searchDTO) {
        InventoryDTO.PdaSearchParamDTO params = searchDTO.getParams();
        if(CharSequenceUtil.isBlank(params.getSkuNo())){
            return new InventoryDTO.PdaInventorySearch();
        }

        //sku信息
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Collections.singletonList(params.getSkuNo()));
        if (ObjectUtil.isEmpty(skuVOList)) {
            return new InventoryDTO.PdaInventorySearch();
        }
        InventoryDTO.PdaInventorySearch pdaInventorySearch = new InventoryDTO.PdaInventorySearch();
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(params.getSkuNo())).findFirst().orElse(new SkuVO());
        pdaInventorySearch.setSkuNo(skuVO.getSkuNo());
        pdaInventorySearch.setSkuName(skuVO.getSkuName());
        pdaInventorySearch.setSpuNo(skuVO.getSpuNo());
        pdaInventorySearch.setSpuName(skuVO.getSpuName());
        if (CharSequenceUtil.isBlank(skuVO.getSpuNo())) {
            pdaInventorySearch.setSpuNo("");
            pdaInventorySearch.setSpuName("");
        }
        pdaInventorySearch.setVariantProperty(skuVO.getVariantProperty());
        pdaInventorySearch.setImagesUrl(skuVO.getSkuImagesUrl());

        //库存信息
        InventoryDTO.InventoryBySkuNoDTO paramDTO = new InventoryDTO.InventoryBySkuNoDTO();
        paramDTO.setSkuNo(params.getSkuNo());
        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        //查询配置过滤对应组织仓库
        if(params.isFilterOrgFlag()){
            List<DictBasicDTO.ListDTO> listDTOList = dictBasicService.getByKey(WmsConstant.WAREHOUSE_BY_FILTER_ORG);
            if(CollectionUtils.isNotEmpty(listDTOList)){
                DictBasicDTO.ListDTO orgDTOList = listDTOList.get(0);
                String orgArr = orgDTOList.getValue();
                paramDTO.setOrgIds(Arrays.asList(orgArr.split(",")));
            }
        }
        paramDTO.setFilterSelfAddFlag(params.isFilterSelfAddFlag());
        paramDTO.setZeroInventory(params.isZeroInventory());
        String permissionSql = authDataFeign.getWarehousePermissionSql("it.warehouse_id");
        paramDTO.setPermissionSql(permissionSql);
        IPage<InventoryDTO.PdaInventoryWarehouseDTO> warehouseDTOPage = baseMapper.pageInventoryWarehouseByParam(query,paramDTO);
        List<InventoryDTO.PdaInventoryWarehouseLocationDTO> warehouseLocationDTOList = baseMapper.listInventoryWarehouseLocationByParam(paramDTO);
        List<InventoryDTO.PdaInventoryWarehouseDTO> warehouseDTOList = warehouseDTOPage.getRecords();
        List<String> warehouseIds = warehouseDTOList.stream().map(InventoryDTO.PdaInventoryWarehouseDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);
        for (InventoryDTO.PdaInventoryWarehouseDTO warehouseDTO : warehouseDTOList) {
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(req -> req.getId().equals(warehouseDTO.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            warehouseDTO.setWarehouseName(updateDTO.getName());
            warehouseDTO.setIndex(updateDTO.getIndex());
            List<InventoryDTO.PdaInventoryWarehouseLocationDTO> locationDTOList = warehouseLocationDTOList.stream().filter(req -> req.getWarehouseId().equals(warehouseDTO.getWarehouseId()) && req.getRealQty() > 0).collect(Collectors.toList());
            for (InventoryDTO.PdaInventoryWarehouseLocationDTO locationDTO : locationDTOList) {
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(locationDTO.getWarehouseId()) && req.getCode().equals(locationDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                locationDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
            }
            warehouseDTO.setWarehouseLocationDTOList(locationDTOList);
        }
        //根据index排序
        warehouseDTOList.sort(Comparator.nullsLast(Comparator.comparing(InventoryDTO.PdaInventoryWarehouseDTO::getIndex)));
        pdaInventorySearch.setWarehouseDTOList(new PagingVO<>(warehouseDTOPage));
        //汇总仓位实际库存/可用库存/冻结库存数量
        List<InventoryDTO.PdaInventoryWarehouseLocationDTO> warehouseDTOList1 = baseMapper.listInventoryWarehouseByParam(paramDTO);
        pdaInventorySearch.setRealTotalQty(warehouseDTOList1.stream().map(InventoryDTO.PdaInventoryWarehouseLocationDTO::getRealQty)
                .filter(Objects::nonNull).reduce(0, Integer::sum));
        pdaInventorySearch.setFrozenTotalQty(warehouseDTOList1.stream().map(InventoryDTO.PdaInventoryWarehouseLocationDTO::getFrozenQty)
                .filter(Objects::nonNull).reduce(0, Integer::sum));
        pdaInventorySearch.setUsableTotalQty(warehouseDTOList1.stream().map(InventoryDTO.PdaInventoryWarehouseLocationDTO::getUsableQty)
                .filter(Objects::nonNull).reduce(0, Integer::sum));
        return pdaInventorySearch;
    }

    @Override
    public List<InventoryEntity> listInventoryByParam(InventoryDTO.ParamDTO dto) {
        return baseMapper.listByParam(dto);
    }

    /**
     * 根据参数获取对应数据
     *
     * @param dto   参数
     * @return     库存数据
     */
    @Override
    public InventoryDTO.InventoryQtyDTO getInventoryQty(InventoryDTO.InventoryBySkuNoDTO dto) {
        InventoryDTO.InventoryQtyDTO result = new InventoryDTO.InventoryQtyDTO();
        List<InventoryEntity> list = this.lambdaQuery()
                .eq(CharSequenceUtil.isNotBlank(dto.getWarehouseId()),InventoryEntity::getSkuId, dto.getSkuId())
                .eq(CharSequenceUtil.isNotBlank(dto.getOrgId()),InventoryEntity::getOrgId, dto.getOrgId())
                .eq(CharSequenceUtil.isNotBlank(dto.getWarehouseId()),InventoryEntity::getWarehouseId, dto.getWarehouseId())
                .eq(InventoryEntity::getWarehouseLocation, dto.getWarehouseLocation())
                .list();
        result.setOrgId(dto.getOrgId());
        result.setSkuId(dto.getSkuId());
        result.setWarehouseId(dto.getWarehouseId());
        String frozen = InventoryStatusEnum.FROZEN.getCode();
        String usable = InventoryStatusEnum.USABLE.getCode();
        Integer frozenQty = list.stream().filter(l -> frozen.equals(l.getDictInventoryStatus())).
                mapToInt(InventoryEntity::getQty).sum();
        result.setFrozenQty(frozenQty);
        Integer usableQty = list.stream().filter(l -> usable.equals(l.getDictInventoryStatus())).
                mapToInt(InventoryEntity::getQty).sum();
        result.setUsableQty(usableQty);
        return result;
    }

    @Override
    public List<InventoryDTO.UsableInventoryViewDTO> listByParam(List<InventoryDTO.UsableInventoryParamDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<String> warehouseIds = list.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        // 查询仓库下面仓位的SKU可用库存
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);

        List<InventoryDTO.UsableInventoryViewDTO> viewList = new ArrayList<>();
        for (InventoryDTO.UsableInventoryParamDTO usableInventoryParamDTO : list) {
            InventoryDTO.UsableInventoryViewDTO view = new InventoryDTO.UsableInventoryViewDTO();
            // 查询仓库下面的SKU的可用库存
            if (Objects.isNull(usableInventoryParamDTO.getWarehouseLocation())) {
                view.setSkuId(usableInventoryParamDTO.getSkuId());
                view.setWarehouseId(usableInventoryParamDTO.getWarehouseId());
                view.setWarehouseLocation(usableInventoryParamDTO.getWarehouseLocation());
                view.setUsableQty(this.getUsableInventoryTotal(usableInventoryParamDTO.getWarehouseId(), usableInventoryParamDTO.getSkuId()));
                view.setFrozenQty(this.getFrozenInventoryTotal(usableInventoryParamDTO.getWarehouseId(), usableInventoryParamDTO.getSkuId()));
                viewList.add(view);
            } else {
                WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(usableInventoryParamDTO.getWarehouseId())).findFirst().orElse(null);
                if (Objects.isNull(warehouseEntity)){
                    throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
                }
                Integer inventoryUsableTotal = this.getInventoryTotal(warehouseEntity.getOrgId(), warehouseEntity.getId(), usableInventoryParamDTO.getSkuId(), usableInventoryParamDTO.getWarehouseLocation(), InventoryStatusEnum.USABLE.getCode());
                view.setSkuId(usableInventoryParamDTO.getSkuId());
                view.setWarehouseId(usableInventoryParamDTO.getWarehouseId());
                view.setWarehouseLocation(usableInventoryParamDTO.getWarehouseLocation());
                view.setUsableQty(inventoryUsableTotal);
                Integer inventoryFrozenTotal = this.getInventoryTotal(warehouseEntity.getOrgId(), warehouseEntity.getId(), usableInventoryParamDTO.getSkuId(), usableInventoryParamDTO.getWarehouseLocation(), InventoryStatusEnum.FROZEN.getCode());
                view.setFrozenQty(inventoryFrozenTotal);
                viewList.add(view);
            }
        }
        return viewList;
    }

    @Override
    public List<InventoryDTO.LocationInventoryResult> listLocationInventoryBySkus(List<InventoryDTO.LocationInventoryParam> param) {
        List<String> warehouseIds = param.stream().map(InventoryDTO.LocationInventoryParam::getWarehouseId).collect(Collectors.toList());
        List<WarehouseLocationEntity> locations = warehouseLocationService.listByWarehouseIds(warehouseIds);
        List<InventoryDTO.LocationInventoryResult> results = new ArrayList<>();
        for (InventoryDTO.LocationInventoryParam inventoryParam : param) {
            List<InventoryEntity> inventoryEntities = list(Wrappers.<InventoryEntity>lambdaQuery()
                    .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                    .eq(InventoryEntity::getWarehouseId, inventoryParam.getWarehouseId())
                    .eq(InventoryEntity::getSkuNo, inventoryParam.getSkuNo())
                    .gt(InventoryEntity::getQty, 0)
                    .orderByDesc(InventoryEntity::getQty));
            InventoryDTO.LocationInventoryResult result = new InventoryDTO.LocationInventoryResult();
            result.setSkuNo(inventoryParam.getSkuNo());
            result.setWarehouseId(inventoryParam.getWarehouseId());
            List<InventoryDTO.LocationInventory> inventories = inventoryEntities.stream().map(e -> {
                WarehouseLocationEntity location = locations.stream().filter(l -> l.getWarehouseId().equals(e.getWarehouseId()))
                        .filter(l -> l.getCode().equals(e.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                InventoryDTO.LocationInventory inventory = new InventoryDTO.LocationInventory();
                inventory.setWarehouseLocation(e.getWarehouseLocation());
                inventory.setWarehouseLocationName(location.getName());
                inventory.setUsableQty(e.getQty());
                return inventory;
            }).collect(Collectors.toList());
            result.setLocationInventory(inventories);
            results.add(result);
        }
        return results;
    }

    @Override
    public InventoryDTO.LocationInventory recommendedLocation(InventoryDTO.RecommendedLocationParam param) {
        InventoryEntity inventory = getRecommendedInventory(param);
        if (ObjectUtil.isEmpty(inventory)){
            throw new ServiceException(ApiError.ERROR_99100);
        }
        WarehouseLocationEntity entity = warehouseLocationService.getOne(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                .eq(WarehouseLocationEntity::getWarehouseId, param.getWarehouseId())
                .eq(WarehouseLocationEntity::getCode, inventory.getWarehouseLocation())
                .last("LIMIT 1")
        );
        InventoryDTO.LocationInventory locationInventory = new InventoryDTO.LocationInventory();
        locationInventory.setWarehouseLocation(inventory.getWarehouseLocation());
        locationInventory.setUsableQty(inventory.getQty());
        locationInventory.setWarehouseLocationName(entity.getName());
        return locationInventory;
    }


    /**
     * 获取最接近传入数量的仓位
     * @param param param
     */
    private InventoryEntity getRecommendedInventory(InventoryDTO.RecommendedLocationParam param) {
        // 首先获取库存相等的库位
        InventoryEntity inventory = getOne(Wrappers.<InventoryEntity>lambdaQuery()
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                .eq(InventoryEntity::getWarehouseId, param.getWarehouseId())
                .eq(InventoryEntity::getSkuNo, param.getSkuNo())
                .eq(InventoryEntity::getQty, param.getUsableQty())
                .last("LIMIT 1")
        );
        if (ObjectUtil.isEmpty(inventory)) {
            // 不存在库存相等的则升序获取大于对应数量的最小库存
            inventory = getOne(Wrappers.<InventoryEntity>lambdaQuery()
                    .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                    .eq(InventoryEntity::getWarehouseId, param.getWarehouseId())
                    .eq(InventoryEntity::getSkuNo, param.getSkuNo())
                    .gt(InventoryEntity::getQty, param.getUsableQty())
                    .orderByAsc(InventoryEntity::getQty)
                    .last("LIMIT 1")
            );
        }
        if (ObjectUtil.isEmpty(inventory)) {
            // 不存在库存大于的则升序获取小于对应数量的最大库存
            inventory = getOne(Wrappers.<InventoryEntity>lambdaQuery()
                    .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                    .eq(InventoryEntity::getWarehouseId, param.getWarehouseId())
                    .eq(InventoryEntity::getSkuNo, param.getSkuNo())
                    .lt(InventoryEntity::getQty, param.getUsableQty())
                    .orderByDesc(InventoryEntity::getQty)
                    .last("LIMIT 1")
            );
        }
        return inventory;
    }

    @Override
    public long countByWarehouse(InventoryDTO.SearchParamDTO searchParamDTO) {
        return inventoryMapper.countByWarehouse(searchParamDTO);
    }

    @Override
    public long countByArea(InventoryDTO.SearchParamDTO searchParamDTO) {
        return inventoryMapper.countByArea(searchParamDTO);
    }

    @Override
    public long countByLocation(InventoryDTO.SearchParamDTO searchParamDTO) {
        return inventoryMapper.countByLocation(searchParamDTO);
    }
    /**
     * PDA:库存查询（仓库）
     * @param searchDTO
     * @return
     */
    @Override
    public InventoryDTO.PdaInventoryWarehousePageDTO<InventoryDTO.PdaInventoryPageDTO> getInventoryByWarehouse(PagingDTO<InventoryDTO.PdaSearchParamDTO> searchDTO) {
        InventoryDTO.PdaSearchParamDTO params = searchDTO.getParams();
        //库存信息
        InventoryDTO.InventoryBySkuNoDTO paramDTO = new InventoryDTO.InventoryBySkuNoDTO();
        if(CharSequenceUtil.isBlank(params.getWarehouseLocation())){
            throw new ServiceException("仓位不存在");
        }
        WarehouseLocationEntity entity = warehouseLocationService.findByWarehouseCode(params.getWarehouseLocation());
        if (Objects.nonNull(entity) && CharSequenceUtil.isNotBlank(entity.getCode())){
            paramDTO.setWarehouseLocation(entity.getCode());
        }else {
            throw new ServiceException("仓位不存在");
        }

        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        //查询配置过滤对应组织仓库
        if(params.isFilterOrgFlag()){
            List<DictBasicDTO.ListDTO> listDTOList = dictBasicService.getByKey(WmsConstant.WAREHOUSE_BY_FILTER_ORG);
            if(CollectionUtils.isNotEmpty(listDTOList)){
                DictBasicDTO.ListDTO orgDTOList = listDTOList.get(0);
                String orgArr = orgDTOList.getValue();
                paramDTO.setOrgIds(Arrays.asList(orgArr.split(",")));
            }
        }
        paramDTO.setFilterSelfAddFlag(params.isFilterSelfAddFlag());
        paramDTO.setZeroInventory(params.isZeroInventory());
        IPage<InventoryDTO.PdaInventoryPageDTO> page = baseMapper.pageInventoryWarehouseBySkuId(query,paramDTO);
        //填充基础信息
        buildWarehouseInfo(page.getRecords(), paramDTO);
        InventoryDTO.PdaInventoryWarehousePageDTO<InventoryDTO.PdaInventoryPageDTO> result = new InventoryDTO.PdaInventoryWarehousePageDTO<>(page);
        //仓位信息回填
        result.setWarehouseLocation(entity.getCode());
        result.setWarehouseLocationName(entity.getName());
        //汇总仓位实际库存/可用库存/冻结库存数量
        result.setRealTotalQty(page.getRecords().stream().map(InventoryDTO.PdaInventoryPageDTO::getRealQty)
                .filter(Objects::nonNull).reduce(0, Integer::sum));
        result.setProductQty((int) page.getTotal());
        return result;
    }

    /**
     * 填充基础信息
     *
     * @param records
     * @param paramDTO
     */
    private void buildWarehouseInfo(List<InventoryDTO.PdaInventoryPageDTO> records, InventoryDTO.InventoryBySkuNoDTO paramDTO) {
        String warehouseLocation = paramDTO.getWarehouseLocation();
        if (CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> skuIds = records.stream().map(InventoryDTO.PdaInventoryPageDTO::getSkuId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)){
            return;
        }
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
//        List<String> warehouseIds = records.stream().map(InventoryDTO.PdaInventoryWarehouseDTO::getWarehouseId).distinct().collect(Collectors.toList());
        paramDTO.setSkuId(null);
        paramDTO.setSkuIds(skuIds);
        List<InventoryDTO.PdaInventoryWarehouseLocationDTO> warehouseDTOList = baseMapper.listInventoryWarehouseBySkuId(paramDTO);
        if (CollectionUtils.isEmpty(warehouseDTOList)){
            return;
        }
        List<String> warehouseIds = warehouseDTOList.stream().map(InventoryDTO.PdaInventoryWarehouseLocationDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = warehouseIds.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj, warehouseLocation)).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        //仓库信息整理
        warehouseDTOList.forEach(pdaInventoryWarehouseDTO -> {
            WarehouseEntity warehouseEntity = warehouseList.stream().filter(e -> CharSequenceUtil.isNotBlank(pdaInventoryWarehouseDTO.getWarehouseId()) && pdaInventoryWarehouseDTO.getWarehouseId().equals(e.getId()))
                    .findFirst().orElse(new WarehouseEntity());
            pdaInventoryWarehouseDTO.setOrgId(warehouseEntity.getOrgId());
            pdaInventoryWarehouseDTO.setWarehouseName(warehouseEntity.getName());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(pdaInventoryWarehouseDTO.getWarehouseId())
                            && pdaInventoryWarehouseDTO.getWarehouseId().equals(e.getWarehouseId()) && CharSequenceUtil.isNotBlank(warehouseLocation) && warehouseLocation.equals(e.getCode()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            pdaInventoryWarehouseDTO.setWarehouseLocation(warehouseLocationEntity.getCode());
            pdaInventoryWarehouseDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
        });
        //结果汇总
        records.forEach(pdaInventoryWarehouseDTO -> {
            String skuId = pdaInventoryWarehouseDTO.getSkuId();
            SkuVO skuVO = skuVOList.stream().filter(e -> CharSequenceUtil.isNotBlank(skuId) && skuId.equals(e.getSkuId()))
                    .findFirst().orElse(new SkuVO());
            pdaInventoryWarehouseDTO.setSkuName(skuVO.getSkuName());
            pdaInventoryWarehouseDTO.setSkuNo(skuVO.getSkuNo());
            pdaInventoryWarehouseDTO.setSkuImagesUrl(skuVO.getSkuImagesUrl());
            List<InventoryDTO.PdaInventoryWarehouseLocationDTO> warehouseDTOS = warehouseDTOList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getWarehouseLocation())
                    && CharSequenceUtil.isNotBlank(e.getWarehouseLocationName()) && CharSequenceUtil.isNotBlank(e.getSkuId()) && e.getSkuId().equals(skuId)).collect(Collectors.toList());
            pdaInventoryWarehouseDTO.setWarehouseLocationDTOList(warehouseDTOS);
        });
    }

    @Override
    public Integer getFrozenInventoryTotal(String warehouseId, String skuId) {
        // 查询仓库组织
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouseEntity)){
            throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
        }
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId, warehouseId).eq(InventoryEntity::getOrgId, warehouseEntity.getOrgId())
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.FROZEN.getCode());

        List<InventoryEntity> list = baseMapper.selectList(queryWrapper);
        return CollUtil.isEmpty(list) ? 0 : list.stream().mapToInt(InventoryEntity::getQty).sum();
    }

    /**
     * 根据skuId和仓库id获取可用数量
     * @param paramDTO
     * @return
     */
    @Override
    public List<InventoryDTO.InventoryViewQtyDTO> getUsableQtyBySkuIdsAndWarehouseIds(InventoryDTO.ParamDTO paramDTO) {
        if (Objects.isNull(paramDTO)) {
            return new ArrayList<>();
        }
        List<InventoryDTO.InventoryViewQtyDTO> list=baseMapper.getUsableQtyBySkuIdsAndWarehouseIds(paramDTO);
        return list;
    }

    @Override
    public PagingVO<InventoryDTO.PagingViewDTO> getInventoryPageData(PagingDTO<InventoryDTO.ExportSearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        // 如果是否选导出处理
        dealExportParams(dto.getParams());
        AdvanceQueryDTO advanceQueryDTO = dto.getParams().getAdvanceQueryDTOList().stream().filter(e -> "dimension".equalsIgnoreCase(e.getField())).findFirst().orElse(null);
        if (null == advanceQueryDTO){
            ServiceException.runError("advanceQueryDTOList.field=dimension不能为空");
        }
        dto.getParams().setDimension(advanceQueryDTO.getValue().toString());
        InventoryDTO.SearchParamDTO searchParamDTO = BeanMapperUtils.map(InventoryDTO.SearchParamDTO.class, dto.getParams());
        Page<InventoryDTO.PagingViewDTO> dataList = new Page<>(dto.getPage(), dto.getPageSize());
        if (dto.getParams().getDimension().equals(InventorySearchDimensionEnum.WAREHOUSE.getCode())) {
            dataList = inventoryMapper.exportByWarehouse(dataList ,searchParamDTO, dto.getLastId());
        }
        if (dto.getParams().getDimension().equals(InventorySearchDimensionEnum.WAREHOUSE_AREA.getCode())) {
            dataList = inventoryMapper.exportByArea( dataList, searchParamDTO, dto.getParams().getWarehouseAreaCodeList(), dto.getLastId());
        }
        if (dto.getParams().getDimension().equals(InventorySearchDimensionEnum.WAREHOUSE_LOCATION.getCode())) {
            if(CharSequenceUtil.isNotBlank(dto.getParams().getWarehouseLocationName())){
                List<WarehouseLocationEntity> list = warehouseLocationService.listByLocationName(dto.getParams().getWarehouseLocationName());
                if(! list.isEmpty()){
                    List<String> codeList = list.stream().map(WarehouseLocationEntity::getCode).distinct().collect(Collectors.toList());
                    dataList = this.baseMapper.exportByLocation(dataList,searchParamDTO, codeList, dto.getLastId());
                }
            }else {
                dataList = this.baseMapper.exportByLocation(dataList,searchParamDTO, dto.getParams().getWarehouseLocationCodeList(), dto.getLastId());
            }
        }
        if (CollUtil.isEmpty(dataList.getRecords())) {
            return new PagingVO<>(dataList);
        }
        fillInventoryPageData(dataList.getRecords());
        return new PagingVO<>(dataList);
    }

    @Override
    public List<InventoryEntity> listInventoryBySkuIds(InventoryQtyDTO.InventoryBySkuDTO dto) {
        if (Objects.isNull(dto) || CollectionUtils.isEmpty(dto.getSkuIdList())){
            return Collections.emptyList();
        }
        return this.lambdaQuery().select(InventoryEntity::getId,InventoryEntity::getSkuId,InventoryEntity::getSkuNo, InventoryEntity::getQty,
                        InventoryEntity::getWarehouseId,InventoryEntity::getDictInventoryStatus, InventoryEntity::getWarehouseLocation)
                .in(InventoryEntity::getSkuId, dto.getSkuIdList()).list();
    }

    @Override
    public Integer getQtyByLocation(String warehouseId, String warehouseLocation) {
        return inventoryMapper.getQtyByLocation(warehouseId, warehouseLocation == null ? "" : warehouseLocation);
    }
    @Override
//    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
//    @GlobalTransactional(rollbackFor = Exception.class, propagation = io.seata.tm.api.transaction.Propagation.REQUIRES_NEW)
    public InventoryEntity getInventory(InventoryTransactionDTO transactionDTO) {
        LambdaQueryWrapper<InventoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryEntity::getSkuId, transactionDTO.getSkuId())
                .eq(InventoryEntity::getWarehouseId, transactionDTO.getWarehouseId())
                .eq(InventoryEntity::getWarehouseLocation, transactionDTO.getWarehouseLocation())
                .eq(InventoryEntity::getDictInventoryStatus, transactionDTO.getInventoryStatus())
                .last("limit 1");
        InventoryEntity inventoryEntity = this.getOne(wrapper);
        // 如果不存在库存数据则初始化一个
        if (Objects.isNull(inventoryEntity)) {
            inventoryEntity=new InventoryEntity();
            inventoryEntity.setSkuId(transactionDTO.getSkuId());
            inventoryEntity.setSkuNo(transactionDTO.getSkuNo());
            inventoryEntity.setOrgId(transactionDTO.getOrgId());
            inventoryEntity.setWarehouseId(transactionDTO.getWarehouseId());
            inventoryEntity.setWarehouseLocation(transactionDTO.getWarehouseLocation());
            inventoryEntity.setDictInventoryStatus(transactionDTO.getInventoryStatus());
            inventoryEntity.setQty(transactionDTO.getQty());
            inventoryEntity.setCreateTime(LocalDateTime.now());
            inventoryEntity.setCreateUserId(transactionDTO.getUserId());
            inventoryEntity.setCreateUserName(transactionDTO.getUserName());
            inventoryEntity.setUpdateTime(LocalDateTime.now());
            inventoryEntity.setUpdateUserId(transactionDTO.getUserId());
            inventoryEntity.setUpdateUserName(transactionDTO.getUserName());
            boolean save = save(inventoryEntity);
            log.warn("库存数据不存在，初始化库存数据：{}  save：{}", inventoryEntity, save);
            if (!save) {
                throw new ServiceException("库存数据保存失败");
            }
        }
        return inventoryEntity;
    }

    @Override
    public PagingVO<DynamicExcelDTO> exportWmsInventoryAge(PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        // 勾选导出处理
        if (CollUtil.isNotEmpty(dto.getParams().getItems())) {
            List<InventoryReportDTO.ExportInventoryAgeItem> checkData = dto.getParams().getItems();
            List<String> warehouseIds = checkData.stream().map(InventoryReportDTO.ExportInventoryAgeItem::getWarehouseId).distinct().collect(Collectors.toList());
            List<String> skuIds = checkData.stream().map(InventoryReportDTO.ExportInventoryAgeItem::getSkuId).distinct().collect(Collectors.toList());
            List<String> warehouseLocation = checkData.stream().map(InventoryReportDTO.ExportInventoryAgeItem::getWarehouseLocation).distinct().collect(Collectors.toList());
            dto.getParams().setWarehouseIdList(warehouseIds);
            dto.getParams().setSkuIdList(skuIds);
            dto.getParams().setWarehouseLocationList(warehouseLocation);
        }

        // 获取用户区间配置
        List<CfgUserRangeDTO.UserRangeDataDTO> userRanges = sysUserFeign.getUserRangeByType(UserRangeTypeEnum.INVENTORY_AGE.getCode(), Boolean.TRUE);
        List<InventoryReportDTO.InventoryAgeRangeDTO> userRangeList = BeanMapperUtils.copyList(InventoryReportDTO.InventoryAgeRangeDTO.class, userRanges);
        dto.getParams().setUserRangeList(userRangeList);

        Page<LinkedHashMap> page = inventoryMapper.exportInventoryPage(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());

        // 标题及值赋值
        List<LinkedHashMap> resultList = fillInventoryAgePageData(page.getRecords(), userRangeList);
        LinkedHashMap headMap = (LinkedHashMap) resultList.get(0).get("head");
        List<LinkedHashMap<String ,Object>> convertDataList = (List<LinkedHashMap<String ,Object>>) resultList.get(0).get("data");
        List<LinkedHashMap<String ,Object>> lastDataList = new LinkedList<>();
        for (LinkedHashMap<String, Object> data : convertDataList) {
            LinkedHashMap<String, Object> sortedData = new LinkedHashMap<>();
            // 按照指定的顺序插入字段
            sortedData.put("skuNo", data.get("skuNo"));
            sortedData.put("productName", data.get("productName"));
            sortedData.put("spuNo", data.get("spuNo"));
            sortedData.put("saleStateName", data.get("saleStateName"));
            sortedData.put("warehouseName", data.get("warehouseName"));
            sortedData.put("orgName", data.get("orgName"));
            sortedData.put("warehouseLocation", data.get("warehouseLocation"));
            sortedData.put("realInventory", data.get("realInventory"));
            sortedData.put("usableInventory", data.get("usableInventory"));
            sortedData.put("1-2天", data.get("1-2天"));
            sortedData.put("2天以上", data.get("2天以上"));
            lastDataList.add(sortedData);
        }
        DynamicExcelDTO excelDTO = new DynamicExcelDTO();
        excelDTO.setHeaders(headMap);
        excelDTO.setData(lastDataList);
        return new PagingVO<>(Collections.singletonList(excelDTO), (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public List<InventoryEntity> listNegativeInventoryByWarehouseId(String warehouseId) {
        if(StringUtils.isBlank(warehouseId)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(InventoryEntity::getWarehouseId, warehouseId).lt(InventoryEntity::getQty,0).list();
    }

    @Override
    public List<InventoryEntity> listInventoryBySkuNos(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().select(InventoryEntity::getId,InventoryEntity::getSkuId,InventoryEntity::getSkuNo, InventoryEntity::getQty,
                        InventoryEntity::getWarehouseId,InventoryEntity::getDictInventoryStatus, InventoryEntity::getWarehouseLocation)
                .in(InventoryEntity::getSkuNo, skuNoList).list();
    }

    @Override
    public List<InventoryDTO.RealQtyDTO> getRealQty(List<String> skuIds, List<String> warehouseIds, List<String> inventoryStatusList) {
        return baseMapper.getRealQty(skuIds, warehouseIds, inventoryStatusList);
    }
}