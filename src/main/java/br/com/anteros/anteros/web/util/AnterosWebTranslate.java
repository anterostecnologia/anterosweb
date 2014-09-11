package br.com.anteros.anteros.web.util;

import br.com.anteros.core.utils.AbstractCoreTranslate;

public class AnterosWebTranslate extends AbstractCoreTranslate {


	public AnterosWebTranslate(String messageBundleName) {
		super(messageBundleName);
	}

	private static AnterosWebTranslate translate;
	
	public static AnterosWebTranslate getInstance(){
		if (translate==null){
			translate = new AnterosWebTranslate("anterossweb_messages");
		}
		return translate;
	}
}
