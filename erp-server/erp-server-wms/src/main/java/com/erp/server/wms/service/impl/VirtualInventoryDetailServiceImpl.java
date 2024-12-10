package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.CfgSettingVirtualEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.VirtualInventoryDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 虚拟仓库明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryDetailServiceImpl extends SuperServiceImpl<VirtualInventoryDetailMapper, VirtualInventoryDetailEntity> implements VirtualInventoryDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

    @Autowired
    private CfgSettingService cfgSettingService;

    @Autowired
    private VirtualInventoryHisService virtualInventoryHisService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public VirtualInventoryDetailEntity addOrUpdate(VirtualInventoryDetailDTO.UpdateDTO addOrUpdateDTO) {
        VirtualInventoryDetailEntity entity = new VirtualInventoryDetailEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, entity);

        // 数据处理
        handleData(entity);

        //生成单号
        if (CharSequenceUtil.isBlank(entity.getId())) {
            String batchNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_N);
            entity.setBatchNo(batchNo);
        }
        log.info("开始新增虚拟仓库明细");
        boolean save = super.save(entity);
        if(!save) {
            throw new ServiceException("虚拟仓库明细保存失败");
        }
        return entity;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.ListDTO> paging(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualInventoryAgeDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualInventoryAgeDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("库龄分析", EXPORT_WMS_VIRTUAL_INVENTORY_AGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public VirtualInventoryAgeDTO.ViewDTO view(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.ViewDTO viewDTO = new VirtualInventoryAgeDTO.ViewDTO();
        BeanMapperUtils.copy(dto,viewDTO);
        //产品信息
        ProductDetailEntity productDetailEntity = FeignQuery.getById(ProductDetailEntity.class, dto.getSkuId());
        if (ObjUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        viewDTO.setSkuNo(productDetailEntity.getSkuNo());
        viewDTO.setProductName(productDetailEntity.getName());

        //仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        if (ObjUtil.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        viewDTO.setWarehouseName(warehouseEntity.getName());
        //虚拟仓库
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(dto.getVirtualWarehouseId());
        if (ObjUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException("未找到虚拟仓库");
        }
        viewDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
        viewDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());
        return viewDTO;
    }

    @Override
    public Boolean exportHisInventoryAge(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史库龄 ", EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDTO> pageData = this.baseMapper.hisInventoryAgePaging(dto.page(), dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> pageData = this.baseMapper.hisInventoryAgeDetailPaging(dto.page(), dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史库龄明细", EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> exportHisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> pageData = this.baseMapper.exportHisInventoryAgeDetailPaging(dto.page(), dto.getParams());
        handleInventoryAgeDetail(pageData.getRecords(),dto.getParams());
        return new PagingVO<>(pageData);
    }


    /**
     * 库龄明细导出数据处理
     * @author will
     * @date 2024/12/9 11:30
     * @param list
     * @param dto
     */
    private void handleInventoryAgeDetail (List<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> list,VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        //查询历史平均库龄
        VirtualInventoryHisDTO.ParamDTO paramDTO = new VirtualInventoryHisDTO.ParamDTO();
        paramDTO.setSkuIdList(Collections.singletonList(dto.getSkuId()));
        paramDTO.setWarehouseIdList(Collections.singletonList(dto.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(Collections.singletonList(dto.getVirtualWarehouseId()));
        paramDTO.setEndDate(dto.getDate());
        List<VirtualInventoryHisEntity> virtualInventoryHisList = virtualInventoryHisService.listByParam(paramDTO);

        for (VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO detailDTO : list) {
            //统计日期
            detailDTO.setDate(dto.getDate());
            //平均库龄
            if (CollUtil.isNotEmpty(virtualInventoryHisList)) {
                detailDTO.setAvgInventoryAgeDays(virtualInventoryHisList.get(0).getAvgInventoryAgeDays());
            }
        }
    }

    @Override
    public List<String> getCfgHead() {
        List<String> headList = new ArrayList<>();
        //查询库龄分析配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingVirtualEnum.INVENTORY_AGE_STATISTICS.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return Collections.EMPTY_LIST;
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();
        for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
            String ageDateInterval = "";
            //区间字段
            if (ObjUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                ageDateInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getEndDays());
            } else {
                ageDateInterval = CharSequenceUtil.format("{}~{}天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
            }
            headList.add(ageDateInterval);
        }
        return headList;
    }

    @Override
    public VirtualInventoryAgeDTO.HisInventoryAgeChartDTO getHisInventoryAgeChart(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.HisInventoryAgeChartDTO chartDTO = new VirtualInventoryAgeDTO.HisInventoryAgeChartDTO();

        List<VirtualInventoryAgeDTO.HisInventoryAgeDTO> list = baseMapper.getHisInventoryAgeChart(dto);
        if (CollUtil.isEmpty(list)) {
            return chartDTO;
        }
        List<LocalDate> dateList = list.stream().map(VirtualInventoryAgeDTO.HisInventoryAgeDTO::getDate).collect(Collectors.toList());
        chartDTO.setDateList(dateList);
        List<String> avgInventoryAgeDaysList = list.stream().map(obj -> obj.getAvgInventoryAgeDays().stripTrailingZeros().toPlainString()).collect(Collectors.toList());
        chartDTO.setAvgInventoryAgeList(avgInventoryAgeDaysList);
        return chartDTO;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryDetailEntity virtualInventoryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页列表处理数据
     */
    private void fillPageData(List<VirtualInventoryAgeDTO.ListDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        //查询库龄分析配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingVirtualEnum.INVENTORY_AGE_STATISTICS.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return;
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();

        //SKU
        List<String> skuIdList = detailList.stream().map(VirtualInventoryAgeDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        //仓库
        List<String> warehouseIdList = detailList.stream().map(VirtualInventoryAgeDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓
        List<String> virtualWarehouseId = detailList.stream().map(VirtualInventoryAgeDTO.ListDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());


        //查询库龄历史
        Integer endDays = list.stream().max(Comparator.comparingInt(CfgSettingVirtualValueDTO.InventoryAgeDateTO::getStartDays)).map(CfgSettingVirtualValueDTO.InventoryAgeDateTO::getEndDays).orElse(null);
        LocalDate minStartDate = ObjUtil.isNull(endDays) ? null : LocalDate.now().minusDays(endDays);
        List<VirtualInventoryHisEntity> virtualInventoryHisList = virtualInventoryHisService.listByParam(new VirtualInventoryHisDTO.ParamDTO(skuIdList, warehouseIdList, virtualWarehouseId, minStartDate));

        HashMap<String, VirtualInventoryAgeDTO.VirtualIntervalDTO> map = new HashMap<>();
        for (VirtualInventoryAgeDTO.ListDTO listDTO :detailList) {
            for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
                String ageDateInterval = "";
                //区间字段
                if (ObjUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                    ageDateInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getEndDays());
                } else {
                    ageDateInterval = CharSequenceUtil.format("{}~{}天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
                }
                LocalDate nowDate = LocalDate.now();
                LocalDate endDate = nowDate.minusDays(inventoryAgeDateTO.getStartDays());
                LocalDate startDate = nowDate.minusDays(inventoryAgeDateTO.getEndDays());
                Integer totalQty = virtualInventoryHisList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSkuId(),listDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(),listDTO.getWarehouseId())
                        && CharSequenceUtil.equals(obj.getVirtualWarehouseId(),listDTO.getVirtualWarehouseId())
                        && obj.getDate().isAfter(endDate)
                        && obj.getDate().isBefore(startDate)
                ).map(VirtualInventoryHisEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                //比例
                BigDecimal ratio = MathUtil.compareTo(listDTO.getVirtualQty(),MathUtil.ZERO) == MathUtil.ZERO ? BigDecimal.ZERO : MathUtil.divide(MathUtil.valueOf(totalQty) ,BigDecimal.valueOf(listDTO.getVirtualQty()));
                map.put(ageDateInterval,new VirtualInventoryAgeDTO.VirtualIntervalDTO(totalQty,StrUtil.format("{}%",ratio) ));
                listDTO.setMap(map);
            }
        }
    }
}

