package com.matejdro.wearremotelist.receiverside;

import android.os.Parcelable;

import com.matejdro.wearremotelist.parcelables.StringParcelableWraper;
import com.matejdro.wearremotelist.receiverside.conn.ConnectionToProvider;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RemoteListManagerTest
{
    @Test
    public void testRemoteListManager()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListListener listListener = mock(RemoteListListener.class);

        RemoteListManager remoteListManager = new RemoteListManager(connection, listListener);
        ArgumentCaptor<RemoteListManager.ListDataReceiver> argumentCaptor = ArgumentCaptor.forClass(RemoteListManager.ListDataReceiver.class);
        verify(connection, only()).setDataReceiver(argumentCaptor.capture());

        RemoteListManager.ListDataReceiver listDataReceiver = argumentCaptor.getValue();
        assertNotNull(listDataReceiver);

        assertNull(remoteListManager.getExistingList("test"));
        RemoteList<StringParcelableWraper> listA = remoteListManager.createRemoteList("test", StringParcelableWraper.CREATOR, 20, 5);
        RemoteList<StringParcelableWraper> listB = remoteListManager.createRemoteList("test", StringParcelableWraper.CREATOR, 20, 5);
        RemoteList<StringParcelableWraper> listC = remoteListManager.getExistingList("test");

        assertNotSame(listA, listB);
        assertSame(listB, listC);

        listDataReceiver.updateSizeReceived("test", 10);
        assertEquals(0, listA.size());
        assertEquals(10, listB.size());
        verify(listListener, only()).onListSizeChanged("test");

        reset(listListener);
        StringParcelableWraper testString = new StringParcelableWraper("testString");
        listDataReceiver.dataReceived("test", 5, new Parcelable[]{testString});
        assertEquals(testString, listB.get(5));
        verify(listListener, only()).newEntriesTransferred("test", 5, 5);
    }

    @Test
    public void testPriorityQueue()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListListener listListener = mock(RemoteListListener.class);

        RemoteListManager remoteListManager = new RemoteListManager(connection, listListener);
        ArgumentCaptor<RemoteListManager.ListDataReceiver> argumentCaptor = ArgumentCaptor.forClass(RemoteListManager.ListDataReceiver.class);
        verify(connection, only()).setDataReceiver(argumentCaptor.capture());

        RemoteListManager.ListDataReceiver listDataReceiver = argumentCaptor.getValue();

        RemoteList<StringParcelableWraper> listA = remoteListManager.createRemoteList("listA", StringParcelableWraper.CREATOR, 20, 11);
        RemoteList<StringParcelableWraper> listB = remoteListManager.createRemoteList("listB", StringParcelableWraper.CREATOR, 20, 11);

        listDataReceiver.updateSizeReceived("listA", 10);
        listDataReceiver.updateSizeReceived("listB", 10);

        reset(connection);
        listA.loadItems(5, true);
        listB.loadItems(5, true);
        verify(connection, times(1)).requestItems("listA", 0, 9);
        verifyNoMoreInteractions(connection);

        reset(connection);
        listDataReceiver.dataReceived("listA", 5, new StringParcelableWraper[0]);
        verify(connection, times(1)).requestItems("listB", 0, 9);
        verifyNoMoreInteractions(connection);

        listA.setPriority(0);
        listB.setPriority(1);

        reset(connection);
        listA.loadItems(5, true);
        listB.loadItems(5, true);
        listDataReceiver.dataReceived("listB", 5, new StringParcelableWraper[0]);

        verify(connection, times(1)).requestItems("listB", 0, 9);
        verifyNoMoreInteractions(connection);

        reset(connection);
        listDataReceiver.dataReceived("listB", 5, new StringParcelableWraper[0]);
        verify(connection, times(1)).requestItems("listA", 0, 9);
        verifyNoMoreInteractions(connection);
    }

    @Test
    public void testThrottling()
    {
        ConnectionToProvider connection = mock(ConnectionToProvider.class);
        RemoteListListener listListener = mock(RemoteListListener.class);

        RemoteListManager remoteListManager = new RemoteListManager(connection, listListener);
        ArgumentCaptor<RemoteListManager.ListDataReceiver> argumentCaptor = ArgumentCaptor.forClass(RemoteListManager.ListDataReceiver.class);
        verify(connection, only()).setDataReceiver(argumentCaptor.capture());

        RemoteListManager.ListDataReceiver listDataReceiver = argumentCaptor.getValue();
        RemoteList<StringParcelableWraper> list = remoteListManager.createRemoteList("test", StringParcelableWraper.CREATOR, 20, 11);

        listDataReceiver.updateSizeReceived("test", 20);

        reset(connection);
        list.get(5);
        list.get(6);
        verify(connection, only()).requestItems("test", 0, 10);

        reset(connection);
        listDataReceiver.dataReceived("test", 99, new Parcelable[]{new StringParcelableWraper("dummy")});
        verify(connection).requestItems("test", 1, 11);
    }

}