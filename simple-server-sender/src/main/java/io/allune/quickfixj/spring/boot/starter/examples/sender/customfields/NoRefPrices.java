package io.allune.quickfixj.spring.boot.starter.examples.sender.customfields;

import quickfix.Group;

public class NoRefPrices extends Group{
	
	private static final int[] ORDER = {22078, 22079, 22080, 22080, 22081};

	public NoRefPrices() {
		super(22078, 22079, ORDER);
	}

	

}
