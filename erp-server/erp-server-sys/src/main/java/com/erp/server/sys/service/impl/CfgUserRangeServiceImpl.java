package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.sys.dto.CfgUserRangeDTO;
import com.erp.model.sys.entity.CfgUserRangeEntity;
import com.erp.model.sys.enums.UserRangeTypeEnum;
import com.erp.server.sys.config.UserRangeProperties;
import com.erp.server.sys.mapper.CfgUserRangeMapper;
import com.erp.server.sys.service.CfgUserRangeService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户区间配置表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-12
 */
@Slf4j
@Service
public class CfgUserRangeServiceImpl extends SuperServiceImpl<CfgUserRangeMapper, CfgUserRangeEntity> implements CfgUserRangeService {

    @Resource
    private UserRangeProperties userRangeProperties;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(CfgUserRangeDTO.SaveDTO userRangeDTO) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        if(StrUtils.isEmpty(userId)) {
            return;
        }

        UserRangeTypeEnum userRangeTypeEnum = UserRangeTypeEnum.getByCode(userRangeDTO.getType());
        List<CfgUserRangeDTO.UserRangeDTO> rangeList = userRangeDTO.getRangeList();
        if(CollUtil.isEmpty(rangeList)) {
            log.info("用户【{}】没有输入区间【{}】", userId, userRangeTypeEnum.getName());
            return;
        }
        List<CfgUserRangeEntity> cfgUserRangeEntityList = Lists.newArrayList();
        for(int i = 0,size = rangeList.size();i < size;i++) {
            CfgUserRangeDTO.UserRangeDTO range = rangeList.get(i);
            // 结束值必须大于开始值
            if(range.getEndValue() <= range.getStartValue()) {
                throw new ServiceException("结束值必须大于开始值");
            }
            // 非第一行当前区间的开始值等于上一行的结束值，即区间不能间断
            if(i > 0) {
                CfgUserRangeDTO.UserRangeDTO lastRange = rangeList.get(i - 1);
                if(range.getStartValue().intValue() != lastRange.getEndValue().intValue()) {
                    throw new ServiceException("开始值必须等于前一个区间的结束值");
                }
            }
            CfgUserRangeEntity cfgUserRangeEntity = new CfgUserRangeEntity();
            cfgUserRangeEntity.setType(userRangeTypeEnum.getCode());
            cfgUserRangeEntity.setStartValue(range.getStartValue());
            cfgUserRangeEntity.setEndValue(range.getEndValue());
            cfgUserRangeEntity.setUserId(userId);
            String name = StrUtil.format(userRangeTypeEnum.getLabel(),range.getStartValue(), range.getEndValue()) + userRangeTypeEnum.getUnitName();
            cfgUserRangeEntity.setName(name);
            cfgUserRangeEntityList.add(cfgUserRangeEntity);
        }
        super.saveBatch(cfgUserRangeEntityList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(CfgUserRangeDTO.SaveDTO userRangeDTO) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        if(StrUtils.isEmpty(userId)) {
            return;
        }

        // 删除当前用户设置的区间值
        super.remove(new QueryWrapper<CfgUserRangeEntity>().eq(CfgUserRangeEntity.FIELD_TYPE, userRangeDTO.getType())
                .eq(CfgUserRangeEntity.USER_ID, userId));

        // 新增
        this.add(userRangeDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(CfgUserRangeDTO.SaveDTO userRangeDTO) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        if(StrUtils.isEmpty(userId)) {
            return;
        }

        UserRangeTypeEnum userRangeTypeEnum = UserRangeTypeEnum.getByCode(userRangeDTO.getType());
        // 判断是否有设置过用户区间
        List<CfgUserRangeEntity> cfgUserRangeList = lambdaQuery().eq(CfgUserRangeEntity::getType, userRangeDTO.getType())
                .eq(CfgUserRangeEntity::getUserId, userId).list();

        if(CollUtil.isEmpty(cfgUserRangeList)) {
            log.info("用户【{}】未设置过用户区间【{}】, 新增区间", userId, userRangeTypeEnum.getName());
            ValidatorUtil.isTrue(CollUtil.isNotEmpty(userRangeDTO.getRangeList()),()->new ServiceException("用户区间不能为空"));
            this.add(userRangeDTO);
        } else {
            log.info("用户【{}】设置过用户区间【{}】, 修改区间", userId, userRangeTypeEnum.getName());
            this.update(userRangeDTO);
        }
    }

    @Override
    public List<CfgUserRangeDTO.UserRangeDataDTO> detail(String type) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        if(StrUtils.isEmpty(userId)) {
            return null;
        }

        UserRangeTypeEnum userRangeTypeEnum = UserRangeTypeEnum.getByCode(type);
        Optional.ofNullable(userRangeTypeEnum).orElseThrow(()->new ServiceException("区间类型错误"));

        List<CfgUserRangeEntity> cfgUserRangeList = lambdaQuery().eq(CfgUserRangeEntity::getType, type)
                .eq(CfgUserRangeEntity::getUserId, userId).list();
        if(CollUtil.isNotEmpty(cfgUserRangeList)) {
            List<CfgUserRangeDTO.UserRangeDataDTO> rangeList = Lists.newArrayListWithExpectedSize(cfgUserRangeList.size());
            cfgUserRangeList = cfgUserRangeList.stream().sorted(Comparator.comparing(CfgUserRangeEntity::getStartValue)).collect(Collectors.toList());
            cfgUserRangeList.stream().forEach(data->{
                CfgUserRangeDTO.UserRangeDataDTO userRangeData = new CfgUserRangeDTO.UserRangeDataDTO();
                userRangeData.setStartValue(data.getStartValue());
                userRangeData.setEndValue(data.getEndValue());
                userRangeData.setName(data.getName());
                rangeList.add(userRangeData);
            });
            return rangeList;
        }
        return null;
    }

    @Override
    public List<CfgUserRangeDTO.UserRangeDataDTO> getUserRanges(String type, Boolean addLast) {
        UserRangeTypeEnum userRangeTypeEnum = UserRangeTypeEnum.getByCode(type);
        String formatName = userRangeTypeEnum.getLabel();
        List<CfgUserRangeDTO.UserRangeDataDTO> rangeList = this.detail(type);
        if(CollUtil.isEmpty(rangeList)) {
            // 获取默认的区间配置
            LinkedHashMap<String, List<CfgUserRangeDTO.UserRangeDataDTO>> rangeMap = userRangeProperties.getRangeMap();
            List configRangeList = rangeMap.get(type);

            if(CollUtil.isNotEmpty(configRangeList)) {
                // 需要赋值展示名称
                rangeList = BeanMapper.copyList(configRangeList, CfgUserRangeDTO.UserRangeDataDTO.class);
                rangeList.stream().forEach(data-> data.setName(StrUtil.format(formatName, data.getStartValue(), data.getEndValue())));
            }
        }
        // 追加最后一个到无穷大的区间
        if(Objects.equals(addLast, Boolean.TRUE) && CollUtil.isNotEmpty(rangeList)) {
            CfgUserRangeDTO.UserRangeDataDTO userRangeDataDTO = rangeList.get(rangeList.size() -1);

            CfgUserRangeDTO.UserRangeDataDTO lastRange = new CfgUserRangeDTO.UserRangeDataDTO();
            lastRange.setStartValue(userRangeDataDTO.getEndValue());
            lastRange.setEndValue(null);
            lastRange.setName(lastRange.getStartValue() + userRangeTypeEnum.getUnitName() + UserRangeTypeEnum.LAST_STR);
            rangeList.add(lastRange);
        }
        return rangeList;
    }

}
