package com.vbrazhnik.vbstorage.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import java.util.Objects;

import org.greenrobot.greendao.DaoException;

@Entity(active = true)
public class Item {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "type")
    @NotNull
    private int     type;

    @Property(nameInDb = "title")
    private String  title;

    @Property(nameInDb = "text")
    private String  text;

    @Property(nameInDb = "image_path")
    private String imagePath;

    @Property(nameInDb = "attach_path")
    private String  attachPath;

    @Property(nameInDb = "deleted")
    private boolean deleted;

    @Property(nameInDb = "time")
    private long    time;

    @ToMany
    @JoinEntity(entity = ItemToTag.class, sourceProperty = "idItem", targetProperty = "idTag")
    private List<Tag> tags;

    /** Used for active entity operations. */
    @Generated(hash = 182764869)
    private transient ItemDao myDao;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    public Item() {
    }

    @Keep
    public Item(Long id, int type, String title, String text, String imagePath, String attachPath,
            long time) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.text = text;
        this.imagePath = imagePath;
        this.attachPath = attachPath;
        this.deleted = false;
        this.time = time;
    }

    @Generated(hash = 946061361)
    public Item(Long id, int type, String title, String text, String imagePath, String attachPath,
            boolean deleted, long time) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.text = text;
        this.imagePath = imagePath;
        this.attachPath = attachPath;
        this.deleted = deleted;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAttachPath() {
        return this.attachPath;
    }

    public void setAttachPath(String attachPath) {
        this.attachPath = attachPath;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Item && Objects.equals(((Item) obj).getId(), this.getId());
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 404234)
    public synchronized void resetTags() {
        tags = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1367823701)
    public List<Tag> getTags() {
        if (tags == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TagDao targetDao = daoSession.getTagDao();
            List<Tag> tagsNew = targetDao._queryItem_Tags(id);
            synchronized (this) {
                if(tags == null) {
                    tags = tagsNew;
                }
            }
        }
        return tags;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 881068859)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getItemDao() : null;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
