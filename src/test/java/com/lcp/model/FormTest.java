package com.lcp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class FormTest {

    @Test
    void testFormGettersAndSetters() {
        Form form = new Form();
        UUID id = UUID.randomUUID();
        String name = "Test Form";
        String createdDate = "2023-10-01";
        User user = new User();

        form.setId(id);
        form.setName(name);
        form.setCreatedDate(createdDate);
        form.setUser(user);

        assertEquals(id, form.getId());
        assertEquals(name, form.getName());
        assertEquals(createdDate, form.getCreatedDate());
        assertEquals(user, form.getUser());
    }
}