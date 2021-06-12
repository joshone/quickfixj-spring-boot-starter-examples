package io.allune.quickfixj.spring.boot.starter.examples.sender.customfields;

import quickfix.Group;

public class NoCompDealerQuotes extends Group{
	
	private static final int[] ORDER = {10009, 10010, 10011, 22485, 22486};

	public NoCompDealerQuotes() {
		super(10009, 10010, ORDER);
	}

	

}
