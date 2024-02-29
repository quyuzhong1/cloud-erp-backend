package com.erp.server.tms.service.impl;


import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.LogisticsChannelConstraintDTO;
import com.erp.model.tms.entity.LogisticsChannelConstraintEntity;
import com.erp.server.tms.mapper.LogisticsChannelConstraintMapper;
import com.erp.server.tms.service.LogisticsChannelConstraintService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流渠道规则约束 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-02-29
 */
@Slf4j
@Service
public class LogisticsChannelConstraintServiceImpl extends SuperServiceImpl<LogisticsChannelConstraintMapper, LogisticsChannelConstraintEntity> implements LogisticsChannelConstraintService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public  List<BatchResultDTO> addAndUpdate(LogisticsChannelConstraintDTO.AddOrUpdateDTO dto) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //当前数据库数据
        List<LogisticsChannelConstraintEntity> list = this.lambdaQuery().eq(LogisticsChannelConstraintEntity :: getChannelId,dto.getChannelId()).list();
        //前端传过来的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> commonDTOList = dto.getCommonDTOList();
        //修改的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> updateList = commonDTOList.stream().filter(v-> StringUtils.isNotBlank(v.getId())).collect(Collectors.toList());
        for(LogisticsChannelConstraintDTO.CommonDTO updateDto : updateList){
            LogisticsChannelConstraintEntity nowEntity = list.stream().filter(v->v.getId().equals(updateDto.getId())).findFirst().orElse(null);
            if(Objects.isNull(nowEntity)){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"获取不到要更新的数据"));
            }
            //判断是否有变化，无变化则忽略
            if(!judgeHasChange(updateDto,nowEntity)){
                continue;
            }
        }
        //新增的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> addList = commonDTOList.stream().filter(v-> StringUtils.isBlank(v.getId())).collect(Collectors.toList());
        //删除的数据
        return resultDTOList;
    }

    private boolean judgeHasChange(LogisticsChannelConstraintDTO.CommonDTO updateDto, LogisticsChannelConstraintEntity nowEntity) {
        return false;
    }

    @Override
    public List<LogisticsChannelConstraintDTO.ListDTO> getList(String channelId) {
        List<LogisticsChannelConstraintEntity> list = this.lambdaQuery().eq(LogisticsChannelConstraintEntity :: getChannelId,channelId).list();
        return BeanMapper.copyList(list, LogisticsChannelConstraintDTO.ListDTO.class);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsChannelConstraintEntity logisticsChannelConstraintEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
