package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.BasicQueryLogisticsProviderEntity;
import com.erp.server.tms.mapper.BasicQueryLogisticsProviderMapper;
import com.erp.server.tms.service.BasicQueryLogisticsProviderService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.BasicQueryLogisticsProviderDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 查询物流商信息表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-03-31
 */
@Slf4j
@Service
public class BasicQueryLogisticsProviderServiceImpl extends SuperServiceImpl<BasicQueryLogisticsProviderMapper, BasicQueryLogisticsProviderEntity> implements BasicQueryLogisticsProviderService {

    @Override
    public List<BasicQueryLogisticsProviderDTO.ListAllVO> listAll(BasicQueryLogisticsProviderDTO.ListAllParamDTO paramDTO) {
        if(StringUtils.isBlank(paramDTO.getLogisticsNameCn())){
            return Lists.newArrayList();
        }
        List<BasicQueryLogisticsProviderEntity> list = this.lambdaQuery()
                .eq(BasicQueryLogisticsProviderEntity::getTrackPlatformType,paramDTO.getTrackPlatformType())
                .like(StrUtil.isNotBlank(paramDTO.getLogisticsNameCn()), BasicQueryLogisticsProviderEntity::getLogisticsNameCn, paramDTO.getLogisticsNameCn())
                .orderByDesc(BasicQueryLogisticsProviderEntity::getCreateTime)
                .list();
        return BeanMapperUtils.copyList(BasicQueryLogisticsProviderDTO.ListAllVO.class, list);
    }
}
