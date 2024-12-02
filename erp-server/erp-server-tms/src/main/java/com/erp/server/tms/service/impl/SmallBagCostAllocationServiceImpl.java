package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import com.erp.server.tms.mapper.SmallBagCostAllocationMapper;
import com.erp.server.tms.service.SmallBagCostAllocationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.TabListDTO;

import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
		return null;
	}

	@Override
	public PagingVO<ListDTO> paging(PagingDTO<PagingParamDTO> dto) {
		return null;
	}
}
