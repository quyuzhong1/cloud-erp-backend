package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpRefundInfoDTO;
import com.erp.model.bi.dto.DmpRefundInfoExcelDTO;
import com.erp.model.bi.dto.DmpRefundInfoSearchDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.server.bi.enums.RefundStatusEnum;
import com.erp.server.bi.mapper.DmpRefundInfoMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpRefundInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 退款列表服务类
 */
@Service
public class DmpRefundInfoServiceImpl extends ServiceImpl<DmpRefundInfoMapper, DmpRefundInfoEntity>
    implements DmpRefundInfoService {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Override
    public PagingVO<DmpRefundInfoDTO> paging(PagingDTO<DmpRefundInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpRefundInfoSearchDTO params = dto.getParams();
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


}




