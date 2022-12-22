package com.erp.server.bi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.MonthEnum;
import com.common.core.utils.StrUtils;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiTargetManagementShowDTO;
import com.erp.model.dmp.entity.BiTargetManagementEntity;
import com.erp.server.bi.mapper.BiTargetManagementMapper;
import com.erp.server.bi.service.BiTargetManagementService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/21 17:35
 */
@Service
public class BiTargetManagementServiceImpl extends ServiceImpl<BiTargetManagementMapper, BiTargetManagementEntity> implements BiTargetManagementService {
    @Override
    public PagingVO<BiTargetManagementShowDTO> paging(PagingDTO<AdvanceSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        AdvanceSearchDTO params = dto.getParams();
        IPage<BiTargetManagementShowDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public List<BiTargetManagementEntity> getSales(LocalDateTime start, LocalDateTime end, String param) {
        // 计算月列表字段
        int startMonth = start.getMonthValue();
        int endMonth = end.getMonthValue();
        List<String> monthList = MonthEnum.getMonthList(startMonth, endMonth);
        if(CollectionUtils.isEmpty(monthList)){
            return new ArrayList<>();
        }
        QueryWrapper<BiTargetManagementEntity> qw = new QueryWrapper<>();
        String addStr = monthList.stream().collect(Collectors.joining(" + ")) + " as january";
        qw.select("id","platform_name","category","sale_type","product_type","product_position",
                "product_no","product_name","per_customer_transaction",addStr);
        qw.last(StrUtil.isNotBlank(param), param);
        List<BiTargetManagementEntity> entityList = baseMapper.selectList(qw);
        return entityList;
    }
}
