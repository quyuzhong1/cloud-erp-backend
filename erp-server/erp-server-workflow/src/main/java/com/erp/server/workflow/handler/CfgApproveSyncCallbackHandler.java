package com.erp.server.workflow.handler;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.*;

/**
 * 构建ERP审批同步的请求体
 * @author jack
 * @date 2025-05-21
 */
@Slf4j
@Component
public class CfgApproveSyncCallbackHandler {

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ProcessManagementService processManagementService;
    @Resource
    private ProcessTaskManagementService processTaskManagementService;
    @Resource
    private ProcessTaskManagementExtService processTaskManagementExtService;
    @Resource
    private ApproveSyncRecordService approveSyncRecordService;
    /**
     *
     * @author jack
     * @date 2025-05-22
     */
    public Boolean quickApproveCallbackHandler(FsCallbackApiReqDTO req ) {
        Map<String, Object> dataJson = cfgSettingService.getFsActionCallback();
        if(Objects.nonNull(dataJson)){
            String str = CBCDecrypter(String.valueOf(dataJson.get("actionCallbackKey")), req.getEncrypt());
            if(StringUtils.isNotBlank(str)){
                try {
                    //转换实体类
                    ObjectMapper objectMapper = new ObjectMapper();
                    FsCallbackApiReqDTO callbackData = objectMapper.readValue(str, FsCallbackApiReqDTO.class);
                    //消息id
                    String messageId = callbackData.getMessageId();
                    //原因
                    String reason = callbackData.getReason();
                    //审批任务操作类型  APPROVE：同意   REJECT：拒绝
                    String actionType = callbackData.getActionType();

                    String thirdUserId = callbackData.getUserId();
                    SysUserThirdEntity sysUserThirdEntity = sysUserFeign.getUserByThird(ThirdpartyPlatformEnum.FS.getCode(), thirdUserId);
                    if(Objects.isNull(sysUserThirdEntity)){
                        //todo 记录失败 返回失败
                        return Boolean.FALSE;
                    }

                    ProcessTaskManagementExtEntity processTaskManagementExtEntity = processTaskManagementExtService.lambdaQuery().eq(ProcessTaskManagementExtEntity::getMessageId, messageId).last("limit 1").one();
                    if(Objects.isNull(processTaskManagementExtEntity)){
                        //todo 记录失败 返回失败
                        return Boolean.FALSE;
                    }

                    String processTaskManagementId = processTaskManagementExtEntity.getProcessTaskManagementId();
                    ProcessTaskManagementEntity processTaskManagementEntity = processTaskManagementService.getById(processTaskManagementId);
                    if(Objects.isNull(processTaskManagementEntity)){
                        //todo 记录失败 返回失败
                        return Boolean.FALSE;
                    }
                    //判断流程节点状态是可以审批状态
                    if(!processTaskManagementEntity.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)){
                        //todo 记录失败 返回失败
                        return Boolean.FALSE;
                    }
                    String processInstanceId = processTaskManagementEntity.getProcessInstanceId();
                    List<ProcessTaskManagementEntity> processTaskManagementList = processTaskManagementService.lambdaQuery().eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId).list();

                    //流程实例管理
                    ProcessManagementEntity processManagementEntity = processManagementService.getByProcessInstanceId(processInstanceId);
                    if(Objects.isNull(processManagementEntity)){
                        //todo 记录失败 返回失败
                        return Boolean.FALSE;
                    }
                    //调用各个系统的approveEnd方法
                    EndProcessDTO endProcessDTO = new EndProcessDTO();
                    endProcessDTO.setBusinessId(processManagementEntity.getBusinessId());
                    endProcessDTO.setBusinessKey(processManagementEntity.getBusinessKey());
                    endProcessDTO.setApproveStatus(actionType.equals("APPROVE")  ? ApproveTypeEnum.PASS : ApproveTypeEnum.REJECT);
                    Map<String, Object> variablesMap =new HashMap<>();
//                    Map<String, Object> variablesMap = processManagementService.getVariablesMap(processManagementEntity.getBusinessKey(), endProcessDTO);
//                    if(Objects.isNull(variablesMap)){
//                        //todo 记录失败 返回失败
//                        return Boolean.FALSE;
//                    }

                    ProcessManagementDTO.ApproveDTO dto = new ProcessManagementDTO.ApproveDTO();
                    dto.setBusinessId(processManagementEntity.getBusinessId());
                    dto.setBusinessKey(processManagementEntity.getBusinessKey());
                    dto.setApproveType(actionType.equals("APPROVE")  ? ApproveTypeEnum.PASS : ApproveTypeEnum.REJECT);
                    dto.setComment(reason);
                    dto.setUserId(sysUserThirdEntity.getUserId());
                    dto.setVariablesMap(variablesMap);
                    log.info("#####ProcessFeignController :::::approve>>>>> 流程审核入参 dto={}", JSONUtil.toJsonStr(dto));
                    ProcessManagementDTO.ApproveResultDTO data = processManagementService.approveProcess(dto, Boolean.TRUE);
                    if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
                        //调用各个系统的approveEnd方法
                        processManagementService.callFeign(processManagementEntity.getBusinessKey(), endProcessDTO);
                    }
                }catch (Exception e) {
                    log.error("解析回调数据失败，数据={}", req.getEncrypt());
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 用随机生成的前16字节IV进行解密,更加具有普遍性
     * @param key 密钥
     * @param source 密文
     * @return 明文
     */
    public static String CBCDecrypter(String key, String source){
        try {
            byte[] ciphertext = Base64.getDecoder().decode(source); // BASE64解密
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(key.getBytes());
            SecretKeySpec skeySpec = new SecretKeySpec(messageDigest.digest(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // "算法/模式/补码方式"
            // 从密文前 16 个字节提取出 IV
            byte[] ivBytes = new byte[16];
            System.arraycopy(ciphertext, 0, ivBytes, 0, ivBytes.length);
            IvParameterSpec iv = new IvParameterSpec(ivBytes); //向量iv
            // 提取出密文 16 个字节以后的内容，即去除 IV 后真正的密文
            byte[] actualCiphertext = new byte[ciphertext.length - ivBytes.length];
            System.arraycopy(ciphertext, ivBytes.length, actualCiphertext, 0, actualCiphertext.length);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] decrypted = cipher.doFinal(actualCiphertext);
            return new String(decrypted);
        } catch (Exception e) {
        }
        return null;
    }
    /**
     * 加密
     * @param key 密钥
     * @param source 明文
     * @return 密文
     */
    public String CBCEncrypter(String key, String source){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(key.getBytes());
            SecretKeySpec skeySpec = new SecretKeySpec(messageDigest.digest(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
            byte[] sSrcBytes = source.getBytes();
            byte[] newSrc =  new byte[sSrcBytes.length + 16];
            byte[] cSrc = new byte[16];
            System.arraycopy(cSrc, 0, newSrc, 0, cSrc.length);
            System.arraycopy(sSrcBytes, 0, newSrc, 16, sSrcBytes.length);
            IvParameterSpec iv = new IvParameterSpec(cSrc);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(newSrc);
            return Base64.getEncoder().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
        } catch (Exception e) {
            //handle Exception
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            String key = "9527";
            String source ="{\n" +
                    "  \"action_type\": \"REJECT\",\n" +
                    "  \"user_id\": \"1319c76g\",\n" +
                    "  \"approval_code\": \"0F625108-DBA7-4B25-B85D-0BBE76CD8ABC\",\n" +
                    "  \"message_id\": \"7514304574156046364\",\n" +
                    "  \"reason\": \"1234564894654\"\n" +
                    "}";
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(key.getBytes());
            SecretKeySpec skeySpec = new SecretKeySpec(messageDigest.digest(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
            byte[] sSrcBytes = source.getBytes();
            byte[] newSrc =  new byte[sSrcBytes.length + 16];
            byte[] cSrc = new byte[16];
            System.arraycopy(cSrc, 0, newSrc, 0, cSrc.length);
            System.arraycopy(sSrcBytes, 0, newSrc, 16, sSrcBytes.length);
            IvParameterSpec iv = new IvParameterSpec(cSrc);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(newSrc);
            String str = Base64.getEncoder().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
            System.out.println("encrypted====="+str);
        } catch (Exception e) {
            //handle Exception
        }


    }

}
