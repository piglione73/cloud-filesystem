package com.paviasystem.cloudfilesystemold.test;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

import com.paviasystem.cloudfilesystemold.Utils;

public class UtilsTest {
	@Test
	public void test_padLeft() {
		assertEquals("0000000000000000000", Utils.padLeft(0));
		assertEquals("0000000000000000001", Utils.padLeft(1));
		assertEquals("0000000000000000999", Utils.padLeft(999));
		assertEquals("9223372036854775807", Utils.padLeft(Long.MAX_VALUE));
		assertEquals("-0000000000000000001", Utils.padLeft(-1));
		assertEquals("-0000000000000000999", Utils.padLeft(-999));
		assertEquals("-9223372036854775808", Utils.padLeft(Long.MIN_VALUE));
	}

	@Test
	public void test_formatTimestamp() {
		assertEquals("1970-01-01T00:00:00.000", Utils.formatTimestamp(new Date(0)));
		assertEquals("1970-01-01T00:00:12.345", Utils.formatTimestamp(new Date(12345)));
	}

	@Test
	public void test_parseTimestamp() throws ParseException {
		assertEquals(new Date(0), Utils.parseTimestamp("1970-01-01T00:00:00.000"));
		assertEquals(new Date(12345), Utils.parseTimestamp("1970-01-01T00:00:12.345"));
	}
}
