package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;
import com.erp.model.sys.dto.SysUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author zdy
 * @ClassName AuthDataFeign
 * @description: 数据权限
 * @date 2025年03月04日
 * @version: 1.0
 */
@FeignClient(name = "erp-sys", contextId = "authDataFeign",configuration = {FeignErrorDecoder.class})
public interface AuthDataFeign {
    /**
     * 获取店铺的用户
     */
    @PostMapping("feign/auth/getShopUserList")
    List<SysUserDTO.ShopDTO> getShopUserList(@RequestBody String userId);

    /**
     * 获取用户权限列表
     * @param userIdList
     * @return
     */
    @PostMapping("feign/auth/listShopIdByUserIds")
    List<SysUserDTO.ShopDTO> listShopIdByUserIds(@RequestBody List<String> userIdList);
    /**
     * 获取仓库的用户
     */
    @PostMapping("feign/auth/getWarehouseUserList")
    List<SysUserDTO.WarehouseDTO> getWarehouseUserList(@RequestBody String userId);

    /**
     * 获取用户店铺权限
     * @return
     */
    @PostMapping("feign/auth/getShopPermissionSql")
    String getShopPermissionSql(@RequestBody String shopTableField);
    /**
     * 获取用户店铺权限
     * @return
     */
    @PostMapping("feign/auth/getShopPermissionSqlByDynamicDataSource")
    String getShopPermissionSqlByDynamicDataSource(@RequestParam String shopTableField , @RequestParam String dynamicDataSource);
    /**
     * 获取用户仓库权限
     * @return
     */
    @PostMapping("feign/auth/getWarehousePermissionSql")
    String getWarehousePermissionSql(@RequestBody String warehouseTableField);
    /**
     * 获取用户仓库权限
     * @return
     */
    @PostMapping("feign/auth/getWarehousePermissionSqlByDynamicDataSource")
    String getWarehousePermissionSqlByDynamicDataSource(@RequestParam String warehouseTableField , @RequestParam String dynamicDataSource);

    /**
     * @description: 根据店铺id查询已关联用户id
     * @author Will
     * @date: 2023/9/7 9:35
     * @param shopIdList
     * @return List<String>
     */
    @PostMapping("feign/auth/listUserIdByShopIdList")
    List<String> listUserIdByShopIdList(@RequestBody List<String> shopIdList);
    /**
     * 增加用户店铺权限
     * @description:
     * @author zdy
     * @date: 2025/5/27 9:35
     * @param addUserShopAuthDTO
     * @return List<String>
     */
    @PostMapping("feign/auth/addUserShopAuth")
    void addUserShopAuth(@RequestBody AuthUserShopDTO.AddUserShopAuthDTO addUserShopAuthDTO);
    /**
     * 增加用户仓库权限
     * @description:
     * @author zdy
     * @date: 2025/5/27 9:35
     * @param addUserWarehouseAuthDTO
     * @return List<String>
     */
    @PostMapping("feign/auth/addUserWarehouseAuth")
    void addUserWarehouseAuth(@RequestBody AuthUserWarehouseDTO.AddUserWarehouseAuthDTO addUserWarehouseAuthDTO);
}
