package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
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
import cn.hutool.crypto.digest.DigestUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.oms.enums.FeedbackStatusEnum;
import org.springframework.beans.BeanUtils;

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

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

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
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_FEEDBACK.getCode(), kolFeedbackEntity.getId(), "新增操作");

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
        
        // 如果已回片，不允许编辑
        if (FeedbackStatusEnum.COMPLETED.getCode().equals(old.getFeedbackStatus())) {
            throw new ServiceException("已回片状态不允许编辑");
        }
        
        KolFeedbackEntity kolFeedbackEntity =  BeanMapperUtils.map(KolFeedbackEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackEntity);
        log.info("编辑 开始修改KOL回片列单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录KOL回片列单日志数据，id：【{}】", kolFeedbackEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackEntity.getId(), "KOL回片列单");
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackEntity,  ModuleTypeEnum.KOL_FEEDBACK.getCode(), kolFeedbackEntity.getId(), msg);
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
        
        // 设置tabFlagName，使用FeedbackStatusEnum获取状态名称
        list.forEach(e -> {
            if (StrUtil.isNotBlank(e.getTabFlag())) {
                FeedbackStatusEnum statusEnum = FeedbackStatusEnum.getByCode(e.getTabFlag());
                if (statusEnum != null) {
                    e.setTabFlagName(statusEnum.getName());
                } else {
                    e.setTabFlagName(e.getTabFlag());
                }
            } else {
                e.setTabFlagName("");
            }
        }); 
        
        // 计算合计数量并添加"全部"标签
        int totalCount = list.stream().mapToInt(KolFeedbackDTO.TabListDTO::getCount).sum();
        KolFeedbackDTO.TabListDTO allTab = new KolFeedbackDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        // 排序：全部 -> 待回片 -> 已回片
        list.sort((a, b) -> {
            // "all" 排在最前面
            if ("all".equals(a.getTabFlag())) {
                return -1;
            }
            if ("all".equals(b.getTabFlag())) {
                return 1;
            }
            // 其他状态按照枚举顺序排序：pending -> completed
            FeedbackStatusEnum enumA = FeedbackStatusEnum.getByCode(a.getTabFlag());
            FeedbackStatusEnum enumB = FeedbackStatusEnum.getByCode(b.getTabFlag());
            if (enumA != null && enumB != null) {
                return enumA.ordinal() - enumB.ordinal();
            }
            // 如果找不到枚举，保持原顺序
            return 0;
        });
        
        return list;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolFeedbackEntity kolFeedbackEntity) {
        // 如果 sourceType 为空，默认设置为当前文档的 SourceTypeEnum
        if (StrUtil.isBlank(kolFeedbackEntity.getSourceType())) {
            kolFeedbackEntity.setSourceType(SourceTypeEnum.KOL_FEEDBACK.getCode());
        }

        // 如果 sourceCode 为空，使用 docNoGenHelper.generateCode() 生成
        if (StrUtil.isBlank(kolFeedbackEntity.getSourceCode())) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_HP);
            kolFeedbackEntity.setSourceCode(code);
        }

        // skuNo 和 productName 通过 skuId 查询
        if (StrUtil.isNotBlank(kolFeedbackEntity.getSkuId()) && 
            (StrUtil.isBlank(kolFeedbackEntity.getSkuNo()) || StrUtil.isBlank(kolFeedbackEntity.getProductName()))) {
            try {
                List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(Collections.singletonList(kolFeedbackEntity.getSkuId()));
                if (CollUtil.isNotEmpty(skuList) && !skuList.isEmpty()) {
                    ProductDetailEntity skuInfo = skuList.get(0);
                    kolFeedbackEntity.setSkuNo(skuInfo.getSkuNo());
                    kolFeedbackEntity.setProductName(skuInfo.getName());
                }
            } catch (Exception e) {
                log.warn("获取SKU信息失败，skuId: {}", kolFeedbackEntity.getSkuId(), e);
            }
        }

        // 达人昵称 TODO（因为还没写）
        // if (StrUtil.isNotBlank(kolFeedbackEntity.getPartnerId()) && StrUtil.isBlank(kolFeedbackEntity.getPartnerNickname())) {
        //     String partnerNickname = commonService.getPartnerNicknameByPartnerId(kolFeedbackEntity.getPartnerId());
        //     kolFeedbackEntity.setPartnerNickname(partnerNickname);
        // }

        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolFeedbackEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolFeedbackEntity.getUrl());
            kolFeedbackEntity.setUrlHash(urlHash);
        }

        // feedbackStatus 回片状态默认 "待回片"
        if (StrUtil.isBlank(kolFeedbackEntity.getFeedbackStatus())) {
            kolFeedbackEntity.setFeedbackStatus(FeedbackStatusEnum.PENDING.getCode());
        }
    }
}
