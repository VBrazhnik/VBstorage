package com.vbrazhnik.vbstorage.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ItemToTag {

    @Id
    private Long id;

    @Property(nameInDb = "id_item")
    private Long idItem;

    @Property(nameInDb = "id_tag")
    private Long idTag;

    public Long getIdTag() {
        return this.idTag;
    }

    public void setIdTag(Long idTag) {
        this.idTag = idTag;
    }

    public Long getIdItem() {
        return this.idItem;
    }

    public void setIdItem(Long idItem) {
        this.idItem = idItem;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Generated(hash = 722794020)
    public ItemToTag(Long id, Long idItem, Long idTag) {
        this.id = id;
        this.idItem = idItem;
        this.idTag = idTag;
    }

    @Generated(hash = 594719119)
    public ItemToTag() {
    }
}
