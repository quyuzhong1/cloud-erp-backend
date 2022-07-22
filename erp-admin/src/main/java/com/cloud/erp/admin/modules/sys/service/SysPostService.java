package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.SysPostDTO;
import com.cloud.erp.admin.modules.sys.entity.SysPostEntity;
import com.cloud.erp.common.common.dto.BasePagingSearchDTO;
import com.cloud.erp.common.common.dto.BaseSearchDTO;
import com.cloud.erp.common.common.dto.PagingDTO;
import com.cloud.erp.common.common.vo.PagingVO;

import java.util.List;

/**
 * @Classname SysPostService
 * @Description TODO
 * @Date 2022-07-12 16:09
 * @Created by yl
 */
public interface SysPostService extends IService<SysPostEntity> {





    boolean savePost(SysPostDTO postEntity);

    boolean updatePost(SysPostDTO postEntity);

    PagingVO paging(PagingDTO<BasePagingSearchDTO> dto);

    boolean removePostByIds(List<String> ids);

    List<SysPostEntity> findPost(BaseSearchDTO dto);
}
