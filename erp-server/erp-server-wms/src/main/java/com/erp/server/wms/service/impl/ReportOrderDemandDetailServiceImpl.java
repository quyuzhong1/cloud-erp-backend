package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;
import com.erp.model.wms.entity.ReportOrderDemandDetailEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderDemandDetailMapper;
import com.erp.server.wms.service.ReportOrderDemandDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    @Autowired
    private ReportOrderDemandDetailMapper baseMapper;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<ReportOrderDemandDetailDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDetailDTO.PagingParamDTO> pagingDTO) {
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
        if (CollectionUtil.isEmpty(reportOrderDemandDetailList)) {
            throw new ServiceException("订单需求明细未找到");
        }

        //用量初始化
        Integer quantity = MathUtil.ONE;
        //bom子级需求量
        if (entity.getIsSplit() && ObjectUtil.isNotEmpty(entity.getBomJson())) {
            List<ReportOrderDemandDetailDTO.BomDTO> bomList = BeanUtil.copyToList(entity.getBomJson(), ReportOrderDemandDetailDTO.BomDTO.class);
           for (ReportOrderDemandDetailDTO.BomDTO bomDTO : bomList) {
               ReportOrderDemandDetailEntity reportOrderDemandDetailEntity = reportOrderDemandDetailList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), bomDTO.getChildSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(reportOrderDemandDetailEntity)) {
                    throw new ServiceException(StrUtil.format("销售订单【{}】、SKU【{}】未找到",entity.getSourceCode(),bomDTO.getChildSkuNo()));
                }
               bomDTO.setQty(reportOrderDemandDetailEntity.getQty());
           }
           viewBomQtyDTO.setBomList(bomList);
           //本条数据子级SKU用量
           quantity = bomList.stream().filter(obj -> StrUtil.equals(obj.getChildSkuId(), entity.getSkuId())).map(ReportOrderDemandDetailDTO.BomDTO::getQuantity).findFirst().orElse(MathUtil.ZERO);
        }
        //父级需求量
        ReportOrderDemandDetailDTO.ParentQtyDTO parentQtyDTO = new ReportOrderDemandDetailDTO.ParentQtyDTO();
        parentQtyDTO.setQty(entity.getQty() / quantity);
        parentQtyDTO.setOrderQty(entity.getOrderQty() / quantity);
        parentQtyDTO.setDeliveryNoticeQty(entity.getDeliveryNoticeQty() / quantity);
        parentQtyDTO.setFrozenQty(entity.getFrozenQty() / quantity);
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
     * 分页数据处理
     * @author will
     * @date 2024/9/25 14:31
     * @param list
     */
    private void fillPageData (List<ReportOrderDemandDetailDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (ReportOrderDemandDetailDTO.ListDTO listDTO : list) {
            //订单类型
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));

            String statusName = !StrUtil.equals(SourceTypeEnum.SO_INFO.getCode(), listDTO.getSourceType()) ?
                    StrUtil.equals(SourceTypeEnum.SO_B2C.getCode(), listDTO.getSourceType()) ? SoB2cBillStatusEnum.getName(listDTO.getStatus()) : RequisitionApplicationStatusEnum.getName(listDTO.getStatus())
                    : DeliveryStatusEnum.getName(listDTO.getStatus());
            //订单状态
            listDTO.setStatusName(StrUtil.format("{}-{}", ApproveStatusEnum.getName(listDTO.getApproveStatus()),statusName));
        }
    }
}
