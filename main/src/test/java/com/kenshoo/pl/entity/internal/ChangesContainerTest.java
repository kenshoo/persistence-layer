package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityChange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ChangesContainerTest {

    @Mock
    private DataTable dataTable;

    @Mock
    private EntityChange<?> entityChange;

    @Test
    public void getInsertByTableAndEntityChange_WhenEverythingExists_ShouldReturnCreateCmd() {

    }
}