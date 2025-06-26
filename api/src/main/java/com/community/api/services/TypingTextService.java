package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Image;
import com.community.api.entity.Qualification;
import com.community.api.entity.TypingText;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class TypingTextService
{

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public List<TypingText> getAllRandomTypingTexts(Boolean archived)
    {
        TypedQuery<TypingText> typedQuery= entityManager.createQuery(Constant.GET_ALL_RANDOM_TYPING_TEXT,TypingText.class);
        typedQuery.setParameter("archived",archived);
        List<TypingText> typingTexts = typedQuery.getResultList();
        return typingTexts;
    }

    @Transactional
    public List<TypingText> getAllArchivedNonArchivedRandomTypingTexts()
    {
        TypedQuery<TypingText> typedQuery= entityManager.createQuery(Constant.GET_ALL_ARCHIVE_UNARCHIVE_RANDOM_TYPING_TEXT,TypingText.class);
        List<TypingText> typingTexts = typedQuery.getResultList();
        return typingTexts;
    }

    @Transactional
    public List<TypingText> addAllRandomTypingTexts(List<TypingText> typingTexts)
    {
        List<TypingText> typingTextsListToAdd = new ArrayList<>();
        for(TypingText typedText : typingTexts)
        {
            TypingText typingTextToAdd =new TypingText();
            long id = findMaxId() + 1;
            if (typedText.getText() == null || typedText.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Typing text cannot be empty or consist only of whitespace");
            }
            String[] words = typedText.getText().split("\\s+");
            if (words.length < 30) {
                throw new IllegalArgumentException("Typing text must contain at least 30 words. Current word count: " + words.length);
            }
            List<TypingText> existingTypingText = getAllArchivedNonArchivedRandomTypingTexts();
            for (TypingText existingTypingText1: existingTypingText) {
                if (existingTypingText1.getText().trim().equalsIgnoreCase(typedText.getText().trim())) {
                    throw new IllegalArgumentException("Typing Text with name '"+typedText.getText().trim()+"' already exists");
                }
            }
            typingTextToAdd.setId(id);
            typingTextToAdd.setText(typedText.getText().trim());
            typingTextsListToAdd.add(typingTextToAdd);
            entityManager.persist(typingTextToAdd);
        }
        return typingTextsListToAdd;
    }

    public long findMaxId() {
        return  entityManager.createQuery("SELECT COALESCE(MAX(t.id), 0) FROM TypingText t", Long.class).getSingleResult();
    }

    @Transactional
    public TypingText archiveOrUnarchiveTypingText(Long typingTextId, Boolean archive)
    {
        TypingText typingText= entityManager.find(TypingText.class,typingTextId);
        if(typingText==null)
        {
            throw new IllegalArgumentException("No typing text exists in db with id "+ typingTextId);
        }
        if (archive) {
            if(typingText.getArchived().equals(true))
            {
                throw new IllegalArgumentException("Typing text is already archived");
            }
            typingText.setArchived(true);
        } else {
            if (!typingText.getArchived()) {
                throw new IllegalArgumentException("Typing text already unarchived");
            }
            typingText.setArchived(false);
        }
        entityManager.merge(typingText);
        return typingText;
    }

    @Transactional
    public TypingText updateTypingText(TypingText typingText, Long typingTextId)
    {
        TypingText typingTextToUpdate= entityManager.find(TypingText.class,typingTextId);
        if(typingTextToUpdate==null)
        {
            throw new IllegalArgumentException("Typing text not found");
        }

        if(typingText.getText()!=null)
        {
            if (typingText.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Typing text cannot be empty or consist only of whitespace");
            }
            String[] words = typingText.getText().split("\\s+");
            if (words.length < 30) {
                throw new IllegalArgumentException("Typing text must contain at least 30 words. Current word count: " + words.length);
            }
            List<TypingText> existingTypingText = getAllArchivedNonArchivedRandomTypingTexts();
            for (TypingText existingTypingText1: existingTypingText) {
                if (existingTypingText1.getText().trim().equalsIgnoreCase(typingText.getText().trim()) && !existingTypingText1.getId().equals(typingTextId)) {
                    throw new IllegalArgumentException("Typing Text with name '"+typingText.getText().trim()+"' already exists");
                }
            }
            typingTextToUpdate.setText(typingText.getText().trim());
        }
        entityManager.merge(typingTextToUpdate);
        return typingTextToUpdate;
    }
}
