package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.OtherItem;
import com.community.api.entity.Qualification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class OtherItemService
{
    protected  EntityManager entityManager;

    public OtherItemService(EntityManager entityManager)
    {
        this.entityManager=entityManager;
    }

    public OtherItem addOtherField(OtherItem otherItem)
    {
        if(otherItem.getTyped_text()==null || otherItem.getTyped_text().trim().isEmpty())
        {
            throw new IllegalArgumentException("You have to enter some text. It cannot be null or Empty");
        }
        entityManager.persist(otherItem);
        return otherItem;
    }
    public List<OtherItem> getAllOtherItems() {
        TypedQuery<OtherItem> query = entityManager.createQuery(Constant.FIND_ALL_OTHER_ITEMS, OtherItem.class);
        List<OtherItem> otherItemList = query.getResultList();
        return otherItemList;
    }
}
