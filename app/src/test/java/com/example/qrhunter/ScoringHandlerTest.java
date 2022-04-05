package com.example.qrhunter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScoringHandlerTest {

    private ScoringHandler setUp() {
        ScoringHandler mScoringHandler = new ScoringHandler();
        return mScoringHandler;
    }

    @Test
    void testScore() {
        ScoringHandler mScoringHandler = setUp();
        assertEquals(4, mScoringHandler.score('2', 2));
    }

    @Test
    void testHexStringReader() {
        ScoringHandler mScoringHandler = setUp();
        assertEquals(0, mScoringHandler.hexStringReader("abcd"));
    }

    @Test
    void testSha256() {
        ScoringHandler mScoringHandler = setUp();
        assertEquals("88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589",
                mScoringHandler.sha256("abcd"));
    }
}
