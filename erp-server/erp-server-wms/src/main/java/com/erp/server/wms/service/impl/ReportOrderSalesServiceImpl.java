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
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
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
    public BaseResultDTO.AddDTO add(ReportOrderSalesDTO.AddDTO addDTO) {
        ReportOrderSalesEntity reportOrderSalesEntity = new ReportOrderSalesEntity();
        BeanMapperUtils.copy(addDTO, reportOrderSalesEntity);

        // 数据处理
        handleData(reportOrderSalesEntity);

        log.info("开始新增订单销量单");
        boolean save = super.save(reportOrderSalesEntity);
        if(!save) {
            throw new ServiceException("订单销量单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单销量单" , reportOrderSalesEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, reportOrderSalesEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(reportOrderSalesEntity.getId(), reportOrderSalesEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportOrderSalesDTO.UpdateDTO updateDTO) {
        ReportOrderSalesEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "订单销量单"));
        ReportOrderSalesEntity reportOrderSalesEntity =  BeanMapperUtils.map(ReportOrderSalesEntity.class, updateDTO);

        // 数据处理
        handleData(reportOrderSalesEntity);
        log.info("编辑 开始修改订单销量单数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportOrderSalesEntity);
        if(!save) {
            throw new ServiceException("订单销量单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录订单销量单日志数据，id：【{}】", reportOrderSalesEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportOrderSalesEntity.getId(), "订单销量单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, reportOrderSalesEntity, null, reportOrderSalesEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderSalesDTO.ListDTO> paging(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean exportExcel(ReportOrderSalesDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售看板", EXPORT_WMS_REPORT_ORDER_SALES.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<ReportOrderSalesDTO.ListDTO> listReportOrderSales(ReportOrderSalesDTO.PagingParamDTO dto) {
        PagingDTO<ReportOrderSalesDTO.PagingParamDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<ReportOrderSalesDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        List<ReportOrderSalesDTO.ListDTO> list = (List<ReportOrderSalesDTO.ListDTO>) resultList.getList();
        return list;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ReportOrderSalesEntity reportOrderSalesEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
