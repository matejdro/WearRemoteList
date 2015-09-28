package com.matejdro.wearremotelist.receiverside;

import android.os.Parcelable;

import com.matejdro.wearremotelist.parcelables.StringParcelableWraper;
import com.matejdro.wearremotelist.receiverside.conn.ConnectionToProvider;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class RemoteListImplTest
{
    @Test
    public void testBasicRequest()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListImpl<StringParcelableWraper> list = new RemoteListImpl<>("test", StringParcelableWraper.CREATOR, connection, 10, 10);

        list.updateSizeReceived("test", 20);
        assertEquals(20, list.size());

        assertNull(list.get(10));
        verify(connection).requestItems("test", 0, 19);

        StringParcelableWraper testString = new StringParcelableWraper("testString");
        list.dataReceived("test", 10, new Parcelable[]{testString});

        reset(connection);
        assertSame(testString, list.get(10));
        verify(connection, never()).requestItems(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void testAutomaticShiftingDownwards()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListImpl<StringParcelableWraper> list = new RemoteListImpl<>("test", StringParcelableWraper.CREATOR, connection, 5, 5);

        list.updateSizeReceived("test", 20);

        assertNull(list.get(10));
        verify(connection).requestItems("test", 5, 15);

        reset(connection);
        list.dataReceived("test", 15, new Parcelable[]{new StringParcelableWraper("dummy")});
        assertNull(list.get(10));
        verify(connection).requestItems("test", 4, 14);

        reset(connection);
        list.dataReceived("test", 14, new Parcelable[]{new StringParcelableWraper("dummy")});
        assertNull(list.get(10));
        verify(connection).requestItems("test", 3, 13);

        reset(connection);
        list.loadItems(10, true);
        verify(connection).requestItems("test", 5, 15);


        reset(connection);
        list = new RemoteListImpl<>("test", StringParcelableWraper.CREATOR, connection, 5, 5);
        list.updateSizeReceived("test", 20);
        assertNull(list.get(19));
        verify(connection).requestItems("test", 9, 19);
    }

    @Test
    public void testAutomaticShiftingUpwards()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListImpl<StringParcelableWraper> list = new RemoteListImpl<>("test", StringParcelableWraper.CREATOR, connection, 5, 5);

        list.updateSizeReceived("test", 20);

        assertNull(list.get(10));
        verify(connection).requestItems("test", 5, 15);

        reset(connection);
        list.dataReceived("test", 5, new Parcelable[]{new StringParcelableWraper("dummy")});
        assertNull(list.get(10));
        verify(connection).requestItems("test", 6, 16);

        reset(connection);
        list.dataReceived("test", 6, new Parcelable[]{new StringParcelableWraper("dummy")});
        assertNull(list.get(10));
        verify(connection).requestItems("test", 7, 17);

        reset(connection);
        list.loadItems(10, true);
        verify(connection).requestItems("test", 5, 15);

        reset(connection);
        list = new RemoteListImpl<>("test", StringParcelableWraper.CREATOR, connection, 5, 5);
        list.updateSizeReceived("test", 20);
        assertNull(list.get(0));
        verify(connection).requestItems("test", 0, 10);
    }

    @Test
    public void testInvalidate()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListImpl<StringParcelableWraper> list = new RemoteListImpl<>("test", StringParcelableWraper.CREATOR, connection, 5, 5);
        list.updateSizeReceived("test", 20);

        list.invalidate();
        assertEquals(0, list.size());
        verify(connection).requestListSize("test");
    }
}