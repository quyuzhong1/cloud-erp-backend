package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;
import com.erp.model.wms.entity.ReportOrderDemandDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderDemandDetailMapper;
import com.erp.server.wms.service.ReportOrderDemandDetailService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_DEMAND_DETAIL;

/**
 * <p>
 * 订单需求明细报表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderDemandDetailServiceImpl extends SuperServiceImpl<ReportOrderDemandDetailMapper, ReportOrderDemandDetailEntity> implements ReportOrderDemandDetailService {
    @Resource
    private ReportOrderDemandDetailMapper baseMapper;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Override
    public Boolean batchAddOrUpdate(List<ReportOrderDemandDetailDTO.AddDTO> addOrUpdateList) {
        List<ReportOrderDemandDetailEntity> list =  BeanUtil.copyToList(addOrUpdateList,ReportOrderDemandDetailEntity.class);
        //删除原数据
        deleteAll();
        List<ReportOrderDemandDetailEntity> resultList = handleData(list);
        //无数据则返回
        if (CollUtil.isEmpty(resultList)) {
            return Boolean.TRUE;
        }
        //新增或修改有变更数据
        boolean save = super.saveOrUpdateBatch(resultList);
        if(!save) {
            throw new ServiceException("订单报表信息保存失败");
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ReportOrderDemandDetailDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ReportOrderDemandDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean exportExcel(ReportOrderDemandDetailDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("订单需求明细", EXPORT_WMS_REPORT_ORDER_DEMAND_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ReportOrderDemandDetailDTO.ListDTO> listReportOrderDemandDetail(PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> pagingParamDTO) {
        PagingVO<ReportOrderDemandDetailDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        return resultList;
    }

    @Override
    public ReportOrderDemandDetailDTO.ViewBomQtyDTO viewBomQty(String id) {
        ReportOrderDemandDetailDTO.ViewBomQtyDTO  viewBomQtyDTO = new ReportOrderDemandDetailDTO.ViewBomQtyDTO();
        //查询数据
        ReportOrderDemandDetailEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("订单需求明细未找到");
        }
        List<ReportOrderDemandDetailEntity> reportOrderDemandDetailList = listBySourceDetailId(entity.getSourceDetailId());
        if (CollUtil.isEmpty(reportOrderDemandDetailList)) {
            throw new ServiceException("订单需求明细未找到");
        }

        //用量初始化
        Integer quantity = MathUtil.ONE;
        //bom子级需求量
        if (entity.getIsSplit() && ObjectUtil.isNotEmpty(entity.getBomJson())) {
            List<ReportOrderDemandDetailDTO.BomJsonDTO> bomJsonList = BeanUtil.copyToList(entity.getBomJson(), ReportOrderDemandDetailDTO.BomJsonDTO.class);

            List<ReportOrderDemandDetailDTO.BomDTO> bomList = new ArrayList<>();
            for (ReportOrderDemandDetailDTO.BomJsonDTO bomJsonDTO : bomJsonList) {

               ReportOrderDemandDetailEntity reportOrderDemandDetailEntity = reportOrderDemandDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), bomJsonDTO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(reportOrderDemandDetailEntity)) {
                    throw new ServiceException(CharSequenceUtil.format("销售订单【{}】、SKU【{}】未找到",entity.getSourceCode(),bomJsonDTO.getSkuNo()));
                }
                ReportOrderDemandDetailDTO.BomDTO bomDTO = new ReportOrderDemandDetailDTO.BomDTO();
                bomDTO.setChildSkuId(bomJsonDTO.getSkuId());
                bomDTO.setChildSkuNo(bomJsonDTO.getSkuNo());
                bomDTO.setQuantity(bomJsonDTO.getQuantity());
                bomDTO.setQty(reportOrderDemandDetailEntity.getQty());
                bomList.add(bomDTO);
           }
           viewBomQtyDTO.setParentSkuId(bomJsonList.get(0).getParentSkuId());
           viewBomQtyDTO.setParentSkuNo(bomJsonList.get(0).getParentSkuNo());
           viewBomQtyDTO.setBomList(bomList);
           //本条数据子级SKU用量
           quantity = bomList.stream().filter(obj -> CharSequenceUtil.equals(obj.getChildSkuId(), entity.getSkuId())).map(ReportOrderDemandDetailDTO.BomDTO::getQuantity).findFirst().orElse(MathUtil.ZERO);
        }
        //父级需求量
        ReportOrderDemandDetailDTO.ParentQtyDTO parentQtyDTO = new ReportOrderDemandDetailDTO.ParentQtyDTO();
        parentQtyDTO.setQty(entity.getQty() / quantity);
        parentQtyDTO.setOrderQty(entity.getOrderQty() / quantity);
        parentQtyDTO.setDeliveryNoticeQty(entity.getDeliveryNoticeQty() / quantity);
        parentQtyDTO.setFrozenQty(entity.getFrozenQty() / quantity);
        viewBomQtyDTO.setParentQtyDTO(parentQtyDTO);
        return viewBomQtyDTO;
    }

    /**
     * 根据来源明细id查询
     * @author will
     * @date 2024/9/26 16:17
     * @param sourceDetailId
     * @return List<ReportOrderDemandDetailEntity>
     */
    private List<ReportOrderDemandDetailEntity> listBySourceDetailId(String sourceDetailId) {
       return lambdaQuery().eq(ReportOrderDemandDetailEntity::getSourceDetailId,sourceDetailId)
                .list();
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
     * 数据格式化
     * @author will
     * @date 2024/9/27 10:51
     * @param list
     */
    private List<ReportOrderDemandDetailEntity>  handleData(List<ReportOrderDemandDetailEntity> list) {
        List<ReportOrderDemandDetailEntity> resultList = new ArrayList<>();
        if (CollUtil.isEmpty(list)) {
            return resultList;
        }
        //SKU
        List<String> skuIdList = list.stream().map(ReportOrderDemandDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //实体仓
        List<String> warehouseIdList = list.stream().map(ReportOrderDemandDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        //虚拟仓
        List<String> virtualWarehouseIdList = list.stream().map(ReportOrderDemandDetailEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        for (ReportOrderDemandDetailEntity entity : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                entity.setSkuNo(productDetailEntity.getSkuNo());
                entity.setProductName(productDetailEntity.getName());
            }
            //仓库
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            entity.setWarehouseName(warehouseName);

            //虚拟仓
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            entity.setVirtualWarehouseName(virtualWarehouseName);

            //需求数量为0则不新增
            if (MathUtil.compareTo(entity.getQty(),MathUtil.ZERO) <= MathUtil.ZERO) {
                continue;
            }
            resultList.add(entity);
        }
        return resultList;
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/9/25 14:31
     * @param list
     */
    private void fillPageData (List<ReportOrderDemandDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (ReportOrderDemandDetailDTO.ListDTO listDTO : list) {
            //订单类型
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));

            String statusName = !CharSequenceUtil.equals(SourceTypeEnum.SO_INFO.getCode(), listDTO.getSourceType()) ?
                    CharSequenceUtil.equals(SourceTypeEnum.SO_B2C.getCode(), listDTO.getSourceType()) ? SoB2cBillStatusEnum.getName(listDTO.getStatus()) : RequisitionApplicationStatusEnum.getName(listDTO.getStatus())
                    : DeliveryStatusEnum.getName(listDTO.getStatus());
            //订单状态
            if (CharSequenceUtil.equals(SourceTypeEnum.REQUISITION_APPLICATION.getCode(), listDTO.getSourceType())) {
                listDTO.setStatusName(statusName);
            } else {
                listDTO.setStatusName(CharSequenceUtil.format("{}-{}", ApproveStatusEnum.getName(listDTO.getApproveStatus()),statusName));
            }
        }
    }
}
