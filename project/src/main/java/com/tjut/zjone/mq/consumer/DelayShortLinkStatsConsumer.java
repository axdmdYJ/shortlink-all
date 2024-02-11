package com.tjut.zjone.mq.consumer;

import com.tjut.zjone.common.constant.RedisKeyConstant;
import com.tjut.zjone.dto.biz.ShortLinkStatsRecordDTO;
import com.tjut.zjone.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
/**
 * 延迟记录短链接统计组件
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final LinkService shortLinkService;

    /**
     * 处理延迟队列中的消息
     */
    public void onMessage() {
        // 创建一个单线程的线程池，用于执行延迟队列的消费任务
        Executors.newSingleThreadExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_stats_consumer");
                            thread.setDaemon(Boolean.TRUE);
                            return thread;
                        })
                .execute(() -> {
                    // 获取 Redisson 的阻塞队列和延迟队列
                    RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(RedisKeyConstant.DELAY_QUEUE_STATS_KEY);
                    RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);

                    for (; ; ) {
                        try {
                            // 从延迟队列中获取消息
                            ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            if (statsRecord != null) {
                                // 处理短链接统计
                                shortLinkService.shortLinkStats(null, null, statsRecord);
                                continue;
                            }

                            // 如果队列为空，则阻塞等待 500 毫秒
                            LockSupport.parkUntil(500);
                        } catch (Throwable ignored) {
                            // 忽略异常
                        }
                    }
                });
    }

    /**
     * 在属性设置后调用，启动消息处理
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}
