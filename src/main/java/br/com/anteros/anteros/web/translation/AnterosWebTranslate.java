/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.anteros.web.translation;

import br.com.anteros.core.translation.AbstractCoreTranslate;
import br.com.anteros.core.translation.TranslateMessage;

/**
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class AnterosWebTranslate extends AbstractCoreTranslate {

	private static AnterosWebTranslate singleton;

	public static AnterosWebTranslate getInstance() {
        if ( singleton == null )
            singleton = new AnterosWebTranslate(AnterosWebTranslateMessages.class);

        return (AnterosWebTranslate) singleton;
    }    
	
	public AnterosWebTranslate(Class<? extends TranslateMessage> translateClass) {
		super(translateClass);
	}

}