package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.mask.cache.CfgMaskWordFullCacheDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgMaskWordDTO;
import com.erp.model.sys.entity.CfgMaskWordEntity;

/**
 * 脱敏词典 服务接口
 *
 * @author cloud-erp
 */
public interface CfgMaskWordService extends SuperService<CfgMaskWordEntity> {

    /**
     * 分页查询
     */
    PagingVO<CfgMaskWordDTO.ListDTO> paging(PagingDTO<CfgMaskWordDTO.SearchParamDTO> dto);

    /**
     * 新增；提交后自动延迟双删 Redis 缓存
     */
    Boolean add(CfgMaskWordDTO.AddDTO dto);

    /**
     * 修改；提交后自动延迟双删 Redis 缓存
     */
    Boolean update(CfgMaskWordDTO.UpdateDTO dto);

    /**
     * 批量删除（逻辑删除）；提交后自动延迟双删 Redis 缓存
     */
    Boolean delete(BaseIdsDTO.IdsDTO dto);

    /**
     * 拉取全量未禁用词典（Feign 暴露给业务节点 Redis miss 兜底）
     */
    CfgMaskWordFullCacheDTO listAllForCache();

    /**
     * 强制刷新：按当前表数据重建 FullCache 并写入 Redis
     */
    Boolean publishFullCache();
}
