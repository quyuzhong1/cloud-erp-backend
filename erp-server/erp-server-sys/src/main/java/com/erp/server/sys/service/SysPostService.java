package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BasePagingSearchDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.SysPostDTO;
import com.erp.model.sys.entity.SysPostEntity;

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
