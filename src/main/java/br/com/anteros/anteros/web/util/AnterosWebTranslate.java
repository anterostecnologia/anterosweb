package br.com.anteros.anteros.web.util;

import br.com.anteros.core.utils.AbstractCoreTranslate;

public class AnterosWebTranslate extends AbstractCoreTranslate {


	private AnterosWebTranslate(String messageBundleName) {
		super(messageBundleName);
	}
	

	static {
		setInstance(new AnterosWebTranslate("anterossweb_messages"));
	}

}
