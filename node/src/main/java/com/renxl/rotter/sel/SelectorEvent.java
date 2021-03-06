package com.renxl.rotter.sel;

import com.moilioncircle.redis.replicator.cmd.impl.AbstractCommand;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-04 19:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SelectorEvent  implements Serializable {

    private static final long serialVersionUID = 1L;


    private AbstractCommand abstartCommand;

    private KeyValuePair keyValuePair;
}
