package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.entity.KolSocialMediaEntity;
import com.erp.server.oms.mapper.KolSocialMediaMapper;
import com.erp.server.oms.mapper.KolFeedbackMapper;
import com.erp.server.oms.service.KolSocialMediaService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolSocialMediaDTO;

import java.time.LocalDateTime;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import cn.hutool.crypto.digest.DigestUtil;
import com.erp.model.oms.enums.KolSocialMediaTypeEnum;
import com.erp.model.oms.enums.FeedbackStatusEnum;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.common.business.dto.base.BatchResultDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_SOCIAL_MEDIA;
/**
 * <p>
 * 达人社媒数据表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolSocialMediaServiceImpl extends SuperServiceImpl<KolSocialMediaMapper, KolSocialMediaEntity> implements KolSocialMediaService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private KolFeedbackMapper kolFeedbackMapper;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private com.erp.server.oms.service.DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSocialMediaDTO.AddDTO addDTO) {
        KolSocialMediaEntity kolSocialMediaEntity = new KolSocialMediaEntity();
        BeanMapperUtils.copy(addDTO, kolSocialMediaEntity);

        // 数据处理
        handleData(kolSocialMediaEntity);

        log.info("开始新增达人社媒数据单");
        boolean save = super.save(kolSocialMediaEntity);
        if(!save) {
            throw new ServiceException("达人社媒数据单保存失败");
        }
        
        // 保存成功后，查询和当前urlHash一致的回片列表，修改回片状态为已回片
        if (StrUtil.isNotBlank(kolSocialMediaEntity.getUrlHash())) {
            LambdaUpdateWrapper<KolFeedbackEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KolFeedbackEntity::getUrlHash, kolSocialMediaEntity.getUrlHash())
                    .eq(KolFeedbackEntity::getFeedbackStatus, FeedbackStatusEnum.PENDING.getCode())
                    .set(KolFeedbackEntity::getFeedbackStatus, FeedbackStatusEnum.COMPLETED.getCode());
            int updateCount = kolFeedbackMapper.update(null, updateWrapper);
            log.info("更新回片状态：urlHash=【{}】，更新数量=【{}】", kolSocialMediaEntity.getUrlHash(), updateCount);
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "达人社媒数据单" , kolSocialMediaEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_SOCIAL_MEDIA.getCode(), kolSocialMediaEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(kolSocialMediaEntity.getId(), kolSocialMediaEntity.getId());
    }

    /**
    * 批量新增
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchAdd(KolSocialMediaDTO.BatchAddDTO dto) {
        List<KolSocialMediaDTO.AddDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());

        for (KolSocialMediaDTO.AddDTO addDTO : list) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = add(addDTO);
                addResult = BatchResultDTO.success(result.getId(), result.getCode(), "新增成功");
            } catch (Exception e) {
                log.error("达人社媒数据批量新增失败", e);
                String title = addDTO.getTitle() != null ? addDTO.getTitle() : "";
                addResult = BatchResultDTO.fail("", title, e.getMessage());
            }
            resultDTOS.add(addResult);
        }

        return resultDTOS;
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSocialMediaDTO.UpdateDTO addOrUpdateDTO) {
        KolSocialMediaEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "达人社媒数据单"));
        KolSocialMediaEntity kolSocialMediaEntity =  BeanMapperUtils.map(KolSocialMediaEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSocialMediaEntity);
        log.info("编辑 开始修改达人社媒数据单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolSocialMediaEntity);
        if(!save) {
            throw new ServiceException("达人社媒数据单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录达人社媒数据单日志数据，id：【{}】", kolSocialMediaEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolSocialMediaEntity.getId(), "达人社媒数据单");
        operateLogService.addModuleOperateLogByObj(old, kolSocialMediaEntity, ModuleTypeEnum.KOL_SOCIAL_MEDIA.getCode(), kolSocialMediaEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolSocialMediaEntity kolSocialMediaEntity) {
        // type 枚举校验（如果为空，可以设置默认值，或者保持为空）
        if (StrUtil.isNotBlank(kolSocialMediaEntity.getType())) {
            KolSocialMediaTypeEnum typeEnum = KolSocialMediaTypeEnum.getByCode(kolSocialMediaEntity.getType());
            if (typeEnum == null) {
                throw new ServiceException("类型【" + kolSocialMediaEntity.getType() + "】不存在，请使用 manual（手动）或 auto（自动）");
            }
        }else {
            kolSocialMediaEntity.setType(KolSocialMediaTypeEnum.MANUAL.getCode());
        }

        // 第三平台如果为空，默认填 "erp"
        if (StrUtil.isBlank(kolSocialMediaEntity.getThirdPlatform())) {
            kolSocialMediaEntity.setThirdPlatform("erp");
        }

        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolSocialMediaEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolSocialMediaEntity.getUrl());
            kolSocialMediaEntity.setUrlHash(urlHash);
        }

        // 入库时间填当前时间戳（毫秒）
        if (kolSocialMediaEntity.getInsertTimestamp() == null) {
            kolSocialMediaEntity.setInsertTimestamp(System.currentTimeMillis());
        }

        // 入库时间（timestamp类型）填当前时间
        if (kolSocialMediaEntity.getInsertTime() == null) {
            kolSocialMediaEntity.setInsertTime(LocalDateTime.now());
        }

        // uniqueKey 填充随机唯一标识（使用UUID去掉横线）
        if (StrUtil.isBlank(kolSocialMediaEntity.getUniqueKey())) {
            kolSocialMediaEntity.setUniqueKey(IdUtil.simpleUUID());
        }
    }

    /**
     * 批量删除
     * @author wuhaotian
     * @date: 2025-12-04
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(BaseIdsDTO.IdsDTO dto) {
        if (CollUtil.isEmpty(dto.getIds())) {
            throw new ServiceException("删除ID列表不能为空");
        }
        
        // 查询要删除的数据
        List<KolSocialMediaEntity> list = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到要删除的数据");
        }
        
        // 执行批量删除
        boolean remove = super.removeByIds(dto.getIds());
        if (!remove) {
            throw new ServiceException("批量删除失败");
        }
        
        log.info("批量删除达人社媒数据成功，删除数量：{}", list.size());
    }

    /**
     * 单个删除
     * @author wuhaotian
     * @date: 2025-12-04
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        KolSocialMediaEntity entity = super.getById(id);
        if (entity == null) {
            throw new ServiceException("达人社媒数据不存在");
        }
        
        // 执行删除
        boolean remove = super.removeById(id);
        if (!remove) {
            throw new ServiceException("删除失败");
        }
        
        log.info("删除达人社媒数据成功，ID：{}", id);
        return BatchResultDTO.success(entity.getId(), entity.getId());
    }

    /**
     * 导出
     * @author wuhaotian
     * @date: 2025-12-04
     * @param dto
     * @return
     */
    @Override
    public Boolean export(PagingDTO<KolSocialMediaDTO.ParamDTO> dto) {
        downloadTaskFeign.saveDownloadTask("达人社媒数据表导出", EXPORT_OMS_KOL_SOCIAL_MEDIA.getCode(), dto);
        return true;
    }

    /**
     * 分页查询
     * @author wuhaotian
     * @date: 2025-12-04
     * @param dto
     * @return
     */
    @Override
    public PagingVO<KolSocialMediaDTO.ListDTO> paging(PagingDTO<KolSocialMediaDTO.ParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<KolSocialMediaDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<KolSocialMediaDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 填充列表数据（枚举值转换）
     */
    private void fillList(List<KolSocialMediaDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 查询来源平台字典
        List<DictBasicDTO.ViewDTO> mediaPlatformDictList = dictBasicService.getByKey("socialMediaPlatform");
        Map<String, String> mediaPlatformNameMap = mediaPlatformDictList.stream()
                .collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue,
                        DictBasicDTO.ViewDTO::getName, (v1, v2) -> v1));

        // 属性赋值
        for (KolSocialMediaDTO.ListDTO data : list) {
            // 类型枚举转换
            if (StrUtil.isNotBlank(data.getType())) {
                KolSocialMediaTypeEnum typeEnum = KolSocialMediaTypeEnum.getByCode(data.getType());
                if (typeEnum != null) {
                    data.setTypeName(typeEnum.getName());
                }
            }

            // 来源平台字典转换
            if (StrUtil.isNotBlank(data.getMediaPlatform())) {
                String mediaPlatformName = mediaPlatformNameMap.getOrDefault(data.getMediaPlatform(), "");
                data.setMediaPlatformName(mediaPlatformName);
            }
        }
    }
}
