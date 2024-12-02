package com.erp.server.tms.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import com.erp.model.tms.enums.SmallBagCostAllocationReportStatusEnum;
import com.erp.server.tms.mapper.SmallBagCostAllocationMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.SmallBagCostAllocationService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 小包费用分摊 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-02
 */
@Slf4j
@Service
public class SmallBagCostAllocationServiceImpl extends SuperServiceImpl<SmallBagCostAllocationMapper, SmallBagCostAllocationEntity> implements SmallBagCostAllocationService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SmallBagCostAllocationDTO.AddDTO addDTO) {
        SmallBagCostAllocationEntity smallBagCostAllocationEntity = new SmallBagCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, smallBagCostAllocationEntity);

        // 数据处理
        handleData(smallBagCostAllocationEntity);

        log.info("开始新增小包费用分摊");
        boolean save = super.save(smallBagCostAllocationEntity);
        if(!save) {
            throw new ServiceException("小包费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "小包费用分摊" , smallBagCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, smallBagCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(smallBagCostAllocationEntity.getId(), smallBagCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SmallBagCostAllocationDTO.UpdateDTO updateDTO) {
        SmallBagCostAllocationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "小包费用分摊"));
        SmallBagCostAllocationEntity smallBagCostAllocationEntity =  BeanMapperUtils.map(SmallBagCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(smallBagCostAllocationEntity);
        log.info("编辑 开始修改小包费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(smallBagCostAllocationEntity);
        if(!save) {
            throw new ServiceException("小包费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录小包费用分摊日志数据，id：【{}】", smallBagCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), smallBagCostAllocationEntity.getId(), "小包费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, smallBagCostAllocationEntity, null, smallBagCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SmallBagCostAllocationEntity smallBagCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<TabListDTO> tabList(PermissionsDTO dto) {
		List<SmallBagCostAllocationDTO.TabListDTO> resultList = new ArrayList<>();
		SmallBagCostAllocationReportStatusEnum[] values = SmallBagCostAllocationReportStatusEnum.values();
        Integer allCount = 0;
        for (SmallBagCostAllocationReportStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            SmallBagCostAllocationDTO.TabListDTO resultDTO = new SmallBagCostAllocationDTO.TabListDTO();
            Integer count = 0;
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(statusEnum.getCode());
            resultDTO.setTabFlagName(statusEnum.getName());
            resultList.add(resultDTO);
            allCount = allCount + resultDTO.getCount();
        }
        
        SmallBagCostAllocationDTO.TabListDTO allTab = new SmallBagCostAllocationDTO.TabListDTO();
        allTab.setCount(allCount);
        allTab.setTabFlag(SearchType.ALL);
        allTab.setTabFlagName("全部");
        resultList.add(allTab);
        
        return resultList;
	}

	@Override
	public PagingVO<ListDTO> paging(PagingDTO<PagingParamDTO> dto) {
		return null;
	}

	@Override
	public BatchResultDTO updateReportStatus(String id, String reportDate, String reportStatus) {
		return null;
	}

	@Override
	public BatchResultDTO reAllocation(String id) {
		return null;
	}

	@Override
	public BatchResultDTO pushBigTable(String id) {
		return null;
	}
}
