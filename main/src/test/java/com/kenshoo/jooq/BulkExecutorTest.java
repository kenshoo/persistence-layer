package com.kenshoo.jooq;

import org.jooq.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by khiloj on 3/23/16
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BulkExecutorTest {
    @Mock
    private Query query;

    @Test
    public void testTotalUpdatedRecords() {
        when(query.execute()).thenReturn(10).thenReturn(5).thenReturn(0);
        assertThat(BulkExecutor.updateTillNoRowsAffected(query), is(15));
        verify(query, times(3)).execute();
    }


}