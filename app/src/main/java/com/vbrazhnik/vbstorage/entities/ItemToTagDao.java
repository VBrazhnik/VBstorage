package com.vbrazhnik.vbstorage.entities;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ITEM_TO_TAG".
*/
public class ItemToTagDao extends AbstractDao<ItemToTag, Long> {

    public static final String TABLENAME = "ITEM_TO_TAG";

    /**
     * Properties of entity ItemToTag.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property IdItem = new Property(1, Long.class, "idItem", false, "id_item");
        public final static Property IdTag = new Property(2, Long.class, "idTag", false, "id_tag");
    }


    public ItemToTagDao(DaoConfig config) {
        super(config);
    }
    
    public ItemToTagDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ITEM_TO_TAG\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"id_item\" INTEGER," + // 1: idItem
                "\"id_tag\" INTEGER);"); // 2: idTag
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ITEM_TO_TAG\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ItemToTag entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long idItem = entity.getIdItem();
        if (idItem != null) {
            stmt.bindLong(2, idItem);
        }
 
        Long idTag = entity.getIdTag();
        if (idTag != null) {
            stmt.bindLong(3, idTag);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ItemToTag entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long idItem = entity.getIdItem();
        if (idItem != null) {
            stmt.bindLong(2, idItem);
        }
 
        Long idTag = entity.getIdTag();
        if (idTag != null) {
            stmt.bindLong(3, idTag);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ItemToTag readEntity(Cursor cursor, int offset) {
        ItemToTag entity = new ItemToTag( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // idItem
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2) // idTag
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ItemToTag entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setIdItem(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setIdTag(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ItemToTag entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ItemToTag entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ItemToTag entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
