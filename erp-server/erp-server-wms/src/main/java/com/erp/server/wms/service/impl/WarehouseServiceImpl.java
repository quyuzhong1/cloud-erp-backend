package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.WarehouseDTO.WarehouseUpdateStateDTO;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.model.wms.dto.excel.WarehouseExportExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.WarehouseManageTypeEnum;
import com.erp.model.wms.enums.WmsRedisKeyEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.kingdee.SyncKingdeeWarehouseService;
import com.erp.server.wms.listener.WarehouseExcelListener;
import com.erp.server.wms.mapper.WarehouseMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE;

/**
 * <p>
 * 仓库表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
@Slf4j
public class WarehouseServiceImpl extends SuperServiceImpl<WarehouseMapper, WarehouseEntity> implements WarehouseService {

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SyncKingdeeWarehouseService syncKingdeeWarehouseService;

    @Resource
    private RedisService redisService;
    @Resource
    private WarehouseLocationService warehouseLocationService;


    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private WarehouseMappingService warehouseMappingService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualWarehouseChannelService virtualWarehouseChannelService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private AuthDataFeign authDataFeign;

    @Override
    public List<WarehouseDTO.UpdateDTO> listWarehouseByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<WarehouseEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(WarehouseDTO.UpdateDTO.class, list);
    }

    @Override
    public List<WarehouseDTO.ListDTO> listApproveWarehouse(Boolean showByAuth) {
        String permissionSql = null;
        if (Objects.nonNull(showByAuth) && showByAuth){
            permissionSql = authDataFeign.getWarehousePermissionSql("id");
        }
        List<WarehouseDTO.ListDTO> resultList = baseMapper.listApproveWarehouse(permissionSql);
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        // 填充信息
        this.fillListData(resultList, warehouseBindMap);
        return resultList.stream().sorted(Comparator.comparing(WarehouseDTO.ListDTO::getDisabled)).collect(Collectors.toList());
    }

    @Override
    public List<WarehouseDTO.ListInventoryQtyDTO> listWarehouseInventoryQty(WarehouseDTO.ListInventoryQtyParamDTO dto) {
        if (Objects.nonNull(dto.getShowByAuth()) && dto.getShowByAuth()){
            dto.setPermissionSql(authDataFeign.getWarehousePermissionSql("wh.id"));
        }
        List<WarehouseEntity> list = baseMapper.listWarehouse(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.ListDTO.class, list);

        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        // 填充信息
        this.fillListData(resultList, warehouseBindMap);
        //添加及时库存数量
        List<WarehouseDTO.ListInventoryQtyDTO> warehouseList = fillListInventoryQty(dto,resultList);

        return warehouseList;
    }


    /**
     * @description: 填充库存数量
     * @author Will
     * @date: 2024/5/23 19:12
     * @param dto
     * @param list
     * @return List<ListInventoryQtyDTO>
     */
    private List<WarehouseDTO.ListInventoryQtyDTO> fillListInventoryQty (WarehouseDTO.ListInventoryQtyParamDTO dto, List<WarehouseDTO.ListDTO> list) {
        //sku去重
       List<String> skuIdList =  dto.getDetailList().stream().map(WarehouseDTO.ListInventoryDetailParamDTO::getSkuId).distinct().collect(Collectors.toList());

        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        if (CollectionUtils.isNotEmpty(bomChildrenList)) {
            List<String> bomSkuIdList = bomChildrenList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
            skuIdList.addAll(bomSkuIdList);
        }

        //仓库id集合
        List<String> warehouseIdList = list.stream().map(WarehouseDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        //查询SKU、仓库下的及时库存数量
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

        //查询虚拟仓库信息
        VirtualWarehouseChannelDTO.ListPlatformDTO ListPlatformDTO = new VirtualWarehouseChannelDTO.ListPlatformDTO();
        ListPlatformDTO.setWarehouseIdList(warehouseIdList);
        List<String> dictPlatformList = dto.getDetailList().stream().map(WarehouseDTO.ListInventoryDetailParamDTO::getDictPlatform).distinct().collect(Collectors.toList());
        ListPlatformDTO.setDictPlatformList(dictPlatformList);
        List<VirtualWarehouseRelationDTO.ListPlatformDTO> virtualWarehouseList = virtualWarehouseChannelService.listVirtualWarehouseByPlatform(ListPlatformDTO);

        //查询虚拟仓可用库存
        List<VirtualInventoryDTO.ViewQtyDTO> virtualUsableQtyList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(virtualWarehouseList)) {
            List<String> virtualWarehouseIdList = virtualWarehouseList.stream().map(VirtualWarehouseRelationDTO.ListPlatformDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
            VirtualInventoryDTO.ParamDTO vmParamDto = new VirtualInventoryDTO.ParamDTO();
            vmParamDto.setSkuIdList(skuIdList);
            vmParamDto.setWarehouseIdList(warehouseIdList);
            vmParamDto.setVirtualWarehouseIdList(virtualWarehouseIdList);
            virtualUsableQtyList = virtualInventoryService.getVmUsableQtyBySkuIdsAndWIdsAndVmIds(vmParamDto);
        }

        List<WarehouseDTO.ListInventoryQtyDTO> resultList = new ArrayList<>();
        for (WarehouseDTO.ListInventoryDetailParamDTO paramDTO : dto.getDetailList()) {
            WarehouseDTO.ListInventoryQtyDTO inventoryQtyDTO = new WarehouseDTO.ListInventoryQtyDTO();
            inventoryQtyDTO.setSkuId(paramDTO.getSkuId());
            List<WarehouseDTO.WarehouseInventoryQtyDTO> warehouseInventoryQtyList = new ArrayList<>();

            //虚拟仓
            String virtualWarehouseId = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getDictPlatform(), paramDTO.getDictPlatform())
                    && (CollectionUtils.isEmpty(obj.getRelationIdList()) || ObjectUtil.isEmpty(paramDTO.getRelationId()) || obj.getRelationIdList().contains(paramDTO.getRelationId())))
                    .map(VirtualWarehouseRelationDTO.ListPlatformDTO::getVirtualWarehouseId).findFirst().orElse("");

            //销售套装bom需要判断子件库存是否够使用
            List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(paramDTO.getSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                    .collect(Collectors.toList());

            for (WarehouseDTO.ListDTO listDTO : list) {
                WarehouseDTO.WarehouseInventoryQtyDTO warehouseInventoryQtyDTO = BeanMapperUtils.map(WarehouseDTO.WarehouseInventoryQtyDTO.class, listDTO);
                //即时库存
                Integer curInventoryQty = skuInventoryList.stream().filter(r ->Objects.equals(r.getSkuId(), paramDTO.getSkuId())
                                && Objects.equals(r.getWarehouseId(), listDTO.getId()))
                        .map(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal)
                        .reduce(MathUtil.ZERO,Integer::sum);
                warehouseInventoryQtyDTO.setInventoryQty(curInventoryQty);
                //无虚拟仓或者无库存数据
                if (CollectionUtils.isEmpty(virtualUsableQtyList) || CharSequenceUtil.isBlank(virtualWarehouseId)) {
                    warehouseInventoryQtyList.add(warehouseInventoryQtyDTO);
                    continue;
                }
                //实体仓下可用虚拟库存
                Integer virtualInventoryQty = virtualUsableQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), listDTO.getId())
                                && CharSequenceUtil.equals(obj.getSkuId(),paramDTO.getSkuId())
                                && CharSequenceUtil.equals(obj.getToVirtualWarehouseId(),virtualWarehouseId))
                        .map(VirtualInventoryDTO.ViewQtyDTO::getToVirtualWarehouseUsableQty).findFirst().orElse(MathUtil.ZERO);
                warehouseInventoryQtyDTO.setVirtualInventoryQty(virtualInventoryQty);
                //非bom则直接返回
                if (CollectionUtils.isEmpty(childList)) {
                    warehouseInventoryQtyList.add(warehouseInventoryQtyDTO);
                    continue;
                }
                //sku为bom则需要根据子件可用虚拟库存计算
                 List<Integer> parentUsableQtyList = new ArrayList<>();
                for (BomChildrenSkuDTO bomChildrenSkuDTO : childList) {
                    //虚拟仓是否缺货
                    Integer childVirtualUsableQty = virtualUsableQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), bomChildrenSkuDTO.getSkuId())
                                    && CharSequenceUtil.equals(obj.getToVirtualWarehouseId(), virtualWarehouseId)
                                    && CharSequenceUtil.equals(obj.getWarehouseId(), listDTO.getId())
                            ).map(VirtualInventoryDTO.ViewQtyDTO::getToVirtualWarehouseUsableQty)
                            .findFirst().orElse(MathUtil.ZERO);
                    //针对父级可用数量
                    BigDecimal divide = MathUtil.divide(new BigDecimal(childVirtualUsableQty), new BigDecimal(bomChildrenSkuDTO.getQuantity()));
                    Integer parentUsableQty = divide.intValue();
                    parentUsableQtyList.add(parentUsableQty);
                }
                Integer parentUsableQty = parentUsableQtyList.stream().min(Comparator.comparing(obj -> obj)).get();
                warehouseInventoryQtyDTO.setVirtualInventoryQty(parentUsableQty);
                warehouseInventoryQtyList.add(warehouseInventoryQtyDTO);
            }
            //排序
            List<WarehouseDTO.WarehouseInventoryQtyDTO> sortedList = warehouseInventoryQtyList.stream().sorted(Comparator.comparing(WarehouseDTO.WarehouseInventoryQtyDTO::getDisabled).reversed().thenComparing(WarehouseDTO.WarehouseInventoryQtyDTO::getInventoryQty).reversed()).collect(Collectors.toList());
            inventoryQtyDTO.setWarehouseInventoryQtyList(sortedList);
            resultList.add(inventoryQtyDTO);
        }
        return resultList;
    }

    /**
     * 填充列表信息
     */
    private void fillListData(List<WarehouseDTO.ListDTO> resultList, Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap) {
        List<ApproveStatusEnum> statusList = Collections.singletonList(ApproveStatusEnum.APPROVE);

        List<String> orgIds = resultList.stream().map(WarehouseDTO.ListDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);

        for (WarehouseDTO.ListDTO listDTO : resultList) {
            // 组织信息
            String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(listDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
            listDTO.setOrgName(orgName);
            if (!statusList.contains(listDTO.getApproveStatus())) {
                listDTO.setDisabled(true);
            }
            //平台信息
            OmsPlatformEnum platformEnum = this.checkAndGetPlatformInfo(listDTO, warehouseBindMap);
            listDTO.setDictPlatform(null == platformEnum ? "" : platformEnum.getCode());
            listDTO.setPlatformName(null == platformEnum ? "" : platformEnum.getName());
        }
    }

    @Override
    public List<WarehouseDTO.ListTreeDTO> listTree() {
        List<WarehouseDTO.ListDTO> list = listApproveWarehouse(Boolean.TRUE);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListTreeDTO> resultList = handleWarehouseTree(list);
        return resultList;
    }

    @Override
    public List<WarehouseDTO.ListTreeDTO> listTreeByParams(WarehouseDTO.ListParamDTO dto) {
        List<WarehouseDTO.ListDTO> list = listWarehouseByParams(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListTreeDTO> resultList = handleWarehouseTree(list);
        return resultList;
    }

    @Override
    public void assertDisabled(List<WarehouseDTO.WarehouseDisabledAssertDTO> assertList) {
        if (CollUtil.isEmpty(assertList)) {
            return;
        }
        List<WarehouseDTO.WarehouseDisabledAssertDTO> invalidList = new ArrayList<>();
        assertList.stream().forEach(item -> {
            // 仓库禁用/未审核
            if (CharSequenceUtil.isBlank(item.getWarehouseId())) {
                throw new ServiceException("仓库id不能为空");
            }
            WarehouseDTO.UpdateDTO warehouse = detailWithCache(item.getWarehouseId());
            if (ObjectUtil.isNotEmpty(warehouse)) {
                item.setWarehouseName(item.getWarehouseName());
            }
            if (ObjectUtil.isNotEmpty(warehouse) && (warehouse.getDisabled() || !ApproveStatusEnum.APPROVE.getStatus().equals(warehouse.getApproveStatusCode()))) {
                invalidList.add(item);
                return;
            }
            // 库区禁用/未审核
            if (CharSequenceUtil.isNotBlank(item.getWarehouseArea())) {
                WarehouseLocationEntity area = warehouseLocationService.findArea(item.getWarehouseId(), item.getWarehouseArea());
                if (ObjectUtil.isEmpty(area) || area.getDisabled()) {
                    invalidList.add(item);
                    return;
                }
            }
            // 仓位禁用/未审核
            if (CharSequenceUtil.isNotBlank(item.getWarehouseLocation())) {
                WarehouseLocationEntity location = warehouseLocationService.findByWarehouseIdAndCode(item.getWarehouseId(), item.getWarehouseLocation());
                if (ObjectUtil.isEmpty(location) || location.getDisabled()) {
                    invalidList.add(item);
                }
            }
        });
        if (CollUtil.isNotEmpty(invalidList)) {
            List<String> warehouseNameList = invalidList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::getWarehouseName).distinct().collect(Collectors.toList());
            List<String> warehoseAreaList = invalidList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::getWarehouseArea).distinct().collect(Collectors.toList());
            List<String> warehosueLocationList = invalidList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::getWarehouseArea).distinct().collect(Collectors.toList());
            warehouseNameList = CollUtil.isNotEmpty(warehouseNameList) ? warehouseNameList : Collections.emptyList();
            warehoseAreaList = CollUtil.isNotEmpty(warehoseAreaList) ? warehoseAreaList : Collections.emptyList();
            warehosueLocationList = CollUtil.isNotEmpty(warehosueLocationList) ? warehosueLocationList : Collections.emptyList();
            throw new ServiceException(ApiError.WAREHOUSE_AREA_LOCATION_DISABLED, JSONUtil.toJsonStr(warehouseNameList), JSONUtil.toJsonStr(warehoseAreaList), JSONUtil.toJsonStr(warehosueLocationList));
        }
    }

    @Override
    public List<WarehouseDTO.PullDownDTO> listOverseasWarehouse() {
        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        if (warehouseBindMap.isEmpty()) {
            return Collections.emptyList();
        }
        // 查询对应仓库
        Set<String> ids = warehouseBindMap.keySet();
        List<WarehouseEntity> list = lambdaQuery()
                .in(WarehouseEntity::getId, ids)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<WarehouseDTO.PullDownDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.PullDownDTO.class, list);

        //速卖通仓库映射
        List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS = warehouseMappingService.listMappingViewByDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        if (CollUtil.isNotEmpty(mappingViewDTOS)){
            for (WarehouseMappingDTO.MappingViewDTO viewDTO : mappingViewDTOS) {
                WarehouseDTO.PullDownDTO add  = new WarehouseDTO.PullDownDTO();
                add.setId(viewDTO.getWarehouseId());
                add.setName(viewDTO.getWarehouseName());
                resultList.add(add);
            }
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public PagingVO<WarehouseDTO.ListDTO> selectPaging(PagingDTO<WarehouseDTO.SelectDTO> searchDTO) {
        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        WarehouseDTO.SelectDTO params = searchDTO.getParams();
        //查询配置过滤对应组织仓库
        if(params.isFilterOrgFlag()){
            List<DictBasicDTO.ListDTO> listDTOList = dictBasicService.getByKey(WmsConstant.WAREHOUSE_BY_FILTER_ORG);
            if(CollectionUtils.isNotEmpty(listDTOList)){
                DictBasicDTO.ListDTO orgDTOList = listDTOList.get(0);
                String orgArr = orgDTOList.getValue();
                params.setOrgIds(Arrays.asList(orgArr.split(",")));
            }
        }
        if(params.isFilterSelfAddFlag()){
            params.setWarehouseManageType(WarehouseManageTypeEnum.SELF_BUILD.getCode());
        }
        if (Objects.nonNull(params.getShowByAuth()) && params.getShowByAuth()){
            params.setPermissionSql(authDataFeign.getWarehousePermissionSql("wh.id"));
        }
        IPage<WarehouseDTO.ListDTO> pagResult = baseMapper.pagingSelect(query, params);
        List<WarehouseDTO.ListDTO> records = pagResult.getRecords();
        handleSelect(records);
        //排序
        List<WarehouseDTO.ListDTO> list = records.stream().sorted(Comparator.comparing(WarehouseDTO.ListDTO::getDisabled)).collect(Collectors.toList());
        pagResult.setRecords(list);
        return new PagingVO<>(pagResult);
    }

    @Override
//    @Cacheable(cacheNames = "cache:wms:listWarehouseWithCaches",keyGenerator = "myKeyGenerator")
    public List<WarehouseEntity> listWarehouseWithCaches() {
        return this.list();
    }
    @Override
    public PagingVO<WarehouseExportExcelDTO> exportWarehouse(PagingDTO<WarehouseDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        //获取导出数据
        Page<WarehouseDTO.PagingViewDTO> page = baseMapper.getExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        List<WarehouseExportExcelDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            //获取到字典数据类型
            List<String> dictTypeList = new ArrayList<>(3);
            dictTypeList.add(DictBasicEnum.WAREHOUSE_TYPE.getKey());
            dictTypeList.add(DictBasicEnum.WAREHOUSE_MANAGE_TYPE.getKey());
            dictTypeList.add(DictBasicEnum.GEOGRAPHY_LOCATION.getKey());
            List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(dictTypeList);
            List<String> userIdList = page.getRecords().stream().map(WarehouseDTO.PagingViewDTO::getChargeId).distinct().collect(Collectors.toList());
            //获取用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
            List<String> orgIdList = page.getRecords().stream().map(WarehouseDTO.PagingViewDTO::getOrgId).collect(Collectors.toList());
            orgIdList.addAll(page.getRecords().stream().map(WarehouseDTO.PagingViewDTO::getShippingOrganization).collect(Collectors.toList()));
            orgIdList.addAll(page.getRecords().stream().map(WarehouseDTO.PagingViewDTO::getFinancialOrganization).collect(Collectors.toList()));
            List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

            List<DictCountryDTO.ListDTO> dictCountryEntityList = sysUserFeign.countryList();
            List<com.erp.model.oms.entity.DictBasicEntity> channelAffiliationList = FeignQuery.create(com.erp.model.oms.entity.DictBasicEntity.class)
            		.in(com.erp.model.oms.entity.DictBasicEntity::getValue, page.getRecords().stream().map(WarehouseDTO.PagingViewDTO::getChannelAffiliation).collect(Collectors.toList()))
            		.in(com.erp.model.oms.entity.DictBasicEntity::getType, "salesPlatform")
            		.list();

            List<String> warehouseIds = page.getRecords().stream().map(WarehouseDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
            List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS = warehouseMappingService.listMappingViewByWarehouseIds(warehouseIds);

            for (WarehouseDTO.PagingViewDTO item : page.getRecords()) {
                WarehouseExportExcelDTO excelDTO = new WarehouseExportExcelDTO();
                BeanMapper.copy(item, excelDTO);
                //类型id
                String typeId = item.getTypeId();
                String typeName = dictBasicList.stream().filter(d -> d.getId().equals(typeId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setTypeName(typeName);

                //审核名
                ApproveStatusEnum statusEnum = item.getApproveStatus();
                excelDTO.setApproveStatusName(statusEnum.getName());

                //负责人id
                String chargeId = item.getChargeId();
                String userName = userList.stream().filter(u -> chargeId.equals(u.getUserId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                excelDTO.setChargeName(userName);

                //组织id
                String orgId = item.getOrgId();
                String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setOrgName(orgName);
                excelDTO.setEnabled(item.getDisabled() ? "停用" : "启用");
                excelDTO.setIsVirtual(item.getIsVirtual() ? "是" : "否");
                excelDTO.setOnwayWarehouseName(item.getOnwayWarehouseName());
                WarehouseMappingDTO.MappingViewDTO mappingViewDTO = mappingViewDTOS.stream().filter(req -> req.getWarehouseId().equals(item.getId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(mappingViewDTO)) {
                    excelDTO.setThirdWarehouseName(mappingViewDTO.getThirdWarehouseName());
                }
                //经营类型
                String warehouseManageType = item.getWarehouseManageType();
                String warehouseManageTypeName = dictBasicList.stream().filter(d -> warehouseManageType.equals(d.getValue())).
                        map(DictBasicEntity::getName).findFirst().orElse("");
                excelDTO.setWarehouseManageTypeName(warehouseManageTypeName);

                //地理位置
                String geographyLocation = item.getGeographyLocation();
                String geographyLocationName = dictBasicList.stream().filter(d -> geographyLocation.equals(d.getValue())).
                        map(DictBasicEntity::getName).findFirst().orElse("");
                excelDTO.setGeographyLocationName(geographyLocationName);

                //国家
                DictCountryDTO.ListDTO countryDto = dictCountryEntityList.stream().filter(v->v.getId().equals(item.getCountry())).findFirst().orElse(new DictCountryDTO.ListDTO());
                excelDTO.setCountryName(countryDto.getNameCn());
                
                excelDTO.setChannelAffiliationName(channelAffiliationList.stream().filter(o -> o.getValue().equals(item.getChannelAffiliation())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse(""));
                excelDTO.setShippingOrganizationName(orgList.stream().filter(o -> o.getId().equals(item.getShippingOrganization())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse(""));
                excelDTO.setFinancialOrganizationName(orgList.stream().filter(o -> o.getId().equals(item.getFinancialOrganization())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse(""));
                
                resultList.add(excelDTO);
            }
        }
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }


    /**
     * @description: 分页下拉处理
     * @author Will
     * @date: 2024/5/24 13:06
     * @param records
     */
    private void handleSelect (List<WarehouseDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        // 填充信息
        this.fillListSelectData(records, warehouseBindMap);
    }

    /**
     * 填充列表信息
     */
    private void fillListSelectData(List<WarehouseDTO.ListDTO> resultList, Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap) {
        List<ApproveStatusEnum> statusList = Collections.singletonList(ApproveStatusEnum.APPROVE);

        List<String> orgIds = resultList.stream().map(WarehouseDTO.ListDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        for (WarehouseDTO.ListDTO listDTO : resultList) {
            // 组织信息
            String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(listDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
            listDTO.setOrgName(orgName);
            if (!statusList.contains(listDTO.getApproveStatus())) {
                listDTO.setDisabled(true);
            }
            //平台信息
            OmsPlatformEnum platformEnum = this.checkAndGetPlatformInfo(listDTO, warehouseBindMap);
            listDTO.setDictPlatform(null == platformEnum ? "" : platformEnum.getCode());
            listDTO.setPlatformName(null == platformEnum ? "" : platformEnum.getName());
            //平台类型
            DictBasicDTO.ListDTO dict = dictList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(listDTO.getTypeId())).findFirst().orElse(null);
            listDTO.setTypeName(Objects.nonNull(dict) ? dict.getName() : CharSequenceUtil.EMPTY);
        }
    }

    /**
     * 检查绑定海外仓库服务商信息
     */
    private OmsPlatformEnum checkAndGetPlatformInfo(WarehouseDTO.ListDTO listDTO, Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap) {
        List<OverseasProviderDTO.ListWithWarehouseDTO> listWithWarehouseDTOS = warehouseBindMap.get(listDTO.getId());
        if (CollectionUtils.isNotEmpty(listWithWarehouseDTOS)) {
            // 取指定服务商，否则取第一个
            OverseasProviderDTO.ListWithWarehouseDTO warehouseDTO = listWithWarehouseDTOS.stream().findFirst().orElse(null);
            // 设置平台信息
            if (null != warehouseDTO) {
                return OmsPlatformEnum.getByCode(warehouseDTO.getCode());
            }
        }
        return null;
    }


    /**
     * 添加仓库
     *
     * @param dto
     * @return com.erp.model.wms.entity.WarehouseEntity
     * @author yl
     * @date 2023-03-22 10:17
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String add(WarehouseDTO.AddDTO dto) {
        //检查名称
        checkName(null, dto.getName());
        //检查金蝶编号
        checkKingdeeWarehouseCode("", dto.getKingdeeWarehouseCode());
        WarehouseEntity warehouse = new WarehouseEntity();
        BeanMapper.copy(dto, warehouse);
        this.validateOpenCloseTime(warehouse);
        //如果设置了在途仓，获取匹配在途仓名称
        if (CharSequenceUtil.isNotBlank(dto.getOnwayWarehouseId())) {
            WarehouseEntity entity = this.getById(dto.getOnwayWarehouseId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_ONWAY_WAREHOUSE_NOT_EXIST);
            }
            warehouse.setOnwayWarehouseName(entity.getName());
        }

        Boolean result = this.save(warehouse);
        if (result) {

            //如果设置了第三方仓绑定
            if (CharSequenceUtil.isNotBlank(dto.getThirdWarehouseName())) {
                WarehouseMappingEntity checkThirdWarehouseNameExist = warehouseMappingService.checkThirdWarehouseNameExist(dto.getThirdWarehouseName(), PlatformDictEnum.ALI_EXPRESS.getCode());
                if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist)) {
                    WarehouseEntity entity = this.getById(checkThirdWarehouseNameExist.getWarehouseId());
                    throw new ServiceException(ApiError.THIRD_WAREHOUSE_NAME_EXIST, PlatformDictEnum.ALI_EXPRESS.getCode(), dto.getThirdWarehouseName(), entity.getName());
                }
                WarehouseMappingDTO.AddDTO addDTO = new WarehouseMappingDTO.AddDTO();
                addDTO.setName(dto.getThirdWarehouseName());
                addDTO.setWarehouseId(warehouse.getId());
                addDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
                warehouseMappingService.add(addDTO);
            }

            //自动创建库区，仓位
            autoAddAreaAndLocation(warehouse);
            //添加用户仓库权限
            AuthUserWarehouseDTO.AddUserWarehouseAuthDTO addUserWarehouseAuthDTO = new AuthUserWarehouseDTO.AddUserWarehouseAuthDTO();
            addUserWarehouseAuthDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
            addUserWarehouseAuthDTO.setWarehouseIds(Collections.singletonList(warehouse.getId()));
            authDataFeign.addUserWarehouseAuth(addUserWarehouseAuthDTO);
            return warehouse.getId();
        }
        return "";
    }

    private void autoAddAreaAndLocation(WarehouseEntity warehouse) {
        WarehouseAreaDTO.Add areaAddDTO = new WarehouseAreaDTO.Add();
        areaAddDTO.setAreaType(WarehouseAreaTypeEnum.STAGING_AREA.getCode());
        areaAddDTO.setWarehouseId(warehouse.getId());
        areaAddDTO.setCode(WarehouseLocationAreaTypeEnum.PICK.getCode());
        areaAddDTO.setName(WarehouseAreaTypeEnum.STAGING_AREA.getName());
        String areaId = warehouseLocationService.addArea(areaAddDTO);
        WarehouseLocationDTO.AddDTO addDTO = new WarehouseLocationDTO.AddDTO();
        addDTO.setWarehouseId(warehouse.getId());
        addDTO.setWarehouseAreaId(areaId);
        addDTO.setCode("");
        addDTO.setName("空仓位");
        warehouseLocationService.add(addDTO);
    }


    /**
     * 修改仓库
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateWarehouse(WarehouseDTO.UpdateDTO dto) {
        // 删除缓存
        removeCache(Collections.singletonList(dto.getId()));
        //仓库id
        String warehouseId = dto.getId();
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        String code = dto.getKingdeeWarehouseCode();
        String name = dto.getName();
        checkName(warehouseId, name);
        checkKingdeeWarehouseCode(warehouseId, code);
        //负库存按钮调整，校验是否通过
        if(!dto.getAllowNegativeInventory() && warehouse.getAllowNegativeInventory()){
            List<InventoryEntity> negativeInventoryList =  inventoryService.listNegativeInventoryByWarehouseId(warehouseId);
            if(CollectionUtils.isNotEmpty(negativeInventoryList)){
                List<String> skuNos = negativeInventoryList.stream().map(InventoryEntity::getSkuNo).collect(Collectors.toList());
                throw new ServiceException("仓库{}sku{}存在负库存，不允许修改为不允许负库存",warehouse.getName(),skuNos);
            }
        }
        //仓库下绑定第三方店铺不能修改为禁用状态
        //Delete by Edison.qu 2024-07-23 去除不必要的限制
//        if (Objects.nonNull(dto.getDisabled()) && !Objects.equals(dto.getDisabled(), warehouse.getDisabled()) && Objects.equals(dto.getDisabled(), true)) {
//            checkDmpThirdMapping(warehouseId,warehouse.getName());
//        }
        BeanMapper.copy(dto, warehouse);
        this.validateOpenCloseTime(warehouse);

        //如果设置了在途仓，获取匹配在途仓名称
        if (CharSequenceUtil.isNotBlank(dto.getOnwayWarehouseId())) {
            WarehouseEntity entity = this.getById(dto.getOnwayWarehouseId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_ONWAY_WAREHOUSE_NOT_EXIST);
            }
            warehouse.setOnwayWarehouseName(entity.getName());
        }else {
            warehouse.setOnwayWarehouseName("");
            warehouse.setOnwayWarehouseId("");
        }
        if(dto.getChannelAffiliation() == null) {
        	warehouse.setChannelAffiliation("");
        }
        if(StringUtils.isBlank(dto.getCountry())) {
        	warehouse.setCountry("");
        }
        Boolean result = this.updateById(warehouse);
        if (result) {
            //如果设置了第三方仓绑定
            WarehouseMappingDTO.MappingViewDTO mappingViewByDictPlatform = warehouseMappingService.getMappingViewByDictPlatform(warehouseId, PlatformDictEnum.ALI_EXPRESS.getCode());

            WarehouseMappingEntity checkThirdWarehouseNameExist = warehouseMappingService.checkThirdWarehouseNameExist(dto.getThirdWarehouseName(), PlatformDictEnum.ALI_EXPRESS.getCode());

            WarehouseMappingDTO.UpdateDTO updateDTO = new WarehouseMappingDTO.UpdateDTO();
            if (ObjectUtil.isNotEmpty(mappingViewByDictPlatform)) {
                updateDTO.setId(mappingViewByDictPlatform.getId());
                if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist) && !checkThirdWarehouseNameExist.getId().equals(mappingViewByDictPlatform.getId())) {
                    throw new ServiceException(ApiError.THIRD_WAREHOUSE_NAME_EXIST, PlatformDictEnum.ALI_EXPRESS.getCode(), dto.getThirdWarehouseName());
                }
            } else {
                if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist)) {
                    throw new ServiceException(ApiError.THIRD_WAREHOUSE_NAME_EXIST, PlatformDictEnum.ALI_EXPRESS.getCode(), dto.getThirdWarehouseName());
                }
            }
            updateDTO.setName(dto.getThirdWarehouseName());
            updateDTO.setWarehouseId(warehouse.getId());
            updateDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
            warehouseMappingService.update(updateDTO);
            return warehouseId;
        }
        return "";
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:16
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(WarehouseDTO.AddDTO dto) {
        String warehouseId = this.add(dto);
        if (CharSequenceUtil.isBlank(warehouseId)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Collections.singletonList(warehouseId));
        return result;
    }


    /**
     * 仓库提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        // 删除缓存
        removeCache(ids);
        List<WarehouseEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();

        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        return result;
    }


    /**
     * 更改仓库状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean updateStatus(WarehouseUpdateStateDTO dto) {
        // 删除缓存
        removeCache(Collections.singletonList(dto.getId()));
        //仓库id
        String warehouseId = dto.getId();
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        //仓库下绑定第三方店铺不能修改为禁用状态
        //Delete by Edison.qu 2024-07-23 去除不必要的限制
//        if (Objects.nonNull(dto.getState()) && !Objects.equals(dto.getState(), warehouse.getDisabled()) && Objects.equals(dto.getState(), true)) {
//            checkDmpThirdMapping(warehouseId,warehouse.getName());
//        }
        Boolean state = dto.getState();
		if(Boolean.FALSE.equals(state)) {
        	warehouse.setOpenTime(dto.getEnableTime());
        }
        warehouse.setDisabled(state);
        this.validateOpenCloseTime(warehouse);
        this.updateById(warehouse);

        //发送金蝶
        String operate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        if (state) {
            //Delete by Edison.qu 2024-07-23 去除不必要的限制:仓库绑定店铺，不允许禁用
//            List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByWarehouseIds(Collections.singletonList(dto.getId()));
//            if (CollectionUtils.isNotEmpty(shopInfoEntities)) {
//                throw new ServiceException(ApiError.SHOP_INFO_EXIST_WAREHOUSE_NOT_DISABLE, shopInfoEntities.get(MathUtil.ZERO).getName());
//            }
            operate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        //审核通过后发送金蝶
        sendPushTask(Collections.singletonList(warehouse),operate);
        return Boolean.TRUE;
    }

    /**
     * 审核仓库
     *
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO approve(WarehouseEntity entity, String type, String comment, Boolean isNeedProcess) {
        // 删除缓存
        removeCache(Collections.singletonList(entity.getId()));
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getName(),ApiError.ERROR_98006.msg);
        }
        if (WmsConstant.PASS.equals(type)) {
            //审核通过
            this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.APPROVE);
            //审核通过后发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        } else {
            //审核不通过
            this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.REJECT);
        }
        return BatchResultDTO.success(entity.getId(), entity.getName(), "操作成功");
    }


    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(WarehouseEntity entity) {
        // 删除缓存
        removeCache(Collections.singletonList(entity.getId()));
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getName(),ApiError.ERROR_99003.msg);
        }
        List<WarehouseEntity> list = Collections.singletonList(entity);
        //仓库已绑定店铺不允许反审核
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByWarehouseIds(Collections.singletonList(entity.getId()));
        ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> req.getWarehouseId().equals(entity.getId())).distinct().findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
            return BatchResultDTO.fail(entity.getId(),entity.getName(),String.format(ApiError.SHOP_INFO_EXIST_WAREHOUSE_NOT_DISAPPROVE.msg, shopInfoEntity.getName()));
        }
        this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.WAIT_SUBMIT);
        //仓库下绑定第三方店铺不能进行反审核
        list.forEach(warehouseEntity -> {
            checkDmpThirdMapping(warehouseEntity.getId(),warehouseEntity.getName());
        });

        this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.WAIT_SUBMIT);
        //反审核后发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getName(), "操作成功");
    }

    private void checkDmpThirdMapping(String warehouseId,String warehouseName) {
        ThirdMappingDTO.ViewParamDTO viewParamDTO=new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setType(ThirdSysTypeEnum.WAREHOUSE.getCode());
        viewParamDTO.setSysId(warehouseId);
        Boolean hasThirdMapping = dmpThirdMappingFeign.getWhetherBind(viewParamDTO);
        if (!hasThirdMapping){
            throw new ServiceException(ApiError.EXIST_THIRD_WAREHOUSE_MAPPING,warehouseName);
        }
    }


    /**
     * 批量删除
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 12:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> deleteByIds(List<String> ids) {
        // 删除缓存
        removeCache(ids);

        List<WarehouseEntity> list = this.listByIds(ids);
        //待提交
//        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
//        long count = list.stream().filter(s -> !waitSubmitStatus.equals(s.getApproveStatus().getStatus())).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
        List<WarehouseEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (WarehouseEntity entity : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getKingdeeWarehouseCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getKingdeeWarehouseCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(WarehouseEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }

        //删除发送金蝶
        sendPushTask(removeList,SyncOperateEnum.OPERATE_DELETE.getCode());
        this.removeByIds(removeList);
        return resultDTOList;
    }


    /**
     * 获取仓库详情
     *
     * @param warehouseId
     * @return com.erp.model.wms.dto.WarehouseDTO.UpdateDTO
     * @author yl
     * @date 2023-03-22 14:30
     */
    @Override
    public WarehouseDTO.UpdateDTO view(String warehouseId) {
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        WarehouseDTO.UpdateDTO dto = new WarehouseDTO.UpdateDTO();
        BeanMapper.copy(warehouse, dto);
        ApproveStatusEnum approveStatusEnum = warehouse.getApproveStatus();
        dto.setApproveStatusCode(approveStatusEnum.getStatus());

        WarehouseMappingDTO.MappingViewDTO mappingViewByDictPlatform = warehouseMappingService.getMappingViewByDictPlatform(warehouseId, PlatformDictEnum.ALI_EXPRESS.getCode());
        if (ObjectUtil.isNotEmpty(mappingViewByDictPlatform)) {
            dto.setThirdWarehouseName(mappingViewByDictPlatform.getThirdWarehouseName());
        }
        return dto;
    }


    /**
     * 分页获取仓库数据
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-22 14:51
     */
    @Override
    public PagingVO<WarehouseDTO.PagingViewDTO> paging(PagingDTO<WarehouseDTO.PagingParamDTO> dto) {
        WarehouseDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<WarehouseDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        //获取到字典数据类型
        List<String> dictTypeList = new ArrayList<>(3);
        dictTypeList.add(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.WAREHOUSE_MANAGE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.GEOGRAPHY_LOCATION.getKey());
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(dictTypeList);
        List<String> userIdList = list.stream().map(WarehouseDTO.PagingViewDTO::getChargeId).distinct().collect(Collectors.toList());
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        List<String> orgIdList = list.stream().map(WarehouseDTO.PagingViewDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        List<DictCountryDTO.ListDTO> dictCountryEntityList = sysUserFeign.countryList();
        for (WarehouseDTO.PagingViewDTO item : list) {
            //类型id
            String typeId = item.getTypeId();
            String typeName = dictBasicList.stream().filter(d -> d.getId().equals(typeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setTypeName(typeName);
            //审核名
            ApproveStatusEnum statusEnum = item.getApproveStatus();
            item.setApproveStatusName(statusEnum.getName());
            item.setApproveStatusCode(statusEnum.getStatus());
            //负责人id
            String chargeId = item.getChargeId();
            String userName = userList.stream().filter(u -> chargeId.equals(u.getUserId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
            item.setChargeName(userName);
            //组织id
            String orgId = item.getOrgId();
            String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setOrgName(orgName);
            //经营类型
            String warehouseManageType = item.getWarehouseManageType();
            String warehouseManageTypeName = dictBasicList.stream().filter(d -> warehouseManageType.equals(d.getValue())).
                    map(DictBasicEntity::getName).findFirst().orElse("");
            item.setWarehouseManageTypeName(warehouseManageTypeName);

            //地理位置
            String geographyLocation = item.getGeographyLocation();
            String geographyLocationName = dictBasicList.stream().filter(d -> geographyLocation.equals(d.getValue())).
                    map(DictBasicEntity::getName).findFirst().orElse("");
            item.setGeographyLocationName(geographyLocationName);

            //国家
            DictCountryDTO.ListDTO countryDto = dictCountryEntityList.stream().filter(v->v.getId().equals(item.getCountry())).findFirst().orElse(new DictCountryDTO.ListDTO());
            item.setCountryName(countryDto.getNameCn());
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出仓库数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-22 16:08
     */
    @Override
    public void exportWarehouse(WarehouseDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("仓库数据", EXPORT_WMS_WAREHOUSE.getCode(), dto);
    }


    /**
     * 下载仓库模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-22 17:06
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/warehouse.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


    /**
     * 导入仓库数据
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 17:17
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //获取到字典数据类型
        List<String> dictTypeList = new ArrayList<>(3);
        dictTypeList.add(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.WAREHOUSE_MANAGE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.GEOGRAPHY_LOCATION.getKey());
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(dictTypeList);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(new ArrayList<>());
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();

        List<WarehouseEntity> warehouseList = this.list();
        WarehouseExcelListener excelListenerUtil = new WarehouseExcelListener(this, dictBasicList, userList, orgList, warehouseList, warehouseMappingService,countryList);
        try {
            EasyExcel.read(excelFile.getInputStream(), WarehouseExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("仓库导入错误！", e);
            return Boolean.FALSE;
        }
        List<WarehouseExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "仓库错误信息";
            ExcelUtil.export(fileName, "warehouseError", errorList, WarehouseExcelDTO.class, response);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 11:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(WarehouseDTO.UpdateDTO dto) {
        String id = this.updateWarehouse(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(id));

    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(WarehouseEntity::getId, id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), WarehouseEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public WarehouseDTO.UpdateDTO detailWithCache(String id) {
        if (StrUtils.isEmpty(id)) {
            return null;
        }
        String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(id);
        Object obj = redisService.getCacheObject(redisKey);
        if (Objects.nonNull(obj)) {
            // 判断是否空缓存
            if (Objects.equals(RedisService.EMPTY_CACHE_VALUE, obj)) {
                log.info("从redis缓存中查询到仓库信息，仓库id:{}，内容为空", id);
                return new WarehouseDTO.UpdateDTO();
            }
            log.info("从redis缓存中查询到仓库信息，仓库id:{}，内容:{}", id, obj);
            WarehouseDTO.UpdateDTO warehouseDTO = (WarehouseDTO.UpdateDTO) obj;
            return warehouseDTO;
        }
        // 从数据库中查询
        WarehouseEntity warehouse = this.getById(id);
        if (Objects.isNull(warehouse)) {
            log.info("从数据库中没有查询到仓库信息，仓库id:{}，缓存空", id);
            redisService.setCacheObject(redisKey, RedisService.EMPTY_CACHE_VALUE, RedisService.ONE_DAY_CACHE_TIME, TimeUnit.SECONDS);
            return new WarehouseDTO.UpdateDTO();
        }
        WarehouseDTO.UpdateDTO dto = new WarehouseDTO.UpdateDTO();
        BeanMapper.copy(warehouse, dto);
        ApproveStatusEnum approveStatusEnum = warehouse.getApproveStatus();
        dto.setApproveStatusCode(approveStatusEnum.getStatus());
        redisService.setCacheObject(redisKey, dto, RedisService.ONE_DAY_CACHE_TIME, TimeUnit.SECONDS);
        return dto;
    }

    /**
     * 根据金蝶仓库code 获取到对应仓库信息
     *
     * @param kingdeeWarehouseCodeList
     * @return java.util.List<com.erp.model.wms.entity.WarehouseEntity>
     * @author yl
     * @date 2023-06-27 16:34
     */
    @Override
    public List<WarehouseEntity> listByKingdeeCodeList(List<String> kingdeeWarehouseCodeList) {
        if (CollectionUtils.isEmpty(kingdeeWarehouseCodeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(WarehouseEntity::getKingdeeWarehouseCode, kingdeeWarehouseCodeList).list();
    }


    @Override
    public List<WarehouseDTO.ListDTO> listWarehouseByParams(WarehouseDTO.ListParamDTO dto) {
        List<String> typeIdList = null;
        if (CollUtil.isNotEmpty(dto.getTypeCodeList())){
            List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
            typeIdList = dictList.stream().filter(e -> dto.getTypeCodeList().contains(e.getValue())).map(DictBasicDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        }
        dto.setTypeIdList(typeIdList);
        if (Objects.nonNull(dto.getShowByAuth()) && dto.getShowByAuth()){
            dto.setPermissionSql(authDataFeign.getWarehousePermissionSql("id"));
        }
        List<WarehouseEntity> list = baseMapper.listByParam(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<WarehouseDTO.ListDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.ListDTO.class, list);
        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        DictBasicEntity basic = dictBasicService.getOne(Wrappers.<DictBasicEntity>lambdaQuery().eq(DictBasicEntity::getValue, WmsConstant.SUPPLIER));
        // 填充信息
        this.fillListData(resultList, warehouseBindMap);

        return resultList.stream()
                .filter(e -> CharSequenceUtil.isBlank(dto.getDictPlatform()) || (CharSequenceUtil.isNotBlank(dto.getDictPlatform()) && e.getDictPlatform().equalsIgnoreCase(dto.getDictPlatform())))
                .filter(e -> ObjectUtil.isEmpty(dto.getIsSupplier()) || (Boolean.TRUE.equals(dto.getIsSupplier()) && e.getTypeId().equals(basic.getId())))
                .sorted(Comparator.comparing(WarehouseDTO.ListDTO::getDisabled))
                .collect(Collectors.toList());
    }

    @Override
    public PagingVO<WarehouseDTO.PagingNoPermissionDTO> pagingNoPermission(PagingDTO<WarehouseDTO.PagingDTO> dto) {
        WarehouseDTO.PagingDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<WarehouseDTO.PagingNoPermissionDTO> pageData = baseMapper.pagingNoPermission(query, params);
        List<WarehouseDTO.PagingNoPermissionDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        //获取到仓库类型
        List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        //获取用户信息
        List<String> orgIdList = list.stream().map(WarehouseDTO.PagingNoPermissionDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

        for (WarehouseDTO.PagingNoPermissionDTO item : list) {
            //类型id
            String typeId = item.getTypeId();
            String typeName = dictBasicList.stream().filter(d -> d.getId().equals(typeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setTypeName(typeName);
            //组织id
            String orgId = item.getOrgId();
            String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setOrgName(orgName);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<WarehouseDTO.PagingProductViewDTO> pagingProduct(PagingDTO<WarehouseDTO.PagingProductDTO> dto) {
        WarehouseDTO.PagingProductDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<WarehouseDTO.PagingProductViewDTO> pageData = baseMapper.pagingProduct(query, params);
        List<WarehouseDTO.PagingProductViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //获取组织信息
        List<String> orgIdList = list.stream().map(WarehouseDTO.PagingProductViewDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        list.forEach(item -> {
            //组织id
            String orgId = item.getOrgId();
            String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setOrgName(orgName);
        });
        return new PagingVO<>(pageData);
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:41
     */
    private Boolean updateApproveStatus(List<WarehouseEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isEmpty(list)){
            return Boolean.TRUE;
        }
        List<String> ids = list.stream().map(WarehouseEntity::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        return this.lambdaUpdate().in(WarehouseEntity::getId,ids).set(WarehouseEntity::getApproveStatus, statusEnum).update();
    }


    /**
     * 检查金蝶code 是否重复
     *
     * @param kingdeeWarehouseCode
     * @return void
     * @author yl
     * @date 2023-03-22 10:27
     */
    private void checkKingdeeWarehouseCode(String id, String kingdeeWarehouseCode) {
        LambdaQueryWrapper<WarehouseEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(id)) {
            queryWrapper.ne(WarehouseEntity::getId, id);
        }
        queryWrapper.eq(WarehouseEntity::getKingdeeWarehouseCode, kingdeeWarehouseCode);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99000);
        }

    }

    /**
     * 检查仓库名是否存在
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-03-22 10:25
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<WarehouseEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(id)) {
            queryWrapper.ne(WarehouseEntity::getId, id);
        }
        queryWrapper.eq(WarehouseEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
    }

    private void removeCache(List<String> ids) {
        List<String> redisKeys = Lists.newArrayList();
        ids.stream().forEach(id -> {
            String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(id);
            redisKeys.add(redisKey);
        });

        redisService.deleteObject(redisKeys);
    }

    /**
     * @param list
     * @return List<ListTreeDTO>
     * @description: 处理下拉数据
     * @author Will
     * @date: 2023/9/6 15:59
     */
    private List<WarehouseDTO.ListTreeDTO> handleWarehouseTree(List<WarehouseDTO.ListDTO> list) {

        //获取到仓库类型
        List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());

        List<WarehouseDTO.ListTreeDTO> resultList = new ArrayList<>();
        Map<String, List<WarehouseDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(WarehouseDTO.ListDTO::getTypeId));
        for (Map.Entry<String, List<WarehouseDTO.ListDTO>> entry : map.entrySet()) {
            WarehouseDTO.ListTreeDTO listTreeDTO = new WarehouseDTO.ListTreeDTO();
            String key = entry.getKey();
            listTreeDTO.setId(key);
            //类型id
            String typeName = dictBasicList.stream().filter(d -> d.getId().equals(key)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listTreeDTO.setName(typeName);
            listTreeDTO.setListDTO(entry.getValue());
            resultList.add(listTreeDTO);
        }
        return resultList;
    }

    /**
     * 根据名称获取仓库id
     *
     * @param nameList
     * @author hyj
     * @date 2024/4/18 10:26
     */
    @Override
    public List<WarehouseDTO.ListDTO> listByNames(@Param("nameList") List<String> nameList) {
        String permissionSql = authDataFeign.getWarehousePermissionSql("id");
        return baseMapper.listByNames(nameList,permissionSql);
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<WarehouseEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeWarehouseService.syncDataToKingdee(obj, operate);
            syncKingdeeWarehouseService.syncDataToSdy(obj, operate);
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

    @Override
	public boolean checkOpenCloseTime(WarehouseEntity warehouseEntity) {
		Boolean disabled = warehouseEntity.getDisabled();
        if(Boolean.TRUE.equals(disabled)) {
        	warehouseEntity.setCloseTime(LocalDateTime.now());
        }
        return Boolean.FALSE.equals(disabled) && warehouseEntity.getOpenTime() == null;
	}

    private void validateOpenCloseTime(WarehouseEntity warehouseEntity) {
		if(this.checkOpenCloseTime(warehouseEntity)) {
			throw new ServiceException(ApiError.OPEN_STATUS_OPEN_TIME_NOT_NULL);
		}
	}

}
