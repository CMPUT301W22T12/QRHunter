package com.example.qrhunter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkerTest {

    private locations setUp() {
        locations location = new locations(14, 14, "14");
        return location;
    }

    @Test
    void testGetLongitude() {
        locations location = setUp();
        assertEquals(14, location.getLongitude());
    }

    @Test
    void testGetLatitude() {
        locations location = setUp();
        assertEquals(14, location.getLatitude());
    }

    @Test
    void testGetScore() {
        locations location = setUp();
        assertEquals("14", location.getScore());
    }

}
