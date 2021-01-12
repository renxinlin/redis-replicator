package com.renxl.rotter.pipeline.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.renxl.rotter.pipeline.framework.Asserts;
import com.renxl.rotter.pipeline.framework.RotterResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.renxl.rotter.constants.Constants.IP_PORT_SPLIT;
import static com.renxl.rotter.constants.Constants.MULT_NODE_SPLIT;

/**
 * <p>
 * HA 优先跨机房选择
 * selectNode和sourceRedises期望在一个机房提升网络传输性能
 * targetRedis 和  loadNodes在一个机房提升网络传输性能
 *
 * </p>
 *
 * @author renxl
 * @since 2020-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Validated
public class PipelineConfig implements Serializable {

    /**
     * 同步任务已经启动
     */
    private static final Integer start = 1;
    /**
     * 同步任务已经停止
     */
    private static final Integer stop = 0;

    private static final long serialVersionUID = 1L;
    /**
     * 默认redis端口
     */
    private static final Integer DEFAULT_PORT = 6379;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 支持单机 主从 推荐redis版本在4.x从而支持psync2
     */
    @NotEmpty(message = "源redis必填:example[singleAlone:127.0.0.1:3006|masterSlave:127.0.0.1:3006;127.0.0.1:3307]")
    private String sourceRedises;

    /**
     * 参与select和extract阶段的机器,多node则支持HA
     */
    @NotEmpty(message = "源select必填:example[singleAlone:127.0.0.1|HA:127.0.0.1;127.0.0.2]")
    private String selectNodes;

    /**
     * 目的地暂时不支持级联[数据不丢失关于级联会比较麻烦，后期在考虑]
     */
    @NotEmpty(message = "源select必填:example[127.0.0.1]")
    private String targetRedis;

    /**
     * 配置参与load阶段的nodes，多node则支持HA
     */
    @NotEmpty(message = "源select必填:example[singleAlone:127.0.0.1|HA:127.0.0.1;127.0.0.2]")
    private String loadNodes;

    /**
     * 推荐max不要超过 core * 2太多
     */
    @NotNull(message = "请配置并行度")
    @Range(min = 1, max = 128, message = "请合理配置并行度")
    private Integer parallelism;
    /**
     * 0任务停止1任务开启2启动中 3 停止中
     */
    private Integer status = stop;


    /**
     * isMaster 表示是否为主机房  rdb 时只会将主机房同步到双活机房 ;
     */
    private Integer isMaster;


    public void start() {
        this.status = start;
    }

    public void addPort() {
        // 添加的时候 sl node 有可能不在线 或者不存在 这里并不校验 启动去校验 保持一定的灵活性
        String[] masterSlaveSourceRedises = sourceRedises.split(MULT_NODE_SPLIT);
        for (String masterAndSlave : masterSlaveSourceRedises) {
            // 匹配ip:port 或者ip
            String[] ipAndPort = masterAndSlave.split(IP_PORT_SPLIT);
            if (ipAndPort.length == 1) {
                masterAndSlave += DEFAULT_PORT;
            }
        }

        // ip:port 或者ip
        String[] ipAndPort = targetRedis.split(IP_PORT_SPLIT);
        if (ipAndPort.length == 1) {
            targetRedis +=":"+ DEFAULT_PORT;
        }
    }

    public boolean isStart() {
        return status == start;
    }

    /**
     * 选择参与同步任务的selectNode
     *
     * @return
     */
    public List<String> getSelectNodeList() {
        String[] selectNodes = this.selectNodes.split(MULT_NODE_SPLIT);
        List<String> selectNodesList = Arrays.asList(selectNodes);
      return selectNodesList;
    }



    public void checkTarget() {
        Asserts.check(targetRedis.split(MULT_NODE_SPLIT).length == 1, RotterResponse.BizCodeAndMsg.TARGET_NUM_NO_MORE_THAN_ONE);
    }

    public List<String> getLoadNodeList() {
        String[] loadNodes = this.loadNodes.split(MULT_NODE_SPLIT);
        List<String> loadNodesList = Arrays.asList(loadNodes);
        // 使得所有的节点随机的进行工作
        return loadNodesList;
    }
}
