package com.erp.server.tms.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.ObjectUtil;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.erp.model.tms.enums.TransferDeclareCostAllocationReportStatusEnum;
import com.erp.server.tms.mapper.TransferDeclareCostAllocationMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareCostAllocationService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中转费用分摊 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
@Slf4j
@Service
public class TransferDeclareCostAllocationServiceImpl extends SuperServiceImpl<TransferDeclareCostAllocationMapper, TransferDeclareCostAllocationEntity> implements TransferDeclareCostAllocationService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDTO.AddDTO addDTO) {
        TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity = new TransferDeclareCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareCostAllocationEntity);

        // 数据处理
        handleData(transferDeclareCostAllocationEntity);

        log.info("开始新增中转费用分摊");
        boolean save = super.save(transferDeclareCostAllocationEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转费用分摊" , transferDeclareCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareCostAllocationEntity.getId(), transferDeclareCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareCostAllocationDTO.UpdateDTO updateDTO) {
        TransferDeclareCostAllocationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转费用分摊"));
        TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity =  BeanMapperUtils.map(TransferDeclareCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareCostAllocationEntity);
        log.info("编辑 开始修改中转费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareCostAllocationEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转费用分摊日志数据，id：【{}】", transferDeclareCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareCostAllocationEntity.getId(), "中转费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareCostAllocationEntity, null, transferDeclareCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<TabListDTO> tabList(PermissionsDTO dto) {
		List<TransferDeclareCostAllocationDTO.TabListDTO> resultList = new ArrayList<>();
		List<TransferDeclareCostAllocationEntity> list = list();
		TransferDeclareCostAllocationReportStatusEnum[] values = TransferDeclareCostAllocationReportStatusEnum.values();
        for (TransferDeclareCostAllocationReportStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            TransferDeclareCostAllocationDTO.TabListDTO resultDTO = new TransferDeclareCostAllocationDTO.TabListDTO();
            String code = statusEnum.getCode();
			Integer count = (int)list.stream().filter(l -> l.getReportStatus().equals(code)).count();
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(code);
            resultDTO.setTabFlagName(statusEnum.getName());
            resultList.add(resultDTO);
        }
        
        return resultList;
	}
	
	

	@Override
	public PagingVO<ListDTO> paging(PagingDTO<PagingParamDTO> dto) {
		PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ListDTO> pageData = this.baseMapper.paging(query, params);
        List<ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        handleDataPaging(records);
        return new PagingVO(pageData);
	}
	
	private void handleDataPaging(List<ListDTO> records) {
		
	}

	@Override
	public BatchResultDTO updateReportStatus(String id, String reportDate, String reportStatus) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BatchResultDTO reAllocation(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BatchResultDTO delete(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BatchResultDTO pushBigTable(String id) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public Boolean updateBigTableStatus(String id, String bigTableStatus) {
        TransferDeclareCostAllocationEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("单据不存在!");
        }
        return this.lambdaUpdate()
                .set(TransferDeclareCostAllocationEntity::getBigTableStatus, bigTableStatus)
                .eq(TransferDeclareCostAllocationEntity::getId, id)
                .update();
    }
}
