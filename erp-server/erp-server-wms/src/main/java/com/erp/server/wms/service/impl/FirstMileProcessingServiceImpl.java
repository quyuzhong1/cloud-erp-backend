package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.FirstMileProcessingMapper;
import com.erp.server.wms.service.FirstMileProcessingService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2C_PROCESSING;

/**
 * <p>
 * 头程虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class FirstMileProcessingServiceImpl extends SuperServiceImpl<FirstMileProcessingMapper, FirstMileProcessingEntity> implements FirstMileProcessingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileProcessingDTO.AddDTO addDTO) {
        FirstMileProcessingEntity firstMileProcessingEntity = new FirstMileProcessingEntity();
        BeanMapperUtils.copy(addDTO, firstMileProcessingEntity);

        // 数据处理
        handleData(firstMileProcessingEntity);

        log.info("开始新增头程虚拟仓订单跟踪");
        boolean save = super.save(firstMileProcessingEntity);
        if(!save) {
            throw new ServiceException("头程虚拟仓订单跟踪保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程虚拟仓订单跟踪" , firstMileProcessingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileProcessingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileProcessingEntity.getId(), firstMileProcessingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileProcessingDTO.UpdateDTO updateDTO) {
        FirstMileProcessingEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程虚拟仓订单跟踪"));
        FirstMileProcessingEntity firstMileProcessingEntity =  BeanMapperUtils.map(FirstMileProcessingEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileProcessingEntity);
        log.info("编辑 开始修改头程虚拟仓订单跟踪数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileProcessingEntity);
        if(!save) {
            throw new ServiceException("头程虚拟仓订单跟踪保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程虚拟仓订单跟踪日志数据，id：【{}】", firstMileProcessingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileProcessingEntity.getId(), "头程虚拟仓订单跟踪");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileProcessingEntity, null, firstMileProcessingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<FirstMileProcessingDTO.ListDTO> paging(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<FirstMileProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(FirstMileProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("头程虚拟仓列表信息", EXPORT_WMS_SO_B2C_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileProcessingEntity firstMileProcessingEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:24
     * @param list
     */
    private void fillPageData(List<FirstMileProcessingDTO.ListDTO> list) {
        // TODO 验证数据 & 数据赋值
    }
}
