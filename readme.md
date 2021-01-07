设计初衷:
解决同城双活中，redis的数据同步问题!

借鉴otter架构
采用 滑动窗口进行数据传输



设计目标:
保障数据尽可能flyp[不落地]
保障数据不丢失[ack]
保障数据准实时[多线程+pipeline]
保障顺序性:[]
解决mysql反向污染
解决数据回环
提供插件动态加载功能

提供monitor功能
关于监控: 对接监控体系 prometheus  
只提供基础监控,可定制开发

HA: 同步任务自动切换

关于链路追踪:由APM自行实现 

支持redis的主从切换


采用技术组件:


Select 层[redis-replicator]


调度 层[zookeeper]


rpc层[dubbo rpc]



选择队列[disruptor,磁盘和内存混合队列MixBlockingQueue][canal堆积能力差,到底是选择内存型还是采用磁盘形]
由于设计目标就是准实时，所以还是采用disruptor,如果出现了堆积，其本身也是问题





并行




Selected阶段采用配置的replicator进行ha
L的阶段同理



整体采用sel架构 没有t;非结构化的优势

zookeeper                                                                             zk 通知 [1，2，3，4，5]滑动窗口大小           

串行       解耦socket阻塞                 并行【回环过滤,冲突算法】  并行                                                  并行     
replicator [buffer] consume【bsize】  select [缓冲buffer]  e 顺序标 提取 处理  [ put waiting buffer get ack]    eventsend        l[并行提取 串行load ] ack
                     windowsize
                           queue   
pipe[]




Select和e是同机房

l是






zookeeper信息
滑动窗口信息
zookeepr
/rotter/window/{pipelined}/[{},{},{}]
















redis aof 










对redis Command 的划分

1  存在key SetCommand
2  存在keys[PFMergeCommand]
3  特殊的key特殊处理[MSetCommand]
4 不需要处理的key[ReplConfCommand]
5 生产禁止的command[FlushAllCommand]
private byte[][] keys;
BitOpCommand
DelCommand
PFCountCommand
PFMergeCommand
SDiffStoreCommand
SInterStoreCommand
SUnionStoreCommand
UnLinkCommand

==
SelectCommand
==





MSetCommand
MSetNxCommand

SMoveCommand

EvalCommand
EvalShaCommand
PublishCommand

ReplConfCommand
ReplConfGetAckCommand
ScriptCommand
ScriptFlushCommand
ScriptLoadCommand
ZUnionStoreCommand

ExecCommand


BRPopLPushCommand

PingCommand

MultiCommand【标记一个事务】
FlushAllCommand
FlushDBCommand
SwapDBCommand














第一步:
基本版redis双向同步工具完毕


第二步: 重试
增加滑动窗口重试
和滑动窗口幂等处理

第三步: ha
完成node故障转移

第四步: 接入prometheus 不考虑skywalking插件开发

第五步: 部署细化到端口级别  [目前一台机器只能部署一个rotter 不然会造成元数据混乱]

第六步: 采用之前搞得那套layUi开发web管控台








