package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectInfoEntity;

import java.util.List;

/**
 * <p>
 * 产品项目表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectInfoService extends IService<ProjectInfoEntity> {


    ProjectInfoDTO projectInfo(ProductTaskCountShowDTO productTaskCountShowDTO);


    Boolean startProject(StartProjectDTO dto);



    void updateCharge(String projectId, String useName, String userId,Boolean isUpdate);

    PagingVO<List<ProductShowDTO>> paging(PagingDTO<ProductSearchDTO> dto);

    void checkProjectFinish(String productId);

    List<StartItemSourceDTO> getStartItemSourceList();

    void addProject(String productId,String productName);

    void removeByProductId(String productId);

    boolean archive(String productId);

    ProjectInfoEntity getByProductId(String productId);

    List<BasicDTO> listProjectInfo(ProductSearchDTO dto);

    List<ProjectInfoEntity> getByProductIdList(List<String> productIdList);
}
