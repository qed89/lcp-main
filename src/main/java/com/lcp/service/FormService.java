package com.lcp.service;

import com.lcp.dao.FormDao;
import com.lcp.model.Form;
import java.util.List;
import java.util.UUID;

public class FormService {
    private FormDao formDao = new FormDao();

    public List<Form> getFormsByUser(Long userId, int page, int pageSize) {
        return formDao.getFormsByUser(userId, page, pageSize);
    }

    public Form getFormById(UUID id) {
        return formDao.getFormById(id);
    }

    public void saveForm(Form form) {
        formDao.save(form);
    }

    public void updateForm(Form form) {
        formDao.update(form);
    }

    public void deleteForm(UUID id) {
        formDao.delete(id);
    }

    public long countFormsByUser(Long userId) {
        return formDao.countFormsByUser(userId);
    }
}