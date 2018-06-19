/*
 * This file is generated by jOOQ.
*/
package stroom.data.meta.impl.db.stroom;


import javax.annotation.Generated;

import stroom.data.meta.impl.db.stroom.tables.MetaKey;
import stroom.data.meta.impl.db.stroom.tables.MetaNumericValue;
import stroom.data.meta.impl.db.stroom.tables.Stream;
import stroom.data.meta.impl.db.stroom.tables.StreamFeed;
import stroom.data.meta.impl.db.stroom.tables.StreamProcessor;
import stroom.data.meta.impl.db.stroom.tables.StreamType;


/**
 * Convenience access to all tables in stroom
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>stroom.meta_key</code>.
     */
    public static final MetaKey META_KEY = stroom.data.meta.impl.db.stroom.tables.MetaKey.META_KEY;

    /**
     * The table <code>stroom.meta_numeric_value</code>.
     */
    public static final MetaNumericValue META_NUMERIC_VALUE = stroom.data.meta.impl.db.stroom.tables.MetaNumericValue.META_NUMERIC_VALUE;

    /**
     * The table <code>stroom.stream</code>.
     */
    public static final Stream STREAM = stroom.data.meta.impl.db.stroom.tables.Stream.STREAM;

    /**
     * The table <code>stroom.stream_feed</code>.
     */
    public static final StreamFeed STREAM_FEED = stroom.data.meta.impl.db.stroom.tables.StreamFeed.STREAM_FEED;

    /**
     * The table <code>stroom.stream_processor</code>.
     */
    public static final StreamProcessor STREAM_PROCESSOR = stroom.data.meta.impl.db.stroom.tables.StreamProcessor.STREAM_PROCESSOR;

    /**
     * The table <code>stroom.stream_type</code>.
     */
    public static final StreamType STREAM_TYPE = stroom.data.meta.impl.db.stroom.tables.StreamType.STREAM_TYPE;
}
