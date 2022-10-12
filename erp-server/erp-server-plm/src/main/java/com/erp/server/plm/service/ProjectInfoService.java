package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品项目表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectInfoService extends IService<ProjectInfoEntity> {


    ProjectInfoDTO projectInfo(String productId);


    Boolean startProject(StartProjectDTO dto);



    void updateCharge(String projectId, String useName, String userId,Boolean isUpdate);

    PagingVO<List<ProductShowDTO>> paging(PagingDTO<ProductSearchDTO> dto);

    void checkProjectFinish(String productId);

    List<StartItemSourceDTO> getStartItemSourceList();

    void addProject(String productId,String productName);

    void removeByProductId(String productId);
}
