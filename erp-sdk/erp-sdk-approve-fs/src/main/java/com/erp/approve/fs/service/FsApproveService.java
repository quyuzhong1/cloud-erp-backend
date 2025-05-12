package com.erp.approve.fs.service;

import com.erp.approve.fs.config.FsApproveProperties;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.approval.v4.enums.DepartmentIdTypeEnum;
import com.lark.oapi.service.approval.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

// SDK 使用文档：https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/server-side-sdk/java-sdk-guide/preparations
/**
 * @Classname FsApproveService
 * @Date 2022-08-22 9:22
 * @Created by yl
 */

@Slf4j
@Component
public class FsApproveService {
	private final FsApproveProperties fsProperties;

	@Autowired
    public FsApproveService(FsApproveProperties fsProperties) {
        this.fsProperties = fsProperties;
    }

    /**
	 * 获取Client实例的方法
	 * 该方法用于构建并返回一个配置了必要参数的Client实例，以便与飞书API进行交互
	 * @return Client 实例，用于发送网络请求
	 */
	public Client getClient() {
		// 构建client
		Client client = Client.newBuilder(fsProperties.getAppId(), fsProperties.getAppSecret())
				.requestTimeout(3, TimeUnit.SECONDS) // 设置httpclient 超时时间，默认永不超时
				.logReqAtDebug(true) // 在 debug 模式下会打印 http 请求和响应的 headers、body 等信息。.build();
				.build();

		//SDK 使用 client. 业务域.版本.资源 .方法名称 来定位具体的 API 方法
		return client;
	}

	/**
	 * 创建三方审批定义
	 * 该方法用于向外部系统发起审批定义的创建请求
	 *
	 * @param req 包含审批定义信息的请求对象
	 */
	public void createExternalApproval(CreateExternalApprovalReq req){
		// 构建client
		Client client = getClient();
		// 创建请求对象
		req = getCreateExternalApprovalReq();

		try {
			// 发起请求
			CreateExternalApprovalResp resp = client.approval().v4().externalApproval().create(req);

			// 处理服务端错误
			if (!resp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return;
			}
			// 业务数据处理
			System.out.println(Jsons.DEFAULT.toJson(resp.getData()));
		} catch (Exception e) {
			log.error("Error occurred while creating external approval", e);
		}
	}

	private static CreateExternalApprovalReq getCreateExternalApprovalReq() {
		CreateExternalApprovalReq req = CreateExternalApprovalReq.newBuilder()
				.departmentIdType(DepartmentIdTypeEnum.OPENDEPARTMENTID.getValue())
				.externalApproval(ExternalApproval.newBuilder()
						.approvalName("@i18n@1")
						.approvalCode("permission_test")
						.groupCode("work_group")
						.groupName("@i18n@2")
						.external(ApprovalCreateExternal.newBuilder()
								.createLinkMobile("https://applink.feishu.cn/client/mini_program/open?appId=cli_9c90fc38e07a9101&path=pages%2Fapproval-form%2Findex%3Fid%3D9999")
								.createLinkPc("https://applink.feishu.cn/client/mini_program/open?mode=appCenter&appId=cli_9c90fc38e07a9101&path=pc%2Fpages%2Fcreate-form%2Findex%3Fid%3D9999")
								.supportPc(true)
								.supportMobile(true)
								.supportBatchRead(false)
								.enableMarkReaded(false)
								.actionCallbackUrl("http://feishu.cn/approval/openapi/operate")
								.actionCallbackToken("sdjkljkx9lsadf110")
								.actionCallbackKey("gfdqedvsadfgfsd")
								.build())
						.viewers(new ApprovalCreateViewers[] {
								ApprovalCreateViewers.newBuilder()
										.viewerType("TENANT")
										.build()
						})
						.i18nResources(new I18nResource[] {
								I18nResource.newBuilder()
										.locale("zh-CN")
										.texts(new I18nResourceText[] {
												I18nResourceText.newBuilder()
														.key("@i18n@1")
														.value("people")
														.build(),
												I18nResourceText.newBuilder()
														.key("@i18n@2")
														.value("hr")
														.build()
										})
										.isDefault(true)
										.build()
						})
						.managers(new String[] {
								"96449fb3"
						})
						.build())
				.build();
		return req;
	}

	/**
	 * 创建外部实例
	 *
	 * 此方法用于向外部系统发起创建实例的请求，通过构建请求对象并使用客户端发送请求
	 * 如果请求成功，将处理返回的数据；如果请求失败，则打印错误信息
	 *
	 * @param req 创建外部实例的请求对象，包含创建实例所需的信息
	 */
	public void createExternalInstance(CreateExternalInstanceReq req){
		// 构建client
		Client client = getClient();
		// 创建请求对象
		req = getCreateExternalInstanceReq();

        try {
            // 发起请求
            CreateExternalInstanceResp resp = client.approval().v4().externalInstance().create(req);

            // 处理服务端错误
            if(!resp.success()) {
                System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                        resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
                return;
            }
            // 业务数据处理
            System.out.println(Jsons.DEFAULT.toJson(resp.getData()));
        } catch (Exception e) {
			log.error("Error occurred while creating external instance", e);
        }
    }

	private static CreateExternalInstanceReq getCreateExternalInstanceReq() {
		CreateExternalInstanceReq req;
		req = CreateExternalInstanceReq.newBuilder()
				.externalInstance(ExternalInstance.newBuilder()
						.approvalCode("81D31358-93AF-92D6-7425-01A5D67C4E71")
						.status("PENDING")
						.extra("{\"business_key\":\"xxx\",\"xxx\":\"xxx\"}")
						.instanceId("24492654")
						.links(ExternalInstanceLink.newBuilder()
								.pcLink("https://applink.feishu.cn/client/mini_program/open?mode=appCenter&appId=cli_9c90fc38e07a9101&path=pc/pages/detail?id=1234")
								.mobileLink("https://applink.feishu.cn/client/mini_program/open?appId=cli_9c90fc38e07a9101&path=pages/detail?id=1234")
								.build())
						.title("@i18n@1")
						.form(new ExternalInstanceForm[] {
								ExternalInstanceForm.newBuilder()
										.name("@i18n@2")
										.value("@i18n@3")
										.build()
						})
						.userId("a987sf9s")
						.userName("@i18n@9")
						.openId("ou_be73cbc0ee35eb6ca54e9e7cc14998c1")
						.departmentId("od-8ec33278bc2")
						.departmentName("@i18n@10")
						.startTime("1556468012678")
						.endTime("1556468012678")
						.updateTime("1556468012678")
						.displayMethod("BROWSER")
						.updateMode("UPDATE")
						.taskList(new ExternalInstanceTaskNode[] {
								ExternalInstanceTaskNode.newBuilder()
										.taskId("112534")
										.userId("a987sf9s")
										.openId("ou_be73cbc0ee35eb6ca54e9e7cc14998c1")
										.title("@i18n@4")
										.links(ExternalInstanceLink.newBuilder()
												.pcLink("https://applink.feishu.cn/client/mini_program/open?mode=appCenter&appId=cli_9c90fc38e07a9101&path=pc/pages/detail?id=1234")
												.mobileLink("https://applink.feishu.cn/client/mini_program/open?appId=cli_9c90fc38e07a9101&path=pages/detail?id=1234")
												.build())
										.status("PENDING")
										.extra("{\"complete_reason\":\"approved\",\"xxx\":\"xxx\"}")
										.createTime("1556468012678")
										.endTime("1556468012678")
										.updateTime("1556468012678")
										.actionContext("123456")
										.actionConfigs(new ActionConfig[] {
												ActionConfig.newBuilder()
														.actionType("APPROVE")
														.actionName("@i18n@5")
														.isNeedReason(false)
														.isReasonRequired(false)
														.isNeedAttachment(false)
														.build()
										})
										.displayMethod("BROWSER")
										.excludeStatistics(false)
										.nodeId("node")
										.nodeName("i18n@name")
										.build()
						})
						.ccList(new CcNode[] {
								CcNode.newBuilder()
										.ccId("123456")
										.userId("12345")
										.openId("ou_be73cbc0ee35eb6ca54e9e7cc14998c1")
										.links(ExternalInstanceLink.newBuilder()
												.pcLink("https://applink.feishu.cn/client/mini_program/open?mode=appCenter&appId=cli_9c90fc38e07a9101&path=pc/pages/detail?id=1234")
												.mobileLink("https://applink.feishu.cn/client/mini_program/open?appId=cli_9c90fc38e07a9101&path=pages/detail?id=1234")
												.build())
										.readStatus("READ")
										.extra("{\"xxx\":\"xxx\"}")
										.title("xxx")
										.createTime("1556468012678")
										.updateTime("1556468012678")
										.displayMethod("BROWSER")
										.build()
						})
						.i18nResources(new I18nResource[] {
								I18nResource.newBuilder()
										.locale("zh-CN")
										.texts(new I18nResourceText[] {
												I18nResourceText.newBuilder()
														.key("@i18n@1")
														.value("people")
														.build()
										})
										.isDefault(true)
										.build()
						})
						.trusteeshipUrlToken("788981c886b1c28ac29d1e68efd60683d6d90dfce80938ee9453e2a5f3e9e306")
						.trusteeshipUserIdType("user_id")
						.trusteeshipUrls(TrusteeshipUrls.newBuilder()
								.formDetailUrl("https://#{your_domain}/api/form_detail")
								.actionDefinitionUrl("https://#{your_domain}/api/action_definition")
								.approvalNodeUrl("https://#{your_domain}/api/approval_node")
								.actionCallbackUrl("https://#{your_domain}/api/action_callback")
								.pullBusinessDataUrl("https://#{your_domain}/api/pull_business_data")
								.build())
						.trusteeshipCacheConfig(TrusteeshipInstanceCacheConfig.newBuilder()
								.formPolicy("DISABLE")
								.formVaryWithLocale(false)
								.formVersion("1")
								.build())
						.build())
				.build();
		return req;
	}

	/**
	 * 检查外部实例是否符合要求
	 * 该方法用于向外部系统发送请求，以验证外部实例的状态或条件
	 *
	 * @param req 包含检查请求信息的对象，包括需要验证的外部实例的相关数据
	 */
	public void checkExternalInstance(CheckExternalInstanceReq req){
		// 构建client
		Client client = getClient();
		// 创建请求对象
		req = getCheckExternalInstanceReq();

		try {
			// 发起请求
			CheckExternalInstanceResp resp = client.approval().v4().externalInstance().check(req);

			// 处理服务端错误
			if(!resp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return;
			}

			// 业务数据处理
			System.out.println(Jsons.DEFAULT.toJson(resp.getData()));
		} catch (Exception e) {
			log.error("Error occurred while check external instance", e);
		}
	}

	private static CheckExternalInstanceReq getCheckExternalInstanceReq() {
		CheckExternalInstanceReq req;
		req = CheckExternalInstanceReq.newBuilder()
				.checkExternalInstanceReqBody(CheckExternalInstanceReqBody.newBuilder()
						.instances(new ExteranlInstanceCheck[] {
								ExteranlInstanceCheck.newBuilder()
										.instanceId("1234234234242423")
										.updateTime("1591603040000")
										.tasks(new ExternalInstanceTask[] {
												ExternalInstanceTask.newBuilder()
														.taskId("112253")
														.updateTime("1591603040000")
														.build()
										})
										.build()
						})
						.build())
				.build();
		return req;
	}

	/**
	 * 获取三方审批任务状态
	 * 该方法用于调用API列出外部任务
	 *
	 * @param req 列出外部任务的请求对象，包含列出任务所需的参数
	 */
	public void listExternalTask(ListExternalTaskReq req){
		// 构建client
		Client client = getClient();
		// 创建请求对象
		req = getListExternalTaskReq();

		try {
			// 发起请求
			ListExternalTaskResp resp = client.approval().v4().externalTask().list(req);

			// 处理服务端错误
			if(!resp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return;
			}

			// 业务数据处理
			System.out.println(Jsons.DEFAULT.toJson(resp.getData()));
		} catch (Exception e) {
			log.error("Error occurred while listExternalTask", e);
		}
	}

	private static ListExternalTaskReq getListExternalTaskReq() {
		ListExternalTaskReq req;
		req = ListExternalTaskReq.newBuilder()
				.pageSize(10)
				.pageToken("")
				.listExternalTaskReqBody(ListExternalTaskReqBody.newBuilder()
						.approvalCodes(new String[]{})
						.instanceIds(new String[]{})
						.userIds(new String[]{})
						.status("")
						.build())
				.build();
		return req;
	}


}
