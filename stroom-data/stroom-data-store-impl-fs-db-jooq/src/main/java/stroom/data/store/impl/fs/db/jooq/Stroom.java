/*
 * This file is generated by jOOQ.
 */
package stroom.data.store.impl.fs.db.jooq;


import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import stroom.data.store.impl.fs.db.jooq.tables.FsFeedPath;
import stroom.data.store.impl.fs.db.jooq.tables.FsMetaVolume;
import stroom.data.store.impl.fs.db.jooq.tables.FsTypePath;
import stroom.data.store.impl.fs.db.jooq.tables.FsVolume;
import stroom.data.store.impl.fs.db.jooq.tables.FsVolumeState;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Stroom extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>stroom</code>
     */
    public static final Stroom STROOM = new Stroom();

    /**
     * The table <code>stroom.fs_feed_path</code>.
     */
    public final FsFeedPath FS_FEED_PATH = FsFeedPath.FS_FEED_PATH;

    /**
     * The table <code>stroom.fs_meta_volume</code>.
     */
    public final FsMetaVolume FS_META_VOLUME = FsMetaVolume.FS_META_VOLUME;

    /**
     * The table <code>stroom.fs_type_path</code>.
     */
    public final FsTypePath FS_TYPE_PATH = FsTypePath.FS_TYPE_PATH;

    /**
     * The table <code>stroom.fs_volume</code>.
     */
    public final FsVolume FS_VOLUME = FsVolume.FS_VOLUME;

    /**
     * The table <code>stroom.fs_volume_state</code>.
     */
    public final FsVolumeState FS_VOLUME_STATE = FsVolumeState.FS_VOLUME_STATE;

    /**
     * No further instances allowed
     */
    private Stroom() {
        super("stroom", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            FsFeedPath.FS_FEED_PATH,
            FsMetaVolume.FS_META_VOLUME,
            FsTypePath.FS_TYPE_PATH,
            FsVolume.FS_VOLUME,
            FsVolumeState.FS_VOLUME_STATE
        );
    }
}
