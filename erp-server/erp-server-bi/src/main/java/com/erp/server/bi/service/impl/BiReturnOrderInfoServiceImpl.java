package com.erp.server.bi.service.impl;

import static com.alibaba.excel.EasyExcel.read;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoImportExcelDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.enums.ReturnOrderStatusEnum;
import com.erp.server.bi.listener.DmpReturnOrderInfoExcelListener;
import com.erp.server.bi.mapper.BiReturnOrderInfoMapper;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderItemService;
import com.erp.server.bi.service.BiShopInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 退货订单服务
 */
@Service
public class BiReturnOrderInfoServiceImpl extends ServiceImpl<BiReturnOrderInfoMapper, BiReturnOrderInfoEntity>
    implements BiReturnOrderInfoService {

    @Resource
    private BiReturnOrderItemService biReturnOrderItemService;

    @Resource
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;

    @Resource
    private BiShopInfoService biShopInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<DmpReturnOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        DmpReturnOrderInfoSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<DmpReturnOrderInfoDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            pageData.getRecords().forEach(obj -> obj.setStatusName(ReturnOrderStatusEnum.getName(obj.getStatus())));
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public BigDecimal sumRefundAmount(List<String> orderIds, BiFilterDTO dto) {
        return biReturnOrderItemService.sumReturnAmountBySKu(dto);
    }

    @Override
    public void exportExcel(DmpReturnOrderInfoSearchDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<DmpReturnOrderInfoDTO> list = baseMapper.getAllDmpReturnOrderInfo(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj ->obj.setStatusName(ReturnOrderStatusEnum.getName(obj.getStatus())));
        //导出销售数据
        List<DmpReturnOrderInfoExcelDTO> excelList = BeanMapperUtils.copyList(DmpReturnOrderInfoExcelDTO.class, list);
        String fileName = biOrderInfoService.getFileName("退货数据导出");
        ExcelUtil.export(fileName, "退货数据导出", excelList, DmpReturnOrderInfoExcelDTO.class, response);
    }

    @Override
    public BiReturnOrderInfoEntity getByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<BiReturnOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiReturnOrderInfoEntity::getReturnCode,returnOrderId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        //系统中已存在的退货订单
        List<BiReturnOrderInfoEntity> returnOrderList = this.list();

        DmpReturnOrderInfoExcelListener excelListenerUtil = new DmpReturnOrderInfoExcelListener(returnOrderList, biOrderInfoService, biReturnOrderInfoService, biShopInfoService, biReturnOrderItemService,plmTaskFeign);
        try {
            read(excelFile.getInputStream(), DmpReturnOrderInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<DmpReturnOrderInfoImportExcelDTO> list = excelListenerUtil.getDateList();
            if (!list.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/dmpReturnOrderInfo.xlsx";
                String name = "dmpRefundInfo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
                return false;
            }
        } catch (IOException e) {
            throw new ServiceException(ApiError.DEFAULT);
        }
        return true;
    }

    @Override
    public void updateOrderFeeById(String returnOrderId) {
        List<BiReturnOrderItemEntity> dmpReturnOrderItemList = biReturnOrderItemService.listByReturnOrderId(returnOrderId);
        if (CollectionUtils.isNotEmpty(dmpReturnOrderItemList)) {
            //计算明细退货金额合计
            BigDecimal reduce = dmpReturnOrderItemList.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getQuantity()) && ObjectUtils.isNotEmpty(obj.getSellPrice()))
                    .map(obj -> MathUtil.multiply(new BigDecimal(obj.getQuantity()), obj.getSellPrice())).reduce(BigDecimal.ZERO, BigDecimal::add);

            LambdaUpdateWrapper<BiReturnOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(BiReturnOrderInfoEntity::getOrderFee,reduce);
            updateWrapper.eq(BiReturnOrderInfoEntity::getId,returnOrderId);
            this.update(updateWrapper);
        }
    }

    @Override
    public PagingVO<DmpReturnOrderInfoExcelDTO> exportBiReturnOrderInfo(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        //查询所有数据
        Page<DmpReturnOrderInfoDTO> list = baseMapper.getAllDmpReturnOrderInfo(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());

        list.getRecords().forEach(obj ->obj.setStatusName(ReturnOrderStatusEnum.getName(obj.getStatus())));
        //导出销售数据
        List<DmpReturnOrderInfoExcelDTO> excelDTOS = BeanMapperUtils.copyList(DmpReturnOrderInfoExcelDTO.class, list.getRecords());

        return new PagingVO<>(excelDTOS, (int) list.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

}




