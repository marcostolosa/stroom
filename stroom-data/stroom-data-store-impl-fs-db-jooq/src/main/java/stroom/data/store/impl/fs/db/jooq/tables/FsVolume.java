/*
 * This file is generated by jOOQ.
 */
package stroom.data.store.impl.fs.db.jooq.tables;


import stroom.data.store.impl.fs.db.jooq.Indexes;
import stroom.data.store.impl.fs.db.jooq.Keys;
import stroom.data.store.impl.fs.db.jooq.Stroom;
import stroom.data.store.impl.fs.db.jooq.tables.records.FsVolumeRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row10;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class FsVolume extends TableImpl<FsVolumeRecord> {

    private static final long serialVersionUID = -1741693314;

    /**
     * The reference instance of <code>stroom.fs_volume</code>
     */
    public static final FsVolume FS_VOLUME = new FsVolume();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<FsVolumeRecord> getRecordType() {
        return FsVolumeRecord.class;
    }

    /**
     * The column <code>stroom.fs_volume.id</code>.
     */
    public final TableField<FsVolumeRecord, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>stroom.fs_volume.version</code>.
     */
    public final TableField<FsVolumeRecord, Integer> VERSION = createField(DSL.name("version"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.create_time_ms</code>.
     */
    public final TableField<FsVolumeRecord, Long> CREATE_TIME_MS = createField(DSL.name("create_time_ms"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.create_user</code>.
     */
    public final TableField<FsVolumeRecord, String> CREATE_USER = createField(DSL.name("create_user"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.update_time_ms</code>.
     */
    public final TableField<FsVolumeRecord, Long> UPDATE_TIME_MS = createField(DSL.name("update_time_ms"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.update_user</code>.
     */
    public final TableField<FsVolumeRecord, String> UPDATE_USER = createField(DSL.name("update_user"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.path</code>.
     */
    public final TableField<FsVolumeRecord, String> PATH = createField(DSL.name("path"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.status</code>.
     */
    public final TableField<FsVolumeRecord, Byte> STATUS = createField(DSL.name("status"), org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>stroom.fs_volume.byte_limit</code>.
     */
    public final TableField<FsVolumeRecord, Long> BYTE_LIMIT = createField(DSL.name("byte_limit"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.fs_volume.fk_fs_volume_state_id</code>.
     */
    public final TableField<FsVolumeRecord, Integer> FK_FS_VOLUME_STATE_ID = createField(DSL.name("fk_fs_volume_state_id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>stroom.fs_volume</code> table reference
     */
    public FsVolume() {
        this(DSL.name("fs_volume"), null);
    }

    /**
     * Create an aliased <code>stroom.fs_volume</code> table reference
     */
    public FsVolume(String alias) {
        this(DSL.name(alias), FS_VOLUME);
    }

    /**
     * Create an aliased <code>stroom.fs_volume</code> table reference
     */
    public FsVolume(Name alias) {
        this(alias, FS_VOLUME);
    }

    private FsVolume(Name alias, Table<FsVolumeRecord> aliased) {
        this(alias, aliased, null);
    }

    private FsVolume(Name alias, Table<FsVolumeRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> FsVolume(Table<O> child, ForeignKey<O, FsVolumeRecord> key) {
        super(child, key, FS_VOLUME);
    }

    @Override
    public Schema getSchema() {
        return Stroom.STROOM;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.FS_VOLUME_FS_VOLUME_FK_FS_VOLUME_STATE_ID, Indexes.FS_VOLUME_PATH, Indexes.FS_VOLUME_PRIMARY);
    }

    @Override
    public Identity<FsVolumeRecord, Integer> getIdentity() {
        return Keys.IDENTITY_FS_VOLUME;
    }

    @Override
    public UniqueKey<FsVolumeRecord> getPrimaryKey() {
        return Keys.KEY_FS_VOLUME_PRIMARY;
    }

    @Override
    public List<UniqueKey<FsVolumeRecord>> getKeys() {
        return Arrays.<UniqueKey<FsVolumeRecord>>asList(Keys.KEY_FS_VOLUME_PRIMARY, Keys.KEY_FS_VOLUME_PATH);
    }

    @Override
    public List<ForeignKey<FsVolumeRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<FsVolumeRecord, ?>>asList(Keys.FS_VOLUME_FK_FS_VOLUME_STATE_ID);
    }

    public FsVolumeState fsVolumeState() {
        return new FsVolumeState(this, Keys.FS_VOLUME_FK_FS_VOLUME_STATE_ID);
    }

    @Override
    public TableField<FsVolumeRecord, Integer> getRecordVersion() {
        return VERSION;
    }

    @Override
    public FsVolume as(String alias) {
        return new FsVolume(DSL.name(alias), this);
    }

    @Override
    public FsVolume as(Name alias) {
        return new FsVolume(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public FsVolume rename(String name) {
        return new FsVolume(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public FsVolume rename(Name name) {
        return new FsVolume(name, null);
    }

    // -------------------------------------------------------------------------
    // Row10 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row10<Integer, Integer, Long, String, Long, String, String, Byte, Long, Integer> fieldsRow() {
        return (Row10) super.fieldsRow();
    }
}
