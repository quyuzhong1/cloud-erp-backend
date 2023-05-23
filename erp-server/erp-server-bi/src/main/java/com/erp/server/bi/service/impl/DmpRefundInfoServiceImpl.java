package com.erp.server.bi.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpRefundInfoDTO;
import com.erp.model.dmp.dto.DmpRefundInfoExcelDTO;
import com.erp.model.dmp.dto.DmpRefundInfoImportExcelDTO;
import com.erp.model.dmp.dto.DmpRefundInfoSearchDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.enums.RefundStatusEnum;
import com.erp.server.bi.listener.DmpRefundInfoExcelListener;
import com.erp.server.bi.mapper.DmpRefundInfoMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpRefundInfoService;
import com.erp.server.bi.service.DmpRefundItemService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 退款列表服务类
 */
@Service
public class DmpRefundInfoServiceImpl extends ServiceImpl<DmpRefundInfoMapper, DmpRefundInfoEntity>
    implements DmpRefundInfoService {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private DmpRefundItemService dmpRefundItemService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public PagingVO<DmpRefundInfoDTO> paging(PagingDTO<DmpRefundInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpRefundInfoSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<DmpRefundInfoDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            pageData.getRecords().forEach(obj -> obj.setRefundStatusName(RefundStatusEnum.getName(obj.getRefundStatus())));
        }
        return new PagingVO(pageData);
    }

    @Override
    public void exportExcel(DmpRefundInfoSearchDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<DmpRefundInfoDTO> list = baseMapper.getAllRefundInfo(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj ->obj.setRefundStatusName(RefundStatusEnum.getName(obj.getRefundStatus())));
        //导出销售数据
        List<DmpRefundInfoExcelDTO> excelList = BeanMapperUtils.copyList(DmpRefundInfoExcelDTO.class, list);
        String fileName = dmpOrderInfoService.getFileName("退款数据导出");
        ExcelUtil.export(fileName, "退款数据导出", excelList, DmpRefundInfoExcelDTO.class, response);
        return;
    }

    @Override
    public DmpRefundInfoEntity getByRefundId(String refundId) {
        LambdaQueryWrapper<DmpRefundInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpRefundInfoEntity::getRefundId,refundId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        //系统中已存在的退款订单
        List<DmpRefundInfoEntity> refundList = this.list();

        DmpRefundInfoExcelListener excelListenerUtil = new DmpRefundInfoExcelListener(importType,refundList,dmpOrderInfoService, dmpRefundInfoService, dmpShopInfoService,dmpRefundItemService,plmTaskFeign);
        try {
            EasyExcel.read(excelFile.getInputStream(), DmpRefundInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<DmpRefundInfoImportExcelDTO> list = excelListenerUtil.getDateList();
            if (list.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/dmpRefundInfo.xlsx";
                String name = "dmpRefundInfo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
                return false;
            }
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }
        return  true;
    }

}




