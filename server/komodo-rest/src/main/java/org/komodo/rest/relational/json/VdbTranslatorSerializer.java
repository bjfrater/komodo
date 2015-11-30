/*
* JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.komodo.rest.relational.json;

import org.komodo.rest.relational.RestVdbTranslator;
import org.komodo.utils.StringUtils;

/**
 * A GSON serializer/deserializer for {@link RestVdbTranslator}s.
 */
public final class VdbTranslatorSerializer extends BasicEntitySerializer<RestVdbTranslator> {

    @Override
    protected boolean isComplete(final RestVdbTranslator translator) {
        return super.isComplete(translator) && (!StringUtils.isBlank(translator.getId()) && !StringUtils.isBlank(translator.getType()));
    }

    @Override
    protected RestVdbTranslator createEntity() {
        return new RestVdbTranslator();
    }
}