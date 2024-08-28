package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.excel.ExportOverseasInventoryExcelDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.OverseasInventoryMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OverseasInventoryService;
import com.erp.server.wms.service.OverseasProviderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓库存 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasInventoryServiceImpl extends SuperServiceImpl<OverseasInventoryMapper, OverseasInventoryEntity> implements OverseasInventoryService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private OverseasProviderService overseasProviderService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasInventoryDTO.AddDTO addDTO) {
        OverseasInventoryEntity overseasInventoryEntity = new OverseasInventoryEntity();
        BeanMapperUtils.copy(addDTO, overseasInventoryEntity);

        // 数据处理
        handleData(overseasInventoryEntity);

        log.info("开始新增海外仓库存");
        boolean save = super.save(overseasInventoryEntity);
        if(!save) {
            throw new ServiceException("海外仓库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "海外仓库存" , overseasInventoryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasInventoryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasInventoryEntity.getId(), overseasInventoryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasInventoryDTO.UpdateDTO updateDTO) {
        OverseasInventoryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓库存"));
        OverseasInventoryEntity overseasInventoryEntity =  BeanMapperUtils.map(OverseasInventoryEntity.class, updateDTO);

        // 数据处理
        handleData(overseasInventoryEntity);
        log.info("编辑 开始修改海外仓库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasInventoryEntity);
        if(!save) {
            throw new ServiceException("海外仓库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录海外仓库存日志数据，id：【{}】", overseasInventoryEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), overseasInventoryEntity.getId(), "海外仓库存");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasInventoryEntity, null, overseasInventoryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasInventoryEntity overseasInventoryEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public PagingVO<OverseasInventoryDTO.ListDTO> paging(PagingDTO<OverseasInventoryDTO.PagingParamDTO> dto) {
        OverseasInventoryDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        // 查询关联仓库ID
        if (CollectionUtils.isNotEmpty(dto.getParams().getWarehouseIdList())){
            List<OverseasProviderDTO.WarehouseDTO> warehouseDTOList = overseasProviderService.listProviderWarehouseByIds(dto.getParams().getWarehouseIdList());
            if (CollectionUtils.isEmpty(warehouseDTOList)){
                return new PagingVO<>(new Page<>());
            }
            List<String> codeList = warehouseDTOList.stream().map(OverseasProviderDTO.WarehouseDTO::getPlatformWarehouseCode).distinct().collect(Collectors.toList());
            params.setPlatformWarehouseCodeList(codeList);
        }
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasInventoryDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())){
            new PagingVO<>(pageData);
        }
        //填充分页数据
        filList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void filList(List<OverseasInventoryDTO.ListDTO> list) {
        // 查询库存映射关系
        List<String> plaformSkuNoList = list.stream().map(OverseasInventoryDTO.ListDTO::getPlatformSku).distinct().collect(Collectors.toList());
        //  查询仓库ID
        List<OverseasProviderDTO.ListWithWarehouseDTO> listWithWarehouseDTOS = overseasProviderService.listAllMatch();

        //获取库存sku信息
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatformSkuNoList(plaformSkuNoList);
        paramDTO.setIsExpire(false);
        List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);

        // 属性赋值
        for(OverseasInventoryDTO.ListDTO data : list) {
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
                .map(OverseasInventoryDTO.ListDTO::getSkuId)
                .distinct()
                .collect(Collectors.toList());

        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        for (OverseasInventoryDTO.ListDTO data : list) {
            if (StringUtils.isNotBlank(data.getSkuId())){
                SkuVO skuVO = skuVOMap.get(data.getSkuId());
                if (null != skuVO){
                    data.setProductName(skuVO.getSkuName());
                }
            }
        }
    }

    private boolean checkMatch(ListingInfoWithSkuMappingDTO mappingDTO, OverseasInventoryDTO.ListDTO data, List<OverseasProviderDTO.ListWithWarehouseDTO> warehouseDTOList) {
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
    public OverseasInventoryDTO.ListTotalDTO queryParamsTotal(OverseasInventoryDTO.PagingParamDTO params) {
        return baseMapper.queryParamsTotal(params);
    }

    @Override
    public Boolean exportExcel(OverseasInventoryDTO.ExportDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<OverseasInventoryDTO.ListDTO> list = baseMapper.listByParams(dto);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<ExportOverseasInventoryExcelDTO> resultList = BeanMapper.copyList(list,ExportOverseasInventoryExcelDTO.class);
        //  导出
        String fileName = "海外仓库数据";
        try {
            ExcelUtil.export(fileName, "海外仓库数据", resultList, ExportOverseasInventoryExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015 + e.getMessage());
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateByPlatform(OverseasInventoryEntity entity) {
        LambdaQueryWrapper<OverseasInventoryEntity> queryWrapper = new LambdaQueryWrapper<OverseasInventoryEntity>()
                .eq(OverseasInventoryEntity::getDictPlatform, entity.getDictPlatform())
                .eq(OverseasInventoryEntity::getWarehouseCode, entity.getWarehouseCode())
                .eq(OverseasInventoryEntity::getPlatformSku, entity.getPlatformSku());
        OverseasInventoryEntity existingEntity = this.getOne(queryWrapper);
        if (existingEntity == null || entity.getDownloadTime().isAfter(existingEntity.getDownloadTime())) {
            return this.saveOrUpdate(entity,queryWrapper);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotMapping(String platform) {
        LambdaQueryWrapper<OverseasInventoryEntity> queryWrapper = new LambdaQueryWrapper<OverseasInventoryEntity>()
                .eq(OverseasInventoryEntity::getDictPlatform, platform)
                .eq(OverseasInventoryEntity::getSkuId, "");

        List<OverseasInventoryEntity> notMappingEntityList = this.list(queryWrapper);

        if (CollectionUtils.isEmpty(notMappingEntityList)) {
            return;
        }
        //  查询仓库ID
        List<OverseasProviderDTO.ListWithWarehouseDTO> overseasWarehouseList= overseasProviderService.listAllMatch();

        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(platform);
        paramDTO.setPlatformSkuNoList(notMappingEntityList.stream().map(OverseasInventoryEntity::getPlatformSku).distinct().collect(Collectors.toList()));
        paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
        paramDTO.setMatchResult(true);
        paramDTO.setIsExpire(false);

        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);
        Map<String, List<ListingInfoWithSkuMappingDTO>> mappingRelationMap = listingedInfoWithSkuMappingList.stream()
                .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuNo));

        List<OverseasInventoryEntity> updateList = notMappingEntityList.stream()
                .filter(entity -> mappingRelationMap.containsKey(entity.getPlatformSku()))
                .peek(entity -> {
                    List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOList = mappingRelationMap.get(entity.getPlatformSku());
                    if (CollectionUtils.isEmpty(listingInfoWithSkuMappingDTOList)){
                        return;
                    }
                    ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingInfoWithSkuMappingDTOList.stream().filter(ListingInfoWithSkuMappingDTO::getHasMappingAll).findFirst().orElse(null);
                    if (null == listingInfoWithSkuMappingDTO){
                        OverseasProviderDTO.ListWithWarehouseDTO warehouseDTO = overseasWarehouseList.stream()
                                .filter(e ->  e.getCode().equalsIgnoreCase(entity.getDictPlatform()) && e.getPlatformWarehouseCode().equalsIgnoreCase(entity.getWarehouseCode()))
                                .findFirst().orElse(null);
                        if (null != warehouseDTO){
                            listingInfoWithSkuMappingDTO = listingedInfoWithSkuMappingList.stream()
                                    .filter(e-> e.getWarehouseId().equalsIgnoreCase(warehouseDTO.getWarehouseId()) &&
                                            e.getDictPlatform().equalsIgnoreCase(entity.getDictPlatform()) &&
                                            e.getPlatformSkuNo().equalsIgnoreCase(entity.getPlatformSku()))
                                    .findFirst()
                                    .orElse(null);
                        }
                    }
                    if (null == listingInfoWithSkuMappingDTO){
                        return;
                    }
                    entity.setPlatformSkuName(listingInfoWithSkuMappingDTO.getPlatformSkuName().trim());
                    entity.setProductName(listingInfoWithSkuMappingDTO.getProductName().trim());
                    entity.setSkuId(listingInfoWithSkuMappingDTO.getProductSkuId().trim());
                    entity.setSkuNo(listingInfoWithSkuMappingDTO.getProductSkuNo().trim());
                })
                .collect(Collectors.toList());

        if (!updateList.isEmpty()) {
            this.updateBatchById(updateList);
        }
    }

}
