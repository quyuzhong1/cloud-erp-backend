package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.vo.PagingVO;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.erp.server.oms.mapper.KolFeedbackMapper;
import com.erp.server.oms.service.KolFeedbackService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolFeedbackDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.collection.CollUtil;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * KOL回片列表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolFeedbackServiceImpl extends SuperServiceImpl<KolFeedbackMapper, KolFeedbackEntity> implements KolFeedbackService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolFeedbackDTO.AddDTO addDTO) {
        KolFeedbackEntity kolFeedbackEntity = new KolFeedbackEntity();
        BeanMapperUtils.copy(addDTO, kolFeedbackEntity);

        // 数据处理
        handleData(kolFeedbackEntity);

        log.info("开始新增KOL回片列单");
        boolean save = super.save(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "KOL回片列单" , kolFeedbackEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolFeedbackEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolFeedbackEntity.getId(), kolFeedbackEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolFeedbackDTO.UpdateDTO addOrUpdateDTO) {
        KolFeedbackEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "KOL回片列单"));
        KolFeedbackEntity kolFeedbackEntity =  BeanMapperUtils.map(KolFeedbackEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackEntity);
        log.info("编辑 开始修改KOL回片列单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录KOL回片列单日志数据，id：【{}】", kolFeedbackEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackEntity.getId(), "KOL回片列单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackEntity, null, kolFeedbackEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolFeedbackDTO.ListDTO> paging(PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<KolFeedbackDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<KolFeedbackDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public void batchDelete(BaseIdsDTO.IdsDTO dto) {
        if (CollUtil.isEmpty(dto.getIds())) {
            throw new ServiceException("删除ID列表不能为空");
        }
        // TODO 实现批量删除逻辑
        boolean remove = super.removeByIds(dto.getIds());
        if (!remove) {
            throw new ServiceException("批量删除失败");
        }
    }

    @Override
    public void export(PagingDTO<KolFeedbackDTO.ParamDTO> dto, HttpServletResponse response) {
        // TODO 实现导出逻辑
    }

    @Override
    public void importData(MultipartFile file) {
        // TODO 实现导入逻辑
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {

    }

    /**
     * 状态统计
     * @param param
     * @return
     */
    @Override
    public List<KolFeedbackDTO.TabListDTO> tabList(PermissionsDTO param) {
        KolFeedbackDTO.ParamDTO searchParam = new KolFeedbackDTO.ParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<KolFeedbackDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName（这里可以根据实际业务需求设置状态名称）
        list.stream().forEach(e -> {
            // TODO: 根据实际业务需求设置状态名称，可以创建枚举类或使用字典
            e.setTabFlagName(e.getTabFlag() != null ? e.getTabFlag() : "");
        }); 
        
        // 计算合计数量并添加"全部"标签
        int totalCount = list.stream().mapToInt(KolFeedbackDTO.TabListDTO::getCount).sum();
        KolFeedbackDTO.TabListDTO allTab = new KolFeedbackDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolFeedbackEntity kolFeedbackEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
