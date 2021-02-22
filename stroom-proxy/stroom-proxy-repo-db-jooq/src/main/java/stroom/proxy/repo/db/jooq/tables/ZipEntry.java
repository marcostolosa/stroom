/*
 * This file is generated by jOOQ.
 */
package stroom.proxy.repo.db.jooq.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import stroom.proxy.repo.db.jooq.DefaultSchema;
import stroom.proxy.repo.db.jooq.Keys;
import stroom.proxy.repo.db.jooq.tables.records.ZipEntryRecord;


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
public class ZipEntry extends TableImpl<ZipEntryRecord> {

    private static final long serialVersionUID = -1244655737;

    /**
     * The reference instance of <code>zip_entry</code>
     */
    public static final ZipEntry ZIP_ENTRY = new ZipEntry();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ZipEntryRecord> getRecordType() {
        return ZipEntryRecord.class;
    }

    /**
     * The column <code>zip_entry.id</code>.
     */
    public final TableField<ZipEntryRecord, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>zip_entry.extension</code>.
     */
    public final TableField<ZipEntryRecord, String> EXTENSION = createField(DSL.name("extension"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>zip_entry.byte_size</code>.
     */
    public final TableField<ZipEntryRecord, Long> BYTE_SIZE = createField(DSL.name("byte_size"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>zip_entry.fk_zip_data_id</code>.
     */
    public final TableField<ZipEntryRecord, Integer> FK_ZIP_DATA_ID = createField(DSL.name("fk_zip_data_id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>zip_entry</code> table reference
     */
    public ZipEntry() {
        this(DSL.name("zip_entry"), null);
    }

    /**
     * Create an aliased <code>zip_entry</code> table reference
     */
    public ZipEntry(String alias) {
        this(DSL.name(alias), ZIP_ENTRY);
    }

    /**
     * Create an aliased <code>zip_entry</code> table reference
     */
    public ZipEntry(Name alias) {
        this(alias, ZIP_ENTRY);
    }

    private ZipEntry(Name alias, Table<ZipEntryRecord> aliased) {
        this(alias, aliased, null);
    }

    private ZipEntry(Name alias, Table<ZipEntryRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> ZipEntry(Table<O> child, ForeignKey<O, ZipEntryRecord> key) {
        super(child, key, ZIP_ENTRY);
    }

    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<ZipEntryRecord> getPrimaryKey() {
        return Keys.PK_ZIP_ENTRY;
    }

    @Override
    public List<UniqueKey<ZipEntryRecord>> getKeys() {
        return Arrays.<UniqueKey<ZipEntryRecord>>asList(Keys.PK_ZIP_ENTRY);
    }

    @Override
    public List<ForeignKey<ZipEntryRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ZipEntryRecord, ?>>asList(Keys.FK_ZIP_ENTRY_ZIP_DATA_1);
    }

    public ZipData zipData() {
        return new ZipData(this, Keys.FK_ZIP_ENTRY_ZIP_DATA_1);
    }

    @Override
    public ZipEntry as(String alias) {
        return new ZipEntry(DSL.name(alias), this);
    }

    @Override
    public ZipEntry as(Name alias) {
        return new ZipEntry(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ZipEntry rename(String name) {
        return new ZipEntry(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ZipEntry rename(Name name) {
        return new ZipEntry(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, String, Long, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
