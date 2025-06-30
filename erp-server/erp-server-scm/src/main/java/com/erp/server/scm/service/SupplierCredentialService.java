package com.erp.server.scm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.SupplierCredentialEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 供应商资质表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierCredentialService extends SuperService<SupplierCredentialEntity> {

    /**
     * 保存 供应商资质信息
     * @author yl
     * @date 2023-03-17 16:14
     * @param credentialList
     * @return void
     */
    void saveBatchCredential(List<SupplierCredentialDTO.AddDTO> credentialList);

    /**
     * 根据供应商ｉｄ　获取资质信息
     * @author yl
     * @date 2023-03-20 10:19
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierCredentialDTO.UpdateDTO>
     */
    List<SupplierCredentialDTO.UpdateDTO> getBySupplierId(String supplierId);

    /**
     * 修改供应商资质信息
     * @author yl
     * @date 2023-03-20 11:37
     * @param credentialList
     * @param supplierId
     * @return void
     */
    void updateCredential(List<SupplierCredentialDTO.UpdateDTO> credentialList, String supplierId);

    /**
     * 根据供应商id 集合删除资质信息
     * @author yl
     * @date 2023-03-20 18:56
     * @param ids
     * @return void
     */
    void removeBySupplierIds(List<String> ids);

    /**
     * 检查资质日期
     * @author yl
     * @date 2023-03-29 15:48
     * @param credentialList
     * @return void
     */
    void checkListDate(List<SupplierCredentialDTO.AddDTO> credentialList);

    void checkDate(SupplierCredentialEntity supplierCredentialEntity);

    /**
     * 转化 导入的数据
     * @author yl
     * @date 2023-03-31 9:11
     * @param supplierId
     * @param credentialList
     * @return java.util.List<com.erp.model.scm.entity.SupplierAccountEntity>
     */
    List<SupplierCredentialEntity> transform(String supplierId, List<SupplierCredentialDTO.ImportAddDTO> credentialList);

    BaseResultDTO.AddDTO add(SupplierCredentialDTO.AddDTO dto);

    void getCredentialStatuses(SupplierCredentialEntity addEntity);

    void updateBatchCredential(List<SupplierCredentialDTO.UpdateDTO> credentialList);

    Boolean update(SupplierCredentialDTO.UpdateDTO dto);

    List<SupplierCredentialDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<SupplierCredentialDTO.ListDTO> paging(PagingDTO<SupplierCredentialDTO.PagingParamDTO> dto);

    List<SupplierCredentialDTO.ViewDTO> view(List<String> ids);

    BatchResultDTO delete(String id);

    void exportList(SupplierCredentialDTO.PagingParamDTO dto, HttpServletResponse response);

    BatchResultDTO updateStatus(String id);

    DictBasicDTO addDictCredential(String credentialName);
}
