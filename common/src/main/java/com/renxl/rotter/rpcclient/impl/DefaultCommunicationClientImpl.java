/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renxl.rotter.rpcclient.impl;

import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.renxl.rotter.rpcclient.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 通讯交互的client的默认实现实现
 *
 * @author jianghang
 */
@Slf4j
public class DefaultCommunicationClientImpl implements CommunicationClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommunicationClientImpl.class);



    private CommunicationConnectionFactory factory = null;
    private int poolSize = 10;
    private ExecutorService executor = null;
    private int retry = 3;
    private int retryDelay = 1000;
    private boolean discard = false;

    public DefaultCommunicationClientImpl() {
    }

    public DefaultCommunicationClientImpl(CommunicationConnectionFactory factory) {
        this.factory = factory;
    }

    public void initial() {
        RejectedExecutionHandler handler = null;
        if (discard) {
            handler = new ThreadPoolExecutor.DiscardPolicy();
        } else {
            handler = new ThreadPoolExecutor.AbortPolicy();
        }

        executor = new ThreadPoolExecutor(poolSize, poolSize, 60 * 1000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10 * 1000),
                new NamedThreadFactory("communication-async"), handler);
        executor.execute(()->{
            logger.info("start");
        });
    }

    public void destory() {
        executor.shutdown();
    }

    public Object call(final String addr,int port, final Event event) {
        CommunicationParam params = buildParams(addr,port);
        CommunicationConnection connection = null;
        int count = 0;
        Throwable ex = null;
        while (count++ < retry) {
            try {
                connection = factory.createConnection(params);
                return connection.call(event);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(String.format("call[%s] , retry[%s]", addr, count), e);
                try {
                    Thread.sleep(count * retryDelay);
                } catch (InterruptedException e1) {
                    // ignore
                }
                ex = e;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }

        logger.error("call[{}] failed , event[{}]!", addr, event.toString());
        throw new CommunicationException("call[" + addr + "] , Event[" + event.toString() + "]", ex);
    }

    public void call(final String addr, int port,final Event event, final Callback callback) {
        submit(new Runnable() {

            @Override
            public void run() {
                Object obj = call(addr,port,event);
                callback.call(obj);
            }
        });
    }

    public Object call(final String[] addrs,int port, final Event event) {
        if (addrs == null || addrs.length == 0) {
            throw new IllegalArgumentException("addrs example: 127.0.0.1:1099");
        }

        ExecutorCompletionService completionService = new ExecutorCompletionService(executor);
        List<Future<Object>> futures = new ArrayList<Future<Object>>(addrs.length);
        List result = new ArrayList(10);
        for (final String addr : addrs) {
            futures.add(completionService.submit((new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    return DefaultCommunicationClientImpl.this.call(addr,port, event);
                }
            })));
        }

        Exception ex = null;
        int errorIndex = 0;
        while (errorIndex < futures.size()) {
            try {
                Future future = completionService.take();// 它也可能被打断
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                ex = e;
                break;
            } catch (ExecutionException e) {
                ex = e;
                break;
            }

            errorIndex++;
        }

        if (errorIndex < futures.size()) {
            for (int index = 0; index < futures.size(); index++) {
                Future<Object> future = futures.get(index);
                if (future.isDone() == false) {
                    future.cancel(true);
                }
            }
        } else {
            for (int index = 0; index < futures.size(); index++) {
                Future<Object> future = futures.get(index);
                try {
                    result.add(future.get());
                } catch (InterruptedException e) {
                    // ignore
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    // ignore
                }
            }
        }

        if (ex != null) {
            throw new CommunicationException(String.format("call addr[%s] error by %s", addrs[errorIndex],
                    ex.getMessage()), ex);
        } else {
            return result;
        }
    }

    public void call(final String[] addrs,int port, final Event event, final Callback callback) {
        if (addrs == null || addrs.length == 0) {
            throw new IllegalArgumentException("addrs example: 127.0.0.1:1099");
        }
        submit(new Runnable() {

            @Override
            public void run() {
                Object obj = call(addrs,port, event);
                callback.call(obj);
            }
        });
    }

    /**
     * 直接提交一个异步任务
     */
    public Future submit(Runnable call) {
        return executor.submit(call);
    }

    /**
     * 直接提交一个异步任务
     */
    public Future submit(Callable call) {
        return executor.submit(call);
    }

    // ===================== helper method ==================

    private CommunicationParam buildParams(String addr,int port) {
        CommunicationParam params = new CommunicationParam();
        String[] strs = StringUtils.split(addr, ":");
        if (strs == null) {
            throw new IllegalArgumentException("addr example: 127.0.0.1:1099");
        }

        InetAddress address = null;
        try {
            address = InetAddress.getByName(strs[0]);
        } catch (UnknownHostException e) {
            throw new CommunicationException("addr_error", "addr[" + addr + "] is unknow!");
        }
        params.setIp(address.getHostAddress());
        port = strs.length == 1 ? port : Integer.valueOf(strs[1]);

        params.setPort(port);
        return params;
    }

    // ============================= setter / getter ==========================

    public void setFactory(CommunicationConnectionFactory factory) {
        this.factory = factory;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }


}
