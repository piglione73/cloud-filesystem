package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.Utils;

public class UtilsTest {
	@Test
	public void padLeft() {
		assertEquals("0000000000000000000", Utils.padLeft(0));
		assertEquals("0000000000000000001", Utils.padLeft(1));
		assertEquals("0000000000000000999", Utils.padLeft(999));
		assertEquals("9223372036854775807", Utils.padLeft(Long.MAX_VALUE));
		assertEquals("-0000000000000000001", Utils.padLeft(-1));
		assertEquals("-0000000000000000999", Utils.padLeft(-999));
		assertEquals("-9223372036854775808", Utils.padLeft(Long.MIN_VALUE));
	}
}
