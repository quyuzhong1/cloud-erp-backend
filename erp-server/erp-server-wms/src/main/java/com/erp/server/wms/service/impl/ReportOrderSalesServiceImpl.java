package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.entity.ReportOrderDemandEntity;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderSalesMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.ReportOrderSalesService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchAddOrUpdate(List<ReportOrderSalesDTO.AddDTO> addOrUpdateList) {
        List<ReportOrderSalesEntity> list =  BeanMapperUtils.copyList(ReportOrderSalesEntity.class, addOrUpdateList);
        //删除原数据
        deleteAll();
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
    * 新增修改处理数据
    */
    private void handleData(ReportOrderSalesEntity reportOrderSalesEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
