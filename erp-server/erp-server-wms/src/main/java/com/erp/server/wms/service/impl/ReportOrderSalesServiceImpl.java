package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderSalesMapper;
import com.erp.server.wms.service.ReportOrderSalesService;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_SALES;

/**
 * <p>
 * 订单销量表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderSalesServiceImpl extends SuperServiceImpl<ReportOrderSalesMapper, ReportOrderSalesEntity> implements ReportOrderSalesService {
    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchAddOrUpdate(List<ReportOrderSalesDTO.AddDTO> addOrUpdateList) {
        List<ReportOrderSalesEntity> list =  BeanMapperUtils.copyList(ReportOrderSalesEntity.class, addOrUpdateList);
        //删除原数据
        deleteAll();
        //数据处理
        handleData(list);
        log.info("开始新增订单销量单");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("订单销量单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderSalesDTO.ListDTO> paging(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ReportOrderSalesDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        handlePage(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean exportExcel(ReportOrderSalesDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售看板", EXPORT_WMS_REPORT_ORDER_SALES.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderSalesDTO.ListDTO> listReportOrderSales(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> pagingParamDTO) {
        PagingVO<ReportOrderSalesDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        return resultList;
    }

    /**
     * 删除所有数据
     * @author will
     * @date 2024/9/27 10:43
     */
    private void deleteAll() {
        baseMapper.deleteAll();
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/9/29 14:37
     * @param list
     */
    private void handlePage (List<ReportOrderSalesDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //sku
        List<String> skuIdList = list.stream().map(ReportOrderSalesDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        //实体仓id
        List<String> warehouseIdList = list.stream().map(ReportOrderSalesDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓id
        List<String> virtualWarehouseIdList = list.stream().map(ReportOrderSalesDTO.ListDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> allocationDataList = virtualWarehouseAllocationDetailService.listAllocationData(skuIdList, warehouseIdList, virtualWarehouseIdList);

        for (ReportOrderSalesDTO.ListDTO listDTO : list) {
            /**
             * 累计分配 = 【新增分货-调入虚拟仓-分配数量】+ 【虚拟仓调拨-调入虚拟仓-调拨数量】-【虚拟仓调拨-调出虚拟仓-调拨数量】-【取消分货-调出虚拟仓-取消数量】
             */
            //新增分货数量
            Integer addQty = allocationDataList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode())
                            && StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                            && StrUtil.equals(obj.getToVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
            ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

            //调入分货数量
            Integer toTransferQty = allocationDataList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())
                            && StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                            && StrUtil.equals(obj.getToVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
            ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

            //调出分货数量
            Integer fromTransferQty = allocationDataList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())
                            && StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                            && StrUtil.equals(obj.getFromVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
            ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

            //取消分货数量
            Integer cancelQty = allocationDataList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.CANCEL.getCode())
                            && StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                            && StrUtil.equals(obj.getFromVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
            ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            //分配数量
            Integer distributionQty = addQty + toTransferQty - fromTransferQty - cancelQty;
            listDTO.setDistributionQty(distributionQty);
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<ReportOrderSalesEntity> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //SKU
        List<String> skuIdList = list.stream().map(ReportOrderSalesEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //实体仓
        List<String> warehouseIdList = list.stream().map(ReportOrderSalesEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        //虚拟仓
        List<String> virtualWarehouseIdList = list.stream().map(ReportOrderSalesEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        for (ReportOrderSalesEntity entity : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                entity.setSkuNo(productDetailEntity.getSkuNo());
                entity.setProductName(productDetailEntity.getName());
            }
            //仓库
            String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            entity.setWarehouseName(warehouseName);

            //虚拟仓
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            entity.setVirtualWarehouseName(virtualWarehouseName);
        }
    }
}
