package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DslListSeparatorTest {

    @Test
    public void toDisplayJoinsWithAnd() {
        assertEquals("Male and false and true", DslListSeparator.toDisplay("Male@@false@@true"));
    }

    @Test
    public void toDslSplitsOnAnd() {
        assertEquals("leftiris@@rightIris", DslListSeparator.toDsl("leftiris and rightIris"));
    }

    @Test
    public void preservesTechnoAtPassword() {
        assertEquals("dsl1@@Techno@123", DslListSeparator.toDsl("dsl1 and Techno@123"));
    }
}
