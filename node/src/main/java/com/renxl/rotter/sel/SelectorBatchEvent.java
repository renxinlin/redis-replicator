package com.renxl.rotter.sel;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-04 19:27
 */
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
public class SelectorBatchEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * aof rdb 事件
     */
    private List<SelectorEvent> selectorEvent;

    /**
     * 递增序列号
     */
    private long batchId;


}
