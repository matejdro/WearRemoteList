package com.matejdro.wearremotelist;

import com.matejdro.wearremotelist.parcelables.StringParcelableWraper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringParcelableWraperTest
{
    private static final String SAMPLE_STRING = "čžščšđčšksldfgiufdhgiuhfduighdufi8912972743950pojfsiljtžččđ152465498489749848";

    @Test
    public void testConstructor()
    {
        StringParcelableWraper testStringParcelableWraper = new StringParcelableWraper(SAMPLE_STRING);
        assertEquals(SAMPLE_STRING, testStringParcelableWraper.getString());
    }

    @Test
    public void testEquals()
    {
        StringParcelableWraper psA = new StringParcelableWraper(SAMPLE_STRING);
        StringParcelableWraper psB = new StringParcelableWraper(SAMPLE_STRING);

        assertEquals(psA.getString(), psB.getString());
        assertEquals(psA, psB);
    }

    @Test
    public void testHashCode()
    {
        StringParcelableWraper psA = new StringParcelableWraper(SAMPLE_STRING);
        StringParcelableWraper psB = new StringParcelableWraper(SAMPLE_STRING);

        assertEquals(psA.getString().hashCode(), psB.getString().hashCode());
        assertEquals(psA.hashCode(), psB.hashCode());
        assertEquals(psA.getString().hashCode(), psB.hashCode());
        assertEquals(psA.hashCode(), psB.getString().hashCode());
    }

    @Test
    public void testToString()
    {
        StringParcelableWraper testStringParcelableWraper = new StringParcelableWraper(SAMPLE_STRING);
        assertEquals(SAMPLE_STRING, testStringParcelableWraper.toString());
    }
}