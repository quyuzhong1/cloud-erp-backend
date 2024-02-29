package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.LogisticsChannelConstraintDTO;
import com.erp.model.tms.entity.LogisticsChannelConstraintEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.server.tms.mapper.LogisticsChannelConstraintMapper;
import com.erp.server.tms.service.LogisticsChannelConstraintService;
import com.erp.server.tms.service.LogisticsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public  List<BatchResultDTO> addAndUpdate(LogisticsChannelConstraintDTO.AddOrUpdateDTO dto) {
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(dto.getChannelId());
        if(Objects.isNull(channelEntity)){
            throw new ServiceException("获取不到物流渠道信息");
        }
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //当前数据库数据
        List<LogisticsChannelConstraintEntity> list = this.lambdaQuery().eq(LogisticsChannelConstraintEntity :: getChannelId,dto.getChannelId()).list();
        //前端传过来的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> commonDTOList = dto.getCommonDTOList();
        //修改的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> updateList = commonDTOList.stream().filter(v-> StringUtils.isNotBlank(v.getId())).collect(Collectors.toList());
        List<LogisticsChannelConstraintEntity> needUpdateList = new ArrayList<>();
        List<LogisticsChannelConstraintEntity> needAddList = new ArrayList<>();
        for(LogisticsChannelConstraintDTO.CommonDTO updateDto : updateList){
            LogisticsChannelConstraintEntity nowEntity = list.stream().filter(v->v.getId().equals(updateDto.getId())).findFirst().orElse(null);
            if(Objects.isNull(nowEntity)){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"获取不到要更新的数据"));
                continue;
            }
            //判断是否有变化，无变化则忽略
            if(updateDto.equalsEntity(nowEntity)){
                continue;
            }
            //校验数据
            if(!updateDto.isValid()){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"字段至少填写一个且大于0，不能都为空"));
                continue;
            }
            //校验数据
            if(!updateDto.isValidSize()){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"超尺寸填写其中一个，其他字段必须填写完整"));
                continue;
            }
            BeanUtil.copyProperties(updateDto,nowEntity);
            needUpdateList.add(nowEntity);
        }
        //新增的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> addList = commonDTOList.stream().filter(v-> StringUtils.isBlank(v.getId())).collect(Collectors.toList());
        for(LogisticsChannelConstraintDTO.CommonDTO addDTO : addList){
            //判断国家是否存在，不能重复
            LogisticsChannelConstraintEntity sameCountryEntity = list.stream().filter(v->v.getCountry().equals(addDTO.getCountry())).findFirst().orElse(null);
            if(Objects.nonNull(sameCountryEntity)){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"国家配置已存在，不能重复配置"));
                continue;
            }
            if(addList.stream().filter(v->v.getCountry().equals(addDTO.getCountry())).count() > 1){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"存在两条相同国家配置，不能重复配置"));
                continue;
            }
            //校验数据
            if(!addDTO.isValid()){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"字段至少填写一个且大于0，不能都为空"));
                continue;
            }
            //校验数据
            if(!addDTO.isValidSize()){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"超尺寸填写其中一个，其他字段必须填写完整"));
                continue;
            }
            LogisticsChannelConstraintEntity addEntity = new LogisticsChannelConstraintEntity();
            BeanUtil.copyProperties(addDTO,addEntity,"id");
            addEntity.setChannelId(dto.getChannelId());
            needAddList.add(addEntity);
        }
        //删除的数据
        List<LogisticsChannelConstraintEntity> needDeleteList = list.stream().filter(v-> commonDTOList.stream().noneMatch(t->v.getId().equals(t.getId()) || v.getCountry().equals(t.getCountry()))).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(needAddList)){
            this.saveBatch(needAddList);
        }
        if(CollectionUtils.isNotEmpty(needUpdateList)){
            this.updateBatchById(needUpdateList);
        }
        if(CollectionUtils.isNotEmpty(needDeleteList)){
            this.removeByIds(needDeleteList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        }
        return resultDTOList;
    }


    @Override
    public List<LogisticsChannelConstraintDTO.ListDTO> getList(String channelId) {
        List<LogisticsChannelConstraintEntity> list = this.lambdaQuery().eq(LogisticsChannelConstraintEntity :: getChannelId,channelId).list();
        return BeanMapper.copyList(list, LogisticsChannelConstraintDTO.ListDTO.class);
    }
}
