package com.erp.server.bi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.MonthEnum;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiTargetManagementShowDTO;
import com.erp.model.bi.entity.BiTargetManagementEntity;
import com.erp.server.bi.mapper.BiTargetManagementMapper;
import com.erp.server.bi.service.BiTargetManagementService;
import org.apache.commons.collections.CollectionUtils;
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
        params.setParam(dto.getParam());
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
        qw.select("id","platform_name","category","target_type","product_type","product_position",
                "sku_no","product_name","sale_price",addStr);
        qw.eq("year", start.getYear());
        qw.last(StrUtil.isNotBlank(param), param);
        List<BiTargetManagementEntity> entityList = baseMapper.selectList(qw);
        return entityList;
    }

    @Override
    public BiTargetManagementEntity getTargetByExcelData(BiTargetManagementEntity entity) {
        LambdaQueryWrapper<BiTargetManagementEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiTargetManagementEntity::getYear,entity.getYear());
        queryWrapper.eq(BiTargetManagementEntity::getPlatformName,entity.getPlatformName());
        queryWrapper.eq(BiTargetManagementEntity::getCategoryId,entity.getCategoryId());
        queryWrapper.eq(BiTargetManagementEntity::getTargetType,entity.getTargetType());
        if (StringUtils.isNotBlank(entity.getSkuId())) {
            queryWrapper.eq(BiTargetManagementEntity::getSkuId,entity.getSkuId());
        } else {
            queryWrapper.eq(BiTargetManagementEntity::getSpuId,entity.getSpuId());
        }
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public BiTargetManagementEntity getMonthSales(LocalDateTime start, LocalDateTime end, String param, Integer targetType) {
        QueryWrapper<BiTargetManagementEntity> qw = new QueryWrapper<>();
        qw.select("sum(january) as january","sum(february) as february", "sum(march) as march", "sum(april) as april","sum(may) as may",
                "sum(june) as  june","sum(july) as july","sum(august) as august",
                "sum(september) as september","sum(october) as october","sum(november) as november","sum(december) as december");
        qw.eq(null != targetType,"target_type",  targetType);
        qw.eq("year", start.getYear());
        qw.last(StrUtil.isNotBlank(param), param);
        BiTargetManagementEntity entity = baseMapper.selectOne(qw);
        return entity;
    }
}
