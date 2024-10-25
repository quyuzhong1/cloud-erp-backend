package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.mrp.mapper.OverseasHistoryInventoryMapper;
import com.erp.server.mrp.service.OverseasHistoryInventoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓库存 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Service
public class OverseasHistoryInventoryServiceImpl extends SuperServiceImpl<OverseasHistoryInventoryMapper, OverseasHistoryInventoryEntity> implements OverseasHistoryInventoryService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;


    @Override
    public void saveTodayInventory(List<OverseasInventoryEntity> overseasHistoryInventory, LocalDate calculationDate) {
        List<OverseasHistoryInventoryEntity> entityList = list(Wrappers.<OverseasHistoryInventoryEntity>lambdaQuery().eq(OverseasHistoryInventoryEntity::getBillDate, calculationDate));
        List<OverseasHistoryInventoryEntity> entities = overseasHistoryInventory.parallelStream()
                .map(v -> {
                    OverseasHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getWarehouseCode().equals(e.getWarehouseCode()))
                            .filter(e -> v.getDictPlatform().equals(e.getDictPlatform()))
                            .filter(e -> v.getSkuId().equals(e.getSkuId()))
                            .findFirst()
                            .orElse(new OverseasHistoryInventoryEntity());
                    v.setId(null);
                    BeanUtils.copyProperties(v, inventory);
                    inventory.setBillDate(calculationDate);
                    inventory.setId(inventory.getId());
                    return inventory;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(entities);
    }

    @Override
    public PagingVO<OverseasHistoryInventoryDTO.ListDTO> paging(PagingDTO<OverseasHistoryInventoryDTO.PagingParamDTO> dto) {
        OverseasHistoryInventoryDTO.PagingParamDTO params = dto.getParams();
        Page<OverseasHistoryInventoryDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasHistoryInventoryDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())){
            new PagingVO<>(pageData);
        }
        //填充分页数据
        filList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void filList(List<OverseasHistoryInventoryDTO.ListDTO> list) {
        // 查询库存映射关系
        List<String> plaformSkuNoList = list.stream().map(OverseasHistoryInventoryDTO.ListDTO::getPlatformSku).distinct().collect(Collectors.toList());
        //  查询仓库ID
        List<OverseasProviderDTO.ListWithWarehouseDTO> listWithWarehouseDTOS = overseasProviderFeign.listAllMatch();
        //获取库存sku信息
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatformSkuNoList(plaformSkuNoList);
        paramDTO.setIsExpire(false);
        List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);

        // 属性赋值
        for(OverseasHistoryInventoryDTO.ListDTO data : list) {
            ListingInfoWithSkuMappingDTO view = listingedInfoWithSkuMappingList.stream()
                    // 匹配关系
                    .filter(e -> this.checkMatch(e, data, listWithWarehouseDTOS))
                    .findFirst()
                    .orElse(new ListingInfoWithSkuMappingDTO());
            data.setSkuId(view.getProductSkuId());
            data.setSkuNo(view.getProductSkuNo());
            data.setPlatformSkuName(view.getPlatformSkuName());
        }

        //查询产品信息
        List<String> skuIdList = list.stream()
                .map(OverseasHistoryInventoryDTO.ListDTO::getSkuId)
                .distinct()
                .collect(Collectors.toList());

        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        for (OverseasHistoryInventoryDTO.ListDTO data : list) {
            if (StringUtils.isNotBlank(data.getSkuId())){
                SkuVO skuVO = skuVOMap.get(data.getSkuId());
                if (null != skuVO){
                    data.setProductName(skuVO.getSkuName());
                }
            }
        }
    }

    private boolean checkMatch(ListingInfoWithSkuMappingDTO mappingDTO, OverseasHistoryInventoryDTO.ListDTO data, List<OverseasProviderDTO.ListWithWarehouseDTO> warehouseDTOList) {
        // 不分仓库
        if (mappingDTO.getPlatformSkuNo().equalsIgnoreCase(data.getPlatformSku()) && mappingDTO.getHasMappingAll()){
            return true;
        }

        if (CollectionUtils.isEmpty(warehouseDTOList)){
            return false;
        }
        OverseasProviderDTO.ListWithWarehouseDTO warehouseDTO = warehouseDTOList.stream()
                .filter(e -> e.getCode().equalsIgnoreCase(data.getDictPlatform()) && e.getPlatformWarehouseCode().equalsIgnoreCase(data.getWarehouseCode()))
                .findFirst()
                .orElse(null);
        if (null == warehouseDTO){
            return false;
        }
        String warehouseId = warehouseDTO.getWarehouseId();

        return mappingDTO.getDictPlatform().equalsIgnoreCase(data.getDictPlatform())
                && mappingDTO.getPlatformSkuNo().equalsIgnoreCase(data.getPlatformSku())
                && mappingDTO.getWarehouseId().equalsIgnoreCase(warehouseId);
    }

    @Override
    public void exportList(OverseasHistoryInventoryDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("海外仓每日库存", FileTaskEventEnum.EXPORT_MRP_OVERSEAS_INVENTORY.getCode(),dto);
    }
}
