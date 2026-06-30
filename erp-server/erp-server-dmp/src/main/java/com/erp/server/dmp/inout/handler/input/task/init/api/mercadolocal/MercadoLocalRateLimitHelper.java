package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 美客多本土站接口限流退避 / 结果缓存的公共助手。
 *
 * <p>背景：
 * 美客多本土站的订单 / 账单 / 物流 / 物流历史 / 物流SLA / 费用明细 接口在高并发或大批量
 * 跑数时易被平台限流（典型表现 503 / 429）。原实现里所有 init handler 在收到非 200/201 时
 * 都会直接抛 RuntimeException，引起：
 * <ol>
 *   <li>当前事务回滚 → 锁释放慢；</li>
 *   <li>输入任务被标记失败、调度器立刻重试 → 短时间内反复打 API → 限流加剧；</li>
 *   <li>同店铺多个并发任务竞争 dmp_so_info 唯一键，触发 lock timeout 告警。</li>
 * </ol>
 *
 * <p>本助手提供两件事：
 * <ul>
 *   <li>限流退避标记（Redis key 带 TTL）：handler 入口先看是否处于退避中，若是则软退（让任务下次调度再来），
 *       不会真去打 API；收到平台 429/502/503/504 时也写入退避标记。
 *       <b>退避标记 key 不带 inputTaskId</b>，按 (userId, bizType) 全局生效——平台层面的短期冷却，
 *       告诉同店铺所有任务别打。</li>
 *   <li>单条结果缓存（按 inputTaskId 隔离）：cache key 形如
 *       {@code third:platform:apiResult:{platform}:{bizType}:{userId}:{inputTaskId}:{uniqueId}}，
 *       <b>跨任务天然 miss</b>。语义上只服务"当前任务软退后下次重试时跳过已成功的部分"，
 *       不会让别的任务读到当前任务写下的旧快照。</li>
 * </ul>
 *
 * <p>缓存 TTL 按"接口字段变化频率"分两档：
 * <ul>
 *   <li>{@link #CACHE_SECONDS_TASK_LIFE} = 600s：状态会变的接口（{@code shipments} 详情、{@code orders.billing}
 *       账单）。TTL 大致覆盖一个 inputTask 的完整生命周期（含多次软退-重试），
 *       保证当前任务 retry 能命中；下一个调度周期的新 inputTask 因 key 不同自然 miss，
 *       会再去拉真实状态。最坏滞后 ≈ 当前任务生命周期 + 1 个调度周期 ≈ 15~25 分钟，
 *       属于增量同步天然滞后，业务可接受。</li>
 *   <li>{@link #CACHE_SECONDS_STABLE} = 1800s：已落地不变的接口（{@code shipments.history} 物流轨迹、
 *       {@code shipments.sla}、{@code orders.cost}）。事件已发生不会被改写，
 *       TTL 长一些没有正确性风险，多覆盖几次重试更安全。</li>
 * </ul>
 *
 * <p>{@code orders.search}（主任务）本身是分页拉取，不属于"按 fid 逐条调用"模型，不做结果缓存。
 *
 * <p>本助手只服务于 mercadolocal 包下的几个 init handler，不动通用任务调度逻辑。
 */
@Slf4j
@Component
public class MercadoLocalRateLimitHelper {

	@Resource
	private RedisUtil redisUtil;

	/**
	 * 默认限流退避时长（秒）。命中 429/503 之类的限流码时写入这个 TTL，
	 * TTL 内同店铺、同业务类型的请求会被 handler 主动跳过。
	 *
	 * <p>注意：这个值不需要太长。软退后调度器一般秒级 ~ 分钟级就会重新调度本任务，
	 * 期间会被这个标记反复挡住（每次只是"看一眼 Redis"，零 API 消耗）。
	 * 只要平台冷却好，30 秒后下一轮调度就能恢复。
	 */
	public static final long DEFAULT_BACKOFF_SECONDS = 30L;

	/**
	 * 单条结果缓存默认时长（秒），调用 {@link #setResultCache(String, String, String, String, String)}
	 * 不传 TTL 时退化到本值。建议 handler 显式选择档位，参见 {@link #CACHE_SECONDS_TASK_LIFE} /
	 * {@link #CACHE_SECONDS_STABLE}。
	 */
	public static final long DEFAULT_CACHE_SECONDS = 1800L;

	/**
	 * "任务生命周期"档位（秒），适用于状态会变的接口：
	 * <ul>
	 *   <li>{@code shipments} 详情：{@code status} ready_to_ship → shipped → delivered，
	 *       {@code tracking_number} 打单后回填；</li>
	 *   <li>{@code orders.billing} 账单：{@code status} pending → paid。</li>
	 * </ul>
	 * 由于缓存 key 已按 inputTaskId 隔离，命中窗口至多就是"当前任务从首次软退到最终完成"的时间。
	 * 600s（10 分钟）大致覆盖一次任务的多次软退-重试，下一个调度周期的新 inputTask 因 key 不同自然 miss、
	 * 会再去拉真实状态。
	 */
	public static final long CACHE_SECONDS_TASK_LIFE = 600L;

	/**
	 * "已落地不变"档位（秒），适用于事件已发生、不会再改的接口：
	 * <ul>
	 *   <li>{@code shipments.history} 物流轨迹：append-only，旧事件不会被改写；</li>
	 *   <li>{@code shipments.sla} SLA 时间：shipment 创建时基本就定了；</li>
	 *   <li>{@code orders.cost} 费用明细：结算后基本稳定。</li>
	 * </ul>
	 * 1800s（30 分钟）覆盖任务可能经历的最长重试链；这类接口 TTL 长一些没有正确性风险。
	 */
	public static final long CACHE_SECONDS_STABLE = 1800L;

	/** 业务类型：订单列表 search 接口（主任务） */
	public static final String BIZ_ORDER_SEARCH = "orders.search";
	/** 业务类型：订单账单接口 */
	public static final String BIZ_ORDER_BILLING = "orders.billing";
	/** 业务类型：shipments 详情接口 */
	public static final String BIZ_SHIPMENT = "shipments";
	/** 业务类型：shipments history 接口 */
	public static final String BIZ_SHIPMENT_HISTORY = "shipments.history";
	/** 业务类型：shipments SLA 接口 */
	public static final String BIZ_SHIPMENT_SLA = "shipments.sla";
	/** 业务类型：订单费用明细接口 */
	public static final String BIZ_ORDER_COST = "orders.cost";

	/**
	 * 单条结果缓存 key。沿用 RedisCacheConstants 命名风格，区分平台、业务类型、店铺账号、任务 ID、业务唯一标识。
	 *
	 * <p>注意 key 里包含 {@code inputTaskId}：缓存只在"同一个 inputTask 软退后下次重试"时复用，
	 * 跨任务（包括同店铺同接口的另一个 inputTask）天然 miss、不会拿到旧数据。
	 * 这与限流退避标记（不带 taskId、按"店铺账号+接口"全局生效）的语义不同——
	 * 退避是平台层面的短期熔断，缓存只是任务内的 retry 防抖。
	 */
	private static final String RESULT_CACHE_KEY = "third:platform:apiResult:{}:{}:{}:{}:{}";

	/**
	 * 是否已处于限流退避中。
	 */
	public boolean isLimited(String userId, String bizType) {
		return null != redisUtil.get(buildLimitKey(userId, bizType));
	}

	/**
	 * 写入限流退避标记。
	 */
	public void markLimited(String userId, String bizType, long backoffSeconds) {
		long ttl = backoffSeconds <= 0 ? DEFAULT_BACKOFF_SECONDS : backoffSeconds;
		redisUtil.set(buildLimitKey(userId, bizType), DateUtil.now(), ttl);
	}

	/**
	 * 判定是否属于"限流/网关临时不可用"类的 HTTP 状态码：429 / 502 / 503 / 504。
	 * 这类错误属于平台侧短期不可用，handler 应该软退而不是抛异常。
	 */
	public boolean isRateLimitedCode(Integer code) {
		if (code == null) {
			return false;
		}
		int c = code;
		return c == 429 || c == 502 || c == 503 || c == 504;
	}

	/**
	 * 读取单条结果缓存。
	 *
	 * @param inputTaskId 当前 dmp_input_task 的主键，用于把缓存隔离在任务维度，
	 *                    跨任务不命中、避免拿到别的任务写入的旧数据
	 */
	public String getResultCache(String inputTaskId, String userId, String bizType, String uniqueId) {
		Object obj = redisUtil.get(buildResultKey(inputTaskId, userId, bizType, uniqueId));
		return obj == null ? null : obj.toString();
	}

	/**
	 * 写入单条结果缓存（按 {@link #DEFAULT_CACHE_SECONDS} = 1800s 兜底）。
	 * 推荐 handler 显式调用 {@link #setResultCache(String, String, String, String, String, long)}
	 * 并传入 {@link #CACHE_SECONDS_STABLE}，把"档位选择"的意图写在调用点。
	 */
	public void setResultCache(String inputTaskId, String userId, String bizType, String uniqueId, String resultJson) {
		setResultCache(inputTaskId, userId, bizType, uniqueId, resultJson, DEFAULT_CACHE_SECONDS);
	}

	/**
	 * 写入单条结果缓存（自定义 TTL）。
	 *
	 * @param inputTaskId 当前 dmp_input_task 的主键，参与 key 拼接，作用同 {@link #getResultCache}
	 * @param ttlSeconds  缓存秒数；若 ≤ 0 则退化到 {@link #DEFAULT_CACHE_SECONDS}
	 */
	public void setResultCache(String inputTaskId, String userId, String bizType, String uniqueId, String resultJson, long ttlSeconds) {
		long ttl = ttlSeconds <= 0 ? DEFAULT_CACHE_SECONDS : ttlSeconds;
		redisUtil.set(buildResultKey(inputTaskId, userId, bizType, uniqueId), resultJson, ttl);
	}

	private String buildLimitKey(String userId, String bizType) {
		return StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT,
				PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(), userId, bizType);
	}

	private String buildResultKey(String inputTaskId, String userId, String bizType, String uniqueId) {
		return StrUtil.format(RESULT_CACHE_KEY,
				PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(), bizType, userId, inputTaskId, uniqueId);
	}
}
